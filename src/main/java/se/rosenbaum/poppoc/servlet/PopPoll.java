package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.Config;
import se.rosenbaum.poppoc.core.Storage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/PopPoll/*", name = "PopPoll")
public class PopPoll extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(PopPoll.class);


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestIdString = request.getParameter("requestId");
        if (requestIdString == null) {
            logger.error("No requestId");
            throw new RuntimeException("No requestId");
        }
        int requestId;
        try {
            requestId = Integer.parseInt(requestIdString);
        } catch (NumberFormatException e) {
            logger.error("Malformed requestId {}", requestIdString);
            throw new RuntimeException("Malformed requestId: " + requestIdString, e);
        }

        Storage storage = (Storage)getServletContext().getAttribute("storage");
        storage.getPopRequest(requestId);

        String addressString = request.getParameter("address");
        if (addressString == null) {
            throw new RuntimeException("No address in poll!");
        }
        Address address;
        try {
            address = new Address(Config.NETWORK_PARAMETERS, addressString);
        } catch (AddressFormatException e) {
            throw new RuntimeException("Address " + addressString + " not parsable", e);
        }
        String serviceId = storage.getServiceIdForPayment(address);
        if (serviceId != null) {
            response.getWriter().write("VALID POP RECEIVED");
        } else {
            response.getWriter().write("POP NOT RECEIVED YET");
        }
    }
}
