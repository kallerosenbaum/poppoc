package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import se.rosenbaum.poppoc.core.PopRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * User: kalle
 * Date: 4/29/15 5:43 PM
 */
public class SimpleService extends StandardService {
    public int getServiceId() {
        return 1;
    }

    public PopRequest getPopRequest() {
        return createPopRequest(null, null, "service1");
    }

    public String getPaymentUri(Address address) {
        try {
            return "bitcoin:" + URLEncoder.encode(address.toString(), "UTF-8") + "?label=service" + getServiceId();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPaidFor() {
        return paidSatoshis > 0; // Accept any payment.
    }

    public String getPriceTag() {
        return "any amount";
    }

    public void useParameters(Map<String, String[]> parameters) {

    }

    public String getPaymentCallback() {
        return null;
    }

    public String getPopCallback() {
        return "Service?serviceId=" + getServiceId();
    }
}
