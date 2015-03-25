package se.rosenbaum.poppoc.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/UseService/*", name = "UseService")
public class UseService extends PopRequestServlet {
    private Logger logger = LoggerFactory.getLogger(UseService.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String serviceId = request.getParameter("serviceId");
        if (!isSet(serviceId)) {
            logger.error("serviceId is empty or null");
            throw new RuntimeException("ServiceId is null or empty");
        }
        createPopRequest(request, response, null, null, "service" + serviceId);
    }
}
