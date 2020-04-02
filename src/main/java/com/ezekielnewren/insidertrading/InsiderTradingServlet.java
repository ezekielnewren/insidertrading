package com.ezekielnewren.insidertrading;

import com.ezekielnewren.Build;
import com.ezekielnewren.insidertrading.data.AssertionRequestWrapper;
import com.ezekielnewren.insidertrading.data.AssertionResponse;
import com.ezekielnewren.insidertrading.data.RegistrationRequest;
import com.ezekielnewren.insidertrading.data.RegistrationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.webauthn.RegistrationResult;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The {@code InsiderTradingServlet} class handles communication with server when user logs in or registers.
 * @see javax.servlet.http
 * */
public class InsiderTradingServlet extends HttpServlet {

    /**
     * Object to hold data serialization.
     * @see SessionManager
     */
    SessionManager ctx;

    /**
     * Constructor assigns data to ctx object and logs data on success.
     * @throws ServletException throws, never caught.
     */
    @Override
    public void init() throws ServletException {
        if (Util.DEBUG) {
            getServletContext().log("DEBUG MODE");
        }

        ctx = new SessionManager(
                JacksonHelper.newObjectMapper(),
                "mongodb://localhost",
                null,
                Build.get("fqdn"),
                Build.get("title")
        );

        getServletContext().log(InsiderTradingServlet.class.getSimpleName()+" loaded");
    }


    /**
     * Displays a html page with 'only POST allowed' on {@code POST} request.
     * @param req contains the client request information.
     * @param response contains all server response information.
     * @throws IOException if http request is {@code GET}.
     * @see javax.servlet.http.HttpServletRequest
     * @see javax.servlet.http.HttpServletResponse
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>only POST allowed</body></html>");
    }


    /**
     * Displays correct webpage on {@code POST}, handles login and registration by error checking and deserializing values.
     * @param request contains the client request information.
     * @param response contains all server response information.
     * @throws IOException  {@code POST} fails.
     * @see javax.servlet.http.HttpServletRequest
     * @see javax.servlet.http.HttpServletResponse
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
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
                    response.sendError(400, "bad arguments must be "+Build.get("urlprefix")+"<action>/<state> e.g. "+Build.get("urlprefix")+"register/start");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            };

            if (args.length == 2) {
                if ("register".equals(args[0])) {
                    if ("start".equals(args[1])) {
                        // decode arguments

                        JsonNode jsonTmp = ctx.getObjectMapper().readTree(data);
                        String username = jsonTmp.get("username").asText();
                        String displayName = jsonTmp.get("displayName").asText();
                        String nickname = jsonTmp.asText(null);
                        boolean requireResidentKey = jsonTmp.get("requireResidentKey").asBoolean();

                        // webauthn registration start
                        RegistrationRequest regRequest = ctx.getWebAuthn().registerStart(request.getSession(), username, displayName, nickname, requireResidentKey);
                        if (regRequest == null) {
                            sendError(response, 400, BankAPIException.Reason.REGISTRATION_USERNAME_NOT_AVAILABLE.toString());
                            return;
                        }

                        // encode registration request and send it to the client
                        String json = ctx.getObjectMapper().writeValueAsString(regRequest);
                        out.println(json);
                    } else if ("finish".equals(args[1])) {
                        // decode arguments
                        RegistrationResponse regResponse = ctx.getObjectMapper().readValue(data, RegistrationResponse.class);

                        // finish webauthn registration
                        Triple<Boolean, RegistrationRequest, RegistrationResult> result = ctx.getWebAuthn().registerFinish(request.getSession(), regResponse);

                        // respond to client
                        if (result.getLeft()) {
                            out.println(ctx.getObjectMapper().writeValueAsString(result.getMiddle().getUsername()));
                        } else {
                            sendError(response, 400, BankAPIException.Reason.REGISTRATION_FAILED.toString());
                        }
                    } else {
                        sendError(response, 400, BankAPIException.Reason.GENERAL_ERROR.toString());
                    }
                } else if ("login".equals(args[0])) {
                    if ("start".equals(args[1])) {

                        JsonNode jsonTmp = ctx.getObjectMapper().readTree(data);
                        String username = jsonTmp.get("username").asText();
                        boolean requireResidentKey = jsonTmp.get("requireResidentKey").asBoolean();

                        String json;
                        if (!ctx.isLoggedIn(request.getSession(), username)) {
                            AssertionRequestWrapper arw = ctx.getWebAuthn().loginStart(username);
                            if (arw == null) {
                                sendError(response, 400, BankAPIException.Reason.LOGIN_NO_SUCH_USERNAME.toString());
                                return;
                            }
                            out.println(ctx.getObjectMapper().writeValueAsString(arw));
                        } else {
                            sendError(response, 400, BankAPIException.Reason.ALREADY_LOGGED_IN.toString());
                        }
                    } else if ("finish".equals(args[1])) {
                        AssertionResponse ar = ctx.getObjectMapper().readValue(data, AssertionResponse.class);

                        String result = ctx.getWebAuthn().loginFinish(request.getSession(), ar);
                        if (result != null) {
                            out.println(ctx.getObjectMapper().writeValueAsString(result));
                        } else {
                            sendError(response, 400, BankAPIException.Reason.LOGIN_FAILED.toString());
                        }
                    } else {
                        sendError(response, 400, BankAPIException.Reason.GENERAL_ERROR.toString());
                    }
                }
            } else if ("api".equals(args[0])) {
                ObjectNode json = ctx.getApi().onRequest(request.getSession(), data);

                if (!json.get("error").isNull()) {
                    sendError(response, 400, json.get("error").asText());
                } else {
                    out.println(json.get("data").toString());
                }
            }
            else {
                sendError(response, 400, BankAPIException.Reason.GENERAL_ERROR.toString());
            }
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException|Error e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void sendError(HttpServletResponse response, int code, String message) throws IOException {
        PrintWriter out = response.getWriter();

        response.setStatus(code);
        out.println(message);
    }

}
