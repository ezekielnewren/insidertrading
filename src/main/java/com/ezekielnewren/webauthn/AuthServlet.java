package com.ezekielnewren.webauthn;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

public class AuthServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

//    public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException {
//        response.getWriter().println(request.getContentType());
//        super.service(request, response);
//    }
//
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>only POST allowed</body></html>");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

//        byte[] raw = request.getInputStream().readAllBytes();
//        String req = new String(raw, request.getCharacterEncoding());

//        HttpSession sess = req.getSession();
//        sess.setAttribute("loggedin", Boolean.TRUE);
//        boolean x = (boolean) sess.getAttribute("loggedin");


        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String url = request.getRequestURL().toString();
        String json = "{\n" +
                "\"data\": \"It's good to know\",\n" +
                "\"serverName\": \"" + serverName + "\",\n" +
                "\"url\": \"" + url + "\"\n" +
                "}";
        out.println(json);

    }

}
