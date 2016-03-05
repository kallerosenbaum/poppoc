package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitcoinRPCTransactionStoreTestManual {
    BitcoinRPCTransactionStore sut;

    @Before
    public void setup() {
        String url = "http://localhost:18332/";
        String rpcuser = "user";
        String rpcpassword = "password";

        sut = new BitcoinRPCTransactionStore(NetworkParameters.fromID(NetworkParameters.ID_MAINNET), url, rpcuser, rpcpassword);
    }

    @Test
    public void testDownloadTransaction() throws Exception {
        String hash = "3fe5373efdada483b5fa7bdf2249d8274f1b8c04ab5a98bce3edfb732d8e2f86";
        Transaction transaction = sut.getTransaction(new Sha256Hash(hash));
        assertEquals(hash, transaction.getHashAsString());
    }
}