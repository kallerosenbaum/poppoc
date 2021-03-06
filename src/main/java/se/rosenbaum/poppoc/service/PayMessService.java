package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import se.rosenbaum.poppoc.core.PopRequestWithServiceType;
import se.rosenbaum.poppoc.paymess.Message;
import se.rosenbaum.poppoc.paymess.MessageSpace;

import java.util.Map;

public class PayMessService extends StandardService {
    public static final long PRICE_SATOSHIS = 100000L;
    private MessageSpace messageSpace;

    public int getServiceId() {
        return 2;
    }

    public PopRequestWithServiceType getPopRequest() {
        return createPopRequest(null, null, "PayMess " + messageSpace.getId());
    }

    public String getPaymentUri(Address address) {
        return "bitcoin:" + address.toString() + "?label=PayMess%20" + messageSpace.getId() + "&amount=" + PRICE_SATOSHIS*Math.pow(10, -8);
    }

    public boolean isPaidFor() {
        return paidSatoshis >= PRICE_SATOSHIS;
    }

    public String getPriceTag() {
        return Coin.valueOf(PRICE_SATOSHIS).toFriendlyString();
    }

    public void useParameters(Map<String, String[]> parameters) {
        String messageSpaceIdString = getParameter("messageSpaceId", parameters);
        Long messageSpaceId;
        try {
            messageSpaceId = Long.parseLong(messageSpaceIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse long from " + messageSpaceIdString);
        }
        String messageSpaceText = getParameter("messageSpaceText", parameters);
        if (messageSpaceText == null || messageSpaceText.isEmpty()) {
            throw new IllegalArgumentException("messageSpaceText must not be null or empty");
        }
        if (messageSpaceText.length() > 140) {
            throw new IllegalArgumentException("messageSpaceText must be less than 140 characters");
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

    public MessageSpace getMessageSpace() {
        return messageSpace;
    }

    public boolean isSameServiceType(ServiceType serviceType) {
        if (serviceType.getServiceId() != getServiceId()) {
            return false;
        }
        PayMessService other = (PayMessService)serviceType;
        return messageSpace.getId().equals(other.messageSpace.getId());
    }


    public void update(ServiceType nakedServiceType) {
        if (!(nakedServiceType instanceof PayMessService)) {
            throw new IllegalArgumentException("Invalid type of ServiceType:" + nakedServiceType.getClass().toString());
        }
        messageSpace = ((PayMessService)nakedServiceType).messageSpace;
    }

    public long getServiceTime() {
        return 7L*24L*60L*60L*1000L; // 1 week
    }
}
