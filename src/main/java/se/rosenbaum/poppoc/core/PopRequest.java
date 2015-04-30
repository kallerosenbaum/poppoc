package se.rosenbaum.poppoc.core;

import se.rosenbaum.poppoc.service.ServiceType;

import java.io.Serializable;

public class PopRequest implements Serializable {

    Long nonce;
    String txid;
    Long amount;
    String text;

    ServiceType serviceType;

    public PopRequest(Long nonce, ServiceType serviceType) {
        if (nonce == null) {
            throw new IllegalArgumentException("Nonce must not be null");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce must not be negative");
        }
        if (serviceType == null) {
            throw new IllegalArgumentException("ServiceType must not be null");
        }
        this.nonce = nonce;
        this.serviceType = serviceType;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getNonce() {
        return nonce;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }
}
