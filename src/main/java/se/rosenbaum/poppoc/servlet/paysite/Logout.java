package se.rosenbaum.poppoc.servlet.paysite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.servlet.BasicServlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/Logout/*", name = "Logout")
public class Logout extends BasicServlet {
    Logger logger = LoggerFactory.getLogger(Logout.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int serviceId = getServiceId(request);
        removeServiceFromSession(request.getSession(false), serviceId);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.jsp");
        requestDispatcher.forward(request, response);
    }
}
