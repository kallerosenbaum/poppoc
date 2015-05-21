package se.rosenbaum.poppoc.core.validate;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.InvalidPopException;
import se.rosenbaum.poppoc.core.Pop;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class PopValidator {
    Logger logger = LoggerFactory.getLogger(PopValidator.class);
    TransactionStore transactionStore;

    public PopValidator(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
    }

    /**
     * This will check the PoP according to the
     * <a href="https://github.com/kallerosenbaum/poppoc/wiki/Proof-of-Payment">specification</a>
     */
    public Transaction validatePop(Pop pop, Long nonce) throws InvalidPopException {
        // 1 Basic checks
        if (pop == null) {
            throw new InvalidPopException("Pop is null");
        }
        try {
            pop.verify();
        } catch (VerifyError e) {
            throw new InvalidPopException("Basic verification failed.", e);
        }

        // 2 Check OP_RETURN output
        byte[] data = checkOutput(pop);

        byte[] txidBytes = new byte[32];
        System.arraycopy(data, 3, txidBytes, 0, 32);
        Sha256Hash txid = new Sha256Hash(txidBytes);
        Transaction provenTransaction = getTransaction(txid);

        // 3 Check other outputs
        Coin opReturnOutputValue = checkOutputs(pop, provenTransaction);

        // 4 Check nonce
        checkNonce(data, nonce);

        // 5 Check inputs
        // 6 Check signatures
        checkInputsAndSignatures(pop, provenTransaction, opReturnOutputValue);

        // No exceptions, means PoP valid.
        return provenTransaction;
    }

    private Transaction getTransaction(Sha256Hash txid) throws InvalidPopException {
        Transaction localTransaction = transactionStore.getTransaction(txid);
        if (localTransaction == null) {
            throw new InvalidPopException("Unknown transaction");
        }
        return localTransaction;
    }

    private Coin checkOutputs(Pop pop, Transaction provenTransaction) throws InvalidPopException {
        List<TransactionOutput> popOutputs = pop.getOutputs();
        List<TransactionOutput> provenTxOutputs = provenTransaction.getOutputs();
        Coin opReturnPopOutputValue = Coin.ZERO;
        int opReturnCount = 0;
        int popOutputIndex = 1;
        for (TransactionOutput provenTxOutput : provenTxOutputs) {
            byte[] scriptBytes = provenTxOutput.getScriptBytes();
            if (scriptBytes != null && scriptBytes.length > 0 && scriptBytes[0] == ScriptOpCodes.OP_RETURN) {
                opReturnPopOutputValue = opReturnPopOutputValue.add(provenTxOutput.getValue());
                opReturnCount++;
                continue; // OP_RETURN outputs from proven tx should not appear in the PoP.
            }
            if (popOutputIndex < popOutputs.size()) {
                TransactionOutput popOutput = popOutputs.get(popOutputIndex);
                popOutputIndex++;
                if (!popOutput.getValue().equals(provenTxOutput.getValue())) {
                    throw new InvalidPopException("Mismatching value of output " + (popOutputIndex - 1) +
                            ". Expected " + provenTxOutput.getValue() + ", got " + popOutput.getValue());
                }
                if (!Arrays.equals(popOutput.getScriptBytes(), provenTxOutput.getScriptBytes())) {
                    throw new InvalidPopException("Mismatching script of output " + (popOutputIndex - 1));
                }
            }
        }

        int expectedOutputCount = provenTxOutputs.size() + 1 - opReturnCount;
        if (popOutputs.size() != expectedOutputCount) {
            throw new InvalidPopException("Unexpected number of outputs " + popOutputs.size() + ", expected " + expectedOutputCount);
        }
        return opReturnPopOutputValue;
    }

    private void checkNonce(byte[] data, Long popRequestNonce) throws InvalidPopException {
        byte[] nonceBytes = new byte[8];
        System.arraycopy(data, 36, nonceBytes, 3, 5);
        long nonce = ByteBuffer.wrap(nonceBytes).getLong();

        if (nonce != popRequestNonce) {
            throw new InvalidPopException("Wrong nonce");
        }
    }

    private void checkInputsAndSignatures(Pop pop, Transaction provenTransaction, Coin opReturnOutputValue) throws InvalidPopException {
        List<TransactionInput> popInputs = pop.getInputs();
        List<TransactionInput> blockchainTxInputs = provenTransaction.getInputs();
        if (popInputs.size() != blockchainTxInputs.size()) {
            throw new InvalidPopException("Wrong number of inputs");
        }

        Coin inputValue = Coin.ZERO;
        for (int i = 0; i < blockchainTxInputs.size(); i++) {
            // Here I check that the inputs of the pop are in the same order
            // as in the payment transaction.
            TransactionInput popInput = popInputs.get(i);
            TransactionInput bcInput = blockchainTxInputs.get(i);
            if (!popInput.getOutpoint().equals(bcInput.getOutpoint())) {
                throw new InvalidPopException("Mismatching inputs");
            }
            // Check signature
            if (bcInput.getConnectedOutput() == null || popInput.getConnectedOutput() == null) {
                // connect the input to the right transaction:
                Sha256Hash hash = bcInput.getOutpoint().getHash();
                Transaction inputTx = transactionStore.getTransaction(hash);

                if (inputTx == null) {
                    String message = "Could not find input tx: " + hash;
                    logger.debug(message);
                    throw new InvalidPopException(message);
                }
                bcInput.connect(inputTx, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);
                popInput.connect(inputTx, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);
            }
            inputValue = inputValue.add(popInput.getConnectedOutput().getValue());
            try {
                popInput.verify();
            } catch (VerificationException e) {
                logger.debug("Failed to verify input", e);
                throw new InvalidPopException("Signature verification failed", e);
            }
        }

        Coin expectedPopOutputValue = opReturnOutputValue.add(provenTransaction.getFee());
        Coin popOutputValue = pop.getOutput(0).getValue();
        if (!popOutputValue.equals(expectedPopOutputValue)) {
            throw new InvalidPopException("Unexpected value of PoP output. Expected " + expectedPopOutputValue +
                    ". Got " + popOutputValue);
        }
    }

    private byte[] checkOutput(Pop pop) throws InvalidPopException {
        List<TransactionOutput> outputs = pop.getOutputs();
        if (outputs == null || outputs.size() < 2) {
            throw new InvalidPopException("Wrong number of outputs. Expected at least 2.");
        }
        TransactionOutput output = outputs.get(0);

        byte[] scriptBytes = output.getScriptBytes();
        if (scriptBytes == null || scriptBytes.length != 41) {
            throw new InvalidPopException("Invalid script length. Expected 41");
        }
        if (scriptBytes[0] != ScriptOpCodes.OP_RETURN) {
            throw new InvalidPopException("Wrong opcode: " + scriptBytes[0]);
        }

        short version = ByteBuffer.wrap(scriptBytes, 1, 2).getShort();
        if (version != 1) {
            throw new InvalidPopException("Wrong version: " + version + " expected 1");
        }

        return scriptBytes;
    }


}
