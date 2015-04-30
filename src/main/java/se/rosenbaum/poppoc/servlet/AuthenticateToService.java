package se.rosenbaum.poppoc.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.service.ServiceType;
import se.rosenbaum.poppoc.service.ServiceTypeFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/AuthenticateToService/*", name = "AuthenticateToService")
public class AuthenticateToService extends PopRequestServlet {
    private Logger logger = LoggerFactory.getLogger(AuthenticateToService.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int serviceId = getServiceId(request);
        ServiceType serviceType = new ServiceTypeFactory().createServiceType(serviceId);
        createPopRequest(request, response, serviceType);
    }

}
