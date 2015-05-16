package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import se.rosenbaum.poppoc.service.ServiceType;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class PopRequest implements Serializable {

    Long nonce;
    String txid;
    Long amount;
    String label;
    String message;

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

    public Long getNonce() {
        return nonce;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String createPopRequestUri(String popUrl) throws UnsupportedEncodingException {
        String popRequestUri = "btcpop:?p=" + PopEncodeDecode.popURIEncode(popUrl);

        byte[] nonceBytes = new byte[6];
        System.arraycopy(ByteBuffer.allocate(8).putLong(getNonce()).array(), 2, nonceBytes, 0, 6);
        popRequestUri += "&n=" + Base58.encode(nonceBytes);

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
