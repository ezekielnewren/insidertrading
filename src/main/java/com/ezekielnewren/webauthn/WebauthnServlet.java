package com.ezekielnewren.webauthn;

import com.ezekielnewren.Build;
import com.ezekielnewren.webauthn.data.AssertionRequestWrapper;
import com.ezekielnewren.webauthn.data.AssertionResponse;
import com.ezekielnewren.webauthn.data.RegistrationRequest;
import com.ezekielnewren.webauthn.data.RegistrationResponse;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The WebauthnServlet class handles communication with server when user logs in or registers.
 * @see HttpServlet
 * */
public class WebauthnServlet extends HttpServlet {

    /**
     *Object to hold data serialization.
     */
    WebauthnServletContext ctx;

    /**
     * Constructor assigns data to ctx object and logs data on success.
     * @throws ServletException throws a new Servlet Exception.
     */
    @Override
    public void init() throws ServletException {
        if (Util.DEBUG) {
            getServletContext().log("DEBUG MODE");
        }

        ctx = new WebauthnServletContext(
                JacksonHelper.newObjectMapper(),
                "mongodb://localhost",
                null,
                Build.get("fqdn"),
                Build.get("title")
        );

        getServletContext().log(WebauthnServlet.class.getSimpleName()+" loaded");
    }


    /**
     * Displays a html page with 'only POST allowed' on GET request.
     * @param req contains the client request information
     * @param response contains all server response information
     * @throws IOException throws new I/O Exception
     * @see HttpServletRequest
     * @see HttpServletResponse
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>only POST allowed</body></html>");
    }


    /**
     * Displays correct webpage on POST, handles login and registration by error checking and deserializing values.
     * @param request contains the client request information
     * @param response contains all server response information
     * @throws IOException throws new I/O Exception.
     * @see HttpServletRequest
     * @see HttpServletResponse
     */
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
                        JSONObject jsonTmp = new JSONObject(data);
                        String username = jsonTmp.getString("username");
                        boolean requireResidentKey = jsonTmp.getBoolean("requireResidentKey");

                        AssertionRequestWrapper arw = ctx.getWebAuthn().assertionStart(username);

                        String json = ctx.getObjectMapper().writeValueAsString(arw);
                        out.println(json);
                    } else if ("finish".equals(args[1])) {
                        AssertionResponse ar = ctx.getObjectMapper().readValue(data, AssertionResponse.class);

                        boolean result = ctx.getWebAuthn().assertionFinish(ar);

                        String json = result?"\"good\"":"\"bad\"";
                        out.println(json);
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
