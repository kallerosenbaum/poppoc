package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import se.rosenbaum.poppoc.core.Config;
import se.rosenbaum.poppoc.core.Storage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/PaymentPoll/*", name = "PaymentPoll")
public class PaymentPoll extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

        Storage storage = (Storage)getServletContext().getAttribute("storage");
        String serviceId = storage.getServiceIdForPayment(address);
        if (serviceId != null) {
            response.getWriter().write("PAYMENT RECEIVED");
        } else {
            response.getWriter().write("PAYMENT NOT RECEIVED YET");
        }
    }
}
