package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import se.rosenbaum.poppoc.core.PopRequestWithServiceType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public interface ServiceType extends Serializable {
    int getServiceId();
    PopRequestWithServiceType getPopRequest();
    Address setPaymentAddress(Address address);
    Address getPaymentAddress();
    String getPaymentUri(Address address);
    Sha256Hash getPayment();
    long addPayment(long satoshis);
    boolean isPaidFor();
    Date paidDate();
    String getPriceTag();

    /**
     * Must throw a RuntimeException if it doesn't find
     * the parameters it needs
     * @param parameters
     */
    public void useParameters(Map<String, String[]> parameters);

    public String getPaymentCallback();
    public String getPopCallback();

    public boolean isSameServiceType(ServiceType serviceType);

    public void update(ServiceType nakedServiceType);

    public long getServiceTime();
}
