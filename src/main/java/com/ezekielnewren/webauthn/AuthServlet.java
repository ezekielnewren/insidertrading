package com.ezekielnewren.webauthn;

import com.ezekielnewren.Build;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AuthServlet extends HttpServlet {
    WebAuthn wa;

    @Override
    public void init() throws ServletException {
        wa = new WebAuthn(Build.get("fqdn"), Build.get("title"));
        getServletContext().log(AuthServlet.class.getSimpleName()+" loaded");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>only POST allowed</body></html>");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        ObjectMapper om = wa.getObjectMapper();

        URL url = new URL(request.getRequestURL().toString());
        String path = url.getPath().substring(1);
        List<String> tmp = Arrays.asList(path.split("/"));
        String[] args = tmp.subList(1, tmp.size()).toArray(new String[0]);

        byte[] raw = request.getInputStream().readAllBytes();
        String data;
        try {
            data = new String(raw, request.getCharacterEncoding());
        } catch (NullPointerException|UnsupportedEncodingException e) {
            data = new String(raw, "UTF-8");
        }

        Supplier<String> errMsg = ()-> {
            try {
                response.sendError(400, "bad arguments must be /webauthn/<action>/<state> e.g. /webauthn/register/start");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        if (args.length == 2) {
            if ("register".equals(args[0])) {
                if ("start".equals(args[1])) {
                    String username = data;
                    String json = wa.registerStart(request.getSession(), username, Optional.empty(), null, false);
                    out.println(json);
                } else if ("finish".equals(args[1])) {
                    boolean result = wa.registerFinish(request.getSession(), data);
                    String json = result?"\"good\"":"\"bad\"";
                    out.println(json);
                } else {
                    errMsg.get();
                }
            } else if ("login".equals(args[0])) {
                if ("start".equals(args[1])) {

                } else if ("finish".equals(args[1])) {

                } else {
                    errMsg.get();
                }
            }
        } else {
            errMsg.get();
        }
    }

}
