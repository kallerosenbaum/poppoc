package se.rosenbaum.poppoc.core;

import java.io.Serializable;
import java.net.URLEncoder;

public class PopRequest implements Serializable {

    Long nonce;
    String txid;
    Long amount;
    String text;

    int serviceId;

    public PopRequest(Long nonce, int serviceId) {
        if (nonce == null) {
            throw new IllegalArgumentException("Nonce must not be null");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce must not be negative");
        }
        if (serviceId < 1) {
            throw new IllegalArgumentException("Service id must be >0, got " + serviceId);
        }
        this.nonce = nonce;
        this.serviceId = serviceId;
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

    public int getServiceId() {
        return serviceId;
    }
}
