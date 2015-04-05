package se.rosenbaum.poppoc.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/AuthenticateToService/*", name = "AuthenticateToService")
public class AuthenticateToService extends PopRequestServlet {
    private Logger logger = LoggerFactory.getLogger(AuthenticateToService.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String serviceIdString = request.getParameter(JspConst.SERVICE_ID.val());
        if (!isSet(serviceIdString)) {
            logger.error("serviceId is empty or null");
            throw new RuntimeException("ServiceId is null or empty");
        }
        int serviceId = Integer.parseInt(serviceIdString);
        createPopRequest(request, response, serviceId, null, null, "service" + serviceId);
    }
}
