package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

/**
 * User: kalle
 * Date: 2015-05-17 20:26
 */
public interface Wallet {
    Address getNewReceiveAddress();

    Transaction getTransaction(Sha256Hash txid);
}
