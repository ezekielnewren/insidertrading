package com.ezekielnewren.webauthn;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;

public class AuthServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    public void service(ServletRequest request, ServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.getWriter().write("<html><body>servlet example</body></html>");
    }

}
