package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Coin;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/GeneratePopRequest/*", name = "GeneratePopRequest")
public class GeneratePopRequest extends PopRequestServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String txid = getValue(request, "txid");
        String amountString = getValue(request, "amount");
        Coin coin = amountString == null ? null : Coin.parseCoin(amountString);
        String text = getValue(request, "text");
        createPopRequest(request, response, 1000, txid, coin.getValue(), text);
    }

    private String getValue(HttpServletRequest request, String parameter) {
        String value = request.getParameter(parameter);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if ("".equals(value)) {
            return null;
        }
        return value;
    }
}
