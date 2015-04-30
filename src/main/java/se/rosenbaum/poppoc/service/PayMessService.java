package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import se.rosenbaum.poppoc.core.PopRequest;
import se.rosenbaum.poppoc.paymess.Message;
import se.rosenbaum.poppoc.paymess.MessageSpace;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class PayMessService extends StandardService {
    public static final long PRICE_SATOSHIS = 500000L;
    private MessageSpace messageSpace;

    public int getServiceId() {
        return 2;
    }

    public PopRequest getPopRequest() {
        return createPopRequest(null, null, "PayMess " + messageSpace.getId());
    }

    public String getPaymentUri(Address address) {
        try {
            return "bitcoin:" + URLEncoder.encode(address.toString(), "UTF-8") + "?label=PayMess" + messageSpace.getId();
            //"bitcoin:" + urlEncode(address.toString());

            //paymentUri = appendParam(request, "amount", paymentUri);
            //paymentUri = appendParam(request, "label", paymentUri);
            //paymentUri = appendParam(request, "message", paymentUri);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPaidFor() {
        return paidSatoshis >= PRICE_SATOSHIS;
    }

    public String getPriceTag() {
        return Coin.valueOf(PRICE_SATOSHIS).toFriendlyString();
    }

    public void useParameters(Map<String, String[]> parameters) {
        String messageSpaceIdString = getParameter("messageSpaceid", parameters);
        Long messageSpaceId;
        try {
            messageSpaceId = Long.parseLong(messageSpaceIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse long from " + messageSpaceIdString);
        }
        String messageSpaceText = getParameter("messageSpacetext", parameters);
        if (messageSpaceText == null || messageSpaceText.isEmpty()) {
            throw new IllegalArgumentException("messageSpaceText must not be null or empty");
        }
        Message message = new Message(messageSpaceText);
        messageSpace = new MessageSpace(messageSpaceId, message);
    }

    private String getParameter(String parameter, Map<String, String[]> parameters) {
        String[] value = parameters.get(parameter);
        if (value == null || value.length != 1) {
            throw new IllegalArgumentException("Bad " + parameter + ". Was " + (value == null ? "null" : value.length));
        }
        return value[0];
    }

    public String getPaymentCallback() {
        return "PayMess";
    }

    public String getPopCallback() {
        return "PayMess";
    }
}
