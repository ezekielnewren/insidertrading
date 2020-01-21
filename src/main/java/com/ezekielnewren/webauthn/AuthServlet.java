package com.ezekielnewren.webauthn;

import com.ezekielnewren.Build;
import com.ezekielnewren.webauthn.data.Authenticator;
import com.ezekielnewren.webauthn.data.RegistrationRequest;
import com.ezekielnewren.webauthn.data.RegistrationResponse;
import com.ezekielnewren.webauthn.data.UserStore;
import org.json.JSONObject;

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
    AuthServletContext ctx;

    @Override
    public void init() throws ServletException {
        if (Util.DEBUG) {
            getServletContext().log("DEBUG MODE");
        }


        ctx = new AuthServletContext(
                JacksonHelper.newObjectMapper(),
                "mongodb://localhost",
                null,
                Build.get("fqdn"),
                Build.get("title")
        );
        getServletContext().log(AuthServlet.class.getSimpleName()+" loaded");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>only POST allowed</body></html>");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            URL url = new URL(request.getRequestURL().toString());
            String path = url.getPath().substring(1);
            List<String> tmp = Arrays.asList(path.split("/"));
            String[] args = tmp.subList(1, tmp.size()).toArray(new String[0]);

            byte[] raw = request.getInputStream().readAllBytes();
            String data;
            try {
                data = new String(raw, request.getCharacterEncoding());
            } catch (NullPointerException | UnsupportedEncodingException e) {
                data = new String(raw, "UTF-8");
            }

            Supplier<String> errMsg = () -> {
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
                        // decode arguments
                        JSONObject jsonTmp = new JSONObject(data);
                        String username = jsonTmp.getString("username");
                        String displayName = jsonTmp.getString("displayName");
                        String nickname = jsonTmp.isNull("nickname") ? null : jsonTmp.getString("nickname");
                        boolean requireResidentKey = jsonTmp.getBoolean("requireResidentKey");

                        // webauthn registration start
                        RegistrationRequest regRequest = ctx.getWebAuthn().registerStart(request.getSession(), username, displayName, nickname, requireResidentKey);

                        // encode registration request and send it to the client
                        String json = ctx.getObjectMapper().writeValueAsString(regRequest);
                        out.println(json);
                    } else if ("finish".equals(args[1])) {
                        // decode arguments
                        RegistrationResponse regResponse = ctx.getObjectMapper().readValue(data, RegistrationResponse.class);

                        // finish webauthn registration
                        boolean result = ctx.getWebAuthn().registerFinish(request.getSession(), regResponse);
//                        String username = result.getUsername();
//
//                        UserStore store = ctx.getUserStore();
//                        RegistrationStorage regStore = store.getRegistrationStorage();
//                        if (result != null) {
//                            regStore.addRegistrationByUsername(username, result);
//                        }

                        // respond to client
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
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException|Error e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
