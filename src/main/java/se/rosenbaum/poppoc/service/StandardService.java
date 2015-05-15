package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import se.rosenbaum.poppoc.core.PopRequest;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;


public abstract class StandardService implements ServiceType {
    long paidSatoshis = 0;
    Date paidDate = null;
    private Address paymentAddress;

    public Sha256Hash getPayment() {
        return null;
    }

    public long addPayment(long satoshis) {
        boolean alreadyPaidFor = isPaidFor();
        paidSatoshis += satoshis;
        if (isPaidFor() && !alreadyPaidFor) {
            paidDate = new Date();
        }
        return paidSatoshis;
    }

    protected PopRequest createPopRequest(String txid, Long amount, String label) {
        Random random = new SecureRandom();

        byte[] nonceBytes = new byte[6];
        random.nextBytes(nonceBytes);
        byte[] longBytes  = new byte[8];
        System.arraycopy(nonceBytes, 0, longBytes, 2, 6);
        long nonce = ByteBuffer.wrap(longBytes).getLong();

        PopRequest popRequest = new PopRequest(nonce, this);
        popRequest.setAmount(amount);
        popRequest.setLabel(label);
        popRequest.setTxid(txid);
        return popRequest;
    }

    public Date paidDate() {
        return paidDate;
    }

    public Address setPaymentAddress(Address address) {
        return this.paymentAddress = address;
    }

    public Address getPaymentAddress() {
        return this.paymentAddress;
    }

    public boolean isSameServiceType(ServiceType serviceType) {
        return serviceType.getServiceId() == getServiceId();
    }

    public void update(ServiceType nakedServiceType) {

    }

    public long getServiceTime() {
        return 24L*60L*60L*1000L; // 24h
    }
}
