package se.rosenbaum.poppoc.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = "/Service/*", name = "Service")
public class Service extends BasicServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String serviceIdString = request.getParameter(JspConst.SERVICE_ID.val());
        if (serviceIdString == null) {
            throw new RuntimeException("No service id requested");
        }
        int serviceId = Integer.parseInt(serviceIdString);
        HttpSession session = request.getSession(false);
        if (session == null || !isAuthorized(session, serviceId)) {
            redirectToAuthentication(request, response, serviceId);
            return;
        }
        request.setAttribute(JspConst.SERVICE_ID.val(), serviceId);

        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/WEB-INF/service.jsp");
        requestDispatcher.forward(request, response);
    }

    private void redirectToAuthentication(HttpServletRequest request, HttpServletResponse response, int serviceId) throws IOException {
        String authenticateUrl = request.getContextPath() + "/AuthenticateToService?" + JspConst.SERVICE_ID.val() + "=" + serviceId;
        response.sendRedirect(authenticateUrl);
    }
}
