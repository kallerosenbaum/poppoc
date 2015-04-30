package se.rosenbaum.poppoc.servlet;

import se.rosenbaum.poppoc.service.PayMessService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * User: kalle
 * Date: 4/28/15 9:16 PM
 */
@WebServlet(urlPatterns = "/NewMessageSpace/*", name = "NewMessageSpace")
public class UpdateMessageSpace extends PopRequestServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PayMessService serviceType = new PayMessService();
        serviceType.useParameters(request.getParameterMap());

        createPopRequest(request, response, serviceType);
    }
}
