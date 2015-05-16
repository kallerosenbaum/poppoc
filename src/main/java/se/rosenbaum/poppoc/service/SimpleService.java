package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import se.rosenbaum.poppoc.core.PopRequest;

import java.util.Map;

public class SimpleService extends StandardService {
    public int getServiceId() {
        return 1;
    }

    public PopRequest getPopRequest() {
        return createPopRequest(null, null, "service1");
    }

    public String getPaymentUri(Address address) {
        return "bitcoin:" + address.toString() + "?label=service" + getServiceId();
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
