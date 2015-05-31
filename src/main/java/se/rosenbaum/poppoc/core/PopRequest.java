package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import se.rosenbaum.poppoc.service.ServiceType;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class PopRequest implements Serializable {

    byte[] nonce;
    String txid;
    Long amount;
    String label;
    String message;

    ServiceType serviceType;

    public PopRequest(byte[] nonce, ServiceType serviceType) {
        if (nonce == null) {
            throw new IllegalArgumentException("Nonce must not be null");
        }
        if (nonce.length != 6) {
            throw new IllegalArgumentException("Nonce must be of length 6");
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String createPopRequestUri(String popUrl) throws UnsupportedEncodingException {
        String popRequestUri = "btcpop:?p=" + PopEncodeDecode.popURIEncode(popUrl);

        popRequestUri += "&n=" + Base58.encode(nonce);

        if (isSet(getTxid())) {
            popRequestUri += "&txid=" + Base58.encode(getTxid().getBytes());
        }

        if (getAmount() != null) {
            Coin amount = Coin.valueOf(getAmount());
            popRequestUri += "&amount=" + amount.toPlainString();
        }

        if (isSet(getLabel())) {
            popRequestUri += "&label=" + PopEncodeDecode.popURIEncode(getLabel());
        }
        return popRequestUri;
    }

    protected boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
