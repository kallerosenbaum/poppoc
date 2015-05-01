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

    protected PopRequest createPopRequest(String txid, Long amount, String text) {
        Random random = new SecureRandom();

        byte[] nonceBytes = new byte[8];
        random.nextBytes(nonceBytes);
        nonceBytes[0] = 0;
        nonceBytes[1] = 0;
        nonceBytes[2] = 0;
        long nonce = ByteBuffer.wrap(nonceBytes).getLong();

        PopRequest popRequest = new PopRequest(nonce, this);
        popRequest.setAmount(amount);
        popRequest.setText(text);
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
        return 24*60*60*1000; // 24h
    }
}
