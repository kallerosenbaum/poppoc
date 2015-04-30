package se.rosenbaum.poppoc.service;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import se.rosenbaum.poppoc.core.PopRequest;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

/**
 * User: kalle
 * Date: 4/29/15 5:40 PM
 */
public abstract class StandardService implements ServiceType {
    long paidSatoshis = 0;

    public Sha256Hash getPayment() {
        return null;
    }

    public long addPayment(long satoshis) {
        paidSatoshis += satoshis;
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
}
