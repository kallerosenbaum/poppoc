package se.rosenbaum.poppoc.core.validate;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

/**
 * User: kalle
 * Date: 2015-05-17 20:09
 */
public interface TransactionStore {
    Transaction getTransaction(Sha256Hash txid);
}
