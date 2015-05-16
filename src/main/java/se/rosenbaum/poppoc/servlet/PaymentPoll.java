package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Sha256Hash;
import se.rosenbaum.poppoc.core.ClientException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/PaymentPoll/*", name = "PaymentPoll")
public class PaymentPoll extends BasicServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String addressString = request.getParameter(JspConst.RECEIVE_ADDRESS.val());
        if (addressString == null) {
            throw new ClientException("No address in poll!");
        }
        Address address;
        try {
            address = new Address(getConfig().getNetworkParameters(), addressString);
        } catch (AddressFormatException e) {
            throw new ClientException("Address " + addressString + " not parsable", e);
        }

        Sha256Hash txid = getStorage().getPaymentTransaction(address);
        response.setContentType("text/plain; charset=US-ASCII");
        if (txid != null) {
            response.getWriter().write(JspConst.PAYMENT_RECEIVED.val());
        } else {
            response.getWriter().write(JspConst.PAYMENT_NOT_RECEIVED_YET.val());
        }
    }

}
