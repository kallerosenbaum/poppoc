package se.rosenbaum.poppoc.servlet.paymess;

import se.rosenbaum.poppoc.service.PayMessService;
import se.rosenbaum.poppoc.servlet.PopRequestServlet;

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
@WebServlet(urlPatterns = "/UpdateMessageSpace/*", name = "UpdateMessageSpace")
public class UpdateMessageSpace extends PopRequestServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PayMessService nakedServiceType = new PayMessService();
        nakedServiceType.useParameters(request.getParameterMap());

        createPopRequest(request, response, nakedServiceType);
    }
}
