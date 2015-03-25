package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Transaction;

public class Pop extends Transaction {


    public Pop(NetworkParameters params) {
        super(params);
    }

    public Pop(NetworkParameters params, byte[] payloadBytes) throws ProtocolException {
        super(params, payloadBytes);
    }
}
