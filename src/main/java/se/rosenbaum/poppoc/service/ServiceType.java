package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import se.rosenbaum.poppoc.core.PopRequest;

import java.io.Serializable;
import java.util.Map;

public interface ServiceType extends Serializable {
    int getServiceId();
    PopRequest getPopRequest();
    String getPaymentUri(Address address);
    Sha256Hash getPayment();
    long addPayment(long satoshis);
    boolean isPaidFor();
    String getPriceTag();

    /**
     * Must throw a RuntimeException if it doesn't find
     * the parameters it needs
     * @param parameters
     */
    public void useParameters(Map<String, String[]> parameters);

    public String getPaymentCallback();
    public String getPopCallback();
}
