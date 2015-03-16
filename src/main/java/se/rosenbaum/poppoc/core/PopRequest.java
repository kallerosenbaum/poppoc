package se.rosenbaum.poppoc.core;

public class PopRequest {
    Long nonce;
    String txid;
    Long amount;
    String text;

    public PopRequest(Long nonce) {
        if (nonce == null) {
            throw new IllegalArgumentException("Nonce must not be null");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce must not be negative");
        }
        this.nonce = nonce;
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
}
