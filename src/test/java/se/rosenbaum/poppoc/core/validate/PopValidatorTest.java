package se.rosenbaum.poppoc.core.validate;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.testing.FakeTxBuilder;
import org.bitcoinj.testing.TestWithWallet;
import org.junit.Before;
import org.junit.Test;
import se.rosenbaum.poppoc.core.InvalidPopException;
import se.rosenbaum.poppoc.core.Pop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bitcoinj.core.Wallet.SendRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PopValidatorTest extends TestWithWallet {
    public static final long MAX_NONCE = (long) (Math.pow(2, 48) - 1);
    PopValidator sut;
    Wallet payerWallet;

    @Before
    public void setup() throws Exception {
        setUp();
        payerWallet = new Wallet(params);
        sut = new PopValidator(new FakeWalletTransactionStore());
    }

    private class FakeWalletTransactionStore implements TransactionStore {
        public Transaction getTransaction(Sha256Hash txid) {

            Transaction transaction = wallet.getTransaction(txid);
            if (transaction != null) {
                return transaction;
            }
            return null;
        }
    }

    @Test(expected = InvalidPopException.class)
    public void testValidateNullPop() throws Exception {
        validatePop(null);
    }

    @Test
    public void testValidateOkPop() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopBadPublicKey() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);
        signPop(pop);
        List<ScriptChunk> chunks = pop.getInputs().get(0).getScriptSig().getChunks();
        chunks.get(0).data[20]++; // Mess up the public key
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopBadSignature() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);
        signPop(pop);
        List<ScriptChunk> chunks = pop.getInputs().get(0).getScriptSig().getChunks();
        chunks.get(1).data[20]++; // Mess up the signature
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopPopOutputNotFirst() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);

        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>(pop.getOutputs());
        pop.clearOutputs();
        pop.addOutput(outputs.get(1)); // Swap places
        pop.addOutput(outputs.get(0));
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopInvalidOrderOfInputs() throws Exception {
        Pop pop = getPop(new int[]{2, 1}, Coin.valueOf(2, 0), 1);
        List<TransactionInput> inputs = new ArrayList<TransactionInput>(pop.getInputs());
        pop.clearInputs();
        pop.addInput(inputs.get(1)); // Swap order of inputs, which is illegal in pop.
        pop.addInput(inputs.get(0));
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopMissingInput() throws Exception {
        Pop pop = getPop(new int[] {2, 1}, Coin.valueOf(2, 0), 1);

        List<TransactionInput> inputs = new ArrayList<TransactionInput>(pop.getInputs());
        pop.clearInputs();
        if (inputs.get(0).getConnectedOutput().getValue().equals(Coin.COIN)) {
            pop.addInput(inputs.get(1)); // Remove the smaller input
        } else {
            pop.addInput(inputs.get(0)); // Remove the smaller input
        }
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopExtraOP_RETURN() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);
        pop.addOutput(Coin.ZERO, new Script(new byte[]{ScriptOpCodes.OP_RETURN}));
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopExtraOP_RETURNinProvenTxANDinPop() throws Exception {
        Pop pop = getPop(2, Coin.COIN, 1, 0);
        pop.addOutput(Coin.ZERO, new Script(new byte[]{ScriptOpCodes.OP_RETURN}));
        signPop(pop);
        validatePop(pop);
    }

    @Test
    public void testValidatePopOP_RETURNwithZeroInProvenTx() throws Exception {
        Pop pop = getPop(2, Coin.COIN, 1, 0);
        signPop(pop);
        validatePop(pop);
    }

    @Test
    public void testValidatePopOP_RETURNwithOneBTCInProvenTx() throws Exception {
        Pop pop = getPop(3, Coin.COIN, 1, -1);
        signPop(pop);
        assertEquals(2, pop.getOutputs().size());
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopWithOP_RETURNwithOneBTCAndFeeAbsentFromPopOutput() throws Exception {
        Pop pop = getPop(4, Coin.valueOf(2, 0), 1, -1);
        pop.getOutput(0).setValue(Coin.COIN); // Only value from OP_RETURN, none from the 2 BTC fee
        signPop(pop);
        assertEquals(2, pop.getOutputs().size());
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopWithoutOP_RETURNwithOneBTCAndFeeAbsentFromPopOutput() throws Exception {
        Pop pop = getPop(4, Coin.valueOf(3, 0), 1);
        pop.getOutput(0).setValue(Coin.ZERO); // Fee is not moved to input 0
        signPop(pop);
        assertEquals(2, pop.getOutputs().size());
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopWithoutOP_RETURNwithOneBTCAndFeeWrongValueInPopOutput() throws Exception {
        Pop pop = getPop(4, Coin.valueOf(3, 0), 1);
        pop.getOutput(0).setValue(Coin.COIN); // Only part of the fee is moved to input 0
        signPop(pop);
        assertEquals(2, pop.getOutputs().size());
        validatePop(pop);
    }

    private Pop getPop(int[] fundingValue, Coin fee, int... outputValues) throws InsufficientMoneyException, IOException {
        List<Transaction> fundingTransaction = createFundingTransaction(fundingValue);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, fee, outputValues);

        return createValidUnsignedPop(fundingTransaction, paymentToProve);
    }

    private Pop getPop(int fundingValue, Coin fee, int... outputValues) throws Exception {
        return getPop(new int[]{fundingValue}, fee, outputValues);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOP_RETURNwithOneBTCInProvenTxWrongPopOutput() throws Exception {
        List<Transaction> fundingTransaction = createFundingTransaction(3);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.COIN, 1, -1);
        assertEquals(2, paymentToProve.getOutputs().size());
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);
        pop.getOutput(0).setValue(Coin.COIN); // The value of the OP_RETURN is not added to the pop output, but
                                              // instead spent as a fee. That is an invalid pop.
        signPop(pop);
        assertEquals(2, pop.getOutputs().size());
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOutputTooLong() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);

        ByteBuffer byteBuffer = ByteBuffer.allocate(42);
        byteBuffer.put((byte) ScriptOpCodes.OP_RETURN);
        byteBuffer.putShort((short) 1); // version 1
        byteBuffer.put(paymentToProve.getHash().getBytes()); // txid
        ByteBuffer nonceBuffer = ByteBuffer.allocate(8);
        nonceBuffer.putLong(19L);
        byteBuffer.put(nonceBuffer.array(), 2, 6); // nonce
        TransactionOutput invalidPopOutput = new TransactionOutput(params, null, Coin.ZERO, byteBuffer.array());
        signPop(pop);
        testInvalidPopOutput(pop, invalidPopOutput);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOutputTooShort() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);

        ByteBuffer byteBuffer = ByteBuffer.allocate(40);
        byteBuffer.put((byte) ScriptOpCodes.OP_RETURN);
        byteBuffer.putShort((short) 1); // version 1
        byteBuffer.put(paymentToProve.getHash().getBytes()); // txid
        ByteBuffer nonceBuffer = ByteBuffer.allocate(8);
        nonceBuffer.putLong(19L);
        byteBuffer.put(nonceBuffer.array(), 2, 5); // nonce
        TransactionOutput invalidPopOutput = new TransactionOutput(params, null, Coin.ZERO, byteBuffer.array());
        signPop(pop);
        testInvalidPopOutput(pop, invalidPopOutput);
    }

    @Test
    public void testValidatePop6ByteNonce() throws Exception {
        testNonce((long)(Math.pow(2, 41)-1), 1, 255, 255, 255, 255, 255);
    }

    @Test
    public void testValidatePop5ByteNonce() throws Exception {
        testNonce((long)(Math.pow(2, 40)-1), 0, 255, 255, 255, 255, 255);
    }

    @Test
    public void testValidatePopMinNonce() throws Exception {
        testNonce(0L, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testValidatePopMaxNonce() throws Exception {
        testNonce(MAX_NONCE, 255, 255, 255, 255, 255, 255);
    }

    private void testNonce(long requestedNonce, int... nonceUnsignedBytes) throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);

        byte[] scriptBytes = pop.getOutput(0).getScriptBytes();
        for (int i = 0; i < 6; i++) {
            scriptBytes[35+i] = (byte)nonceUnsignedBytes[i];
        }

        signPop(pop);
        validatePop(pop, requestedNonce);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersion0() throws Exception {
        testVersion(0, 0);
    }

    @Test
    public void testVersion1() throws Exception {
        testVersion(0, 1);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersion2() throws Exception {
        testVersion(0, 2);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersion256() throws Exception {
        testVersion(1, 0);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersionMax() throws Exception {
        testVersion(255, 255);
    }

    private void testVersion(int... versionUnsignedBytes) throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);

        byte[] scriptBytes = pop.getOutput(0).getScriptBytes();
        scriptBytes[1] = (byte)versionUnsignedBytes[0];
        scriptBytes[2] = (byte)versionUnsignedBytes[1];

        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOutputInvalidTxId() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);

        byte[] bytes = paymentToProve.getHash().getBytes();
        bytes[0]++;
        TransactionOutput invalidPopOutput = createPopOutput(Sha256Hash.create(bytes), 19, 0);

        testInvalidPopOutput(pop, invalidPopOutput);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOutputInvalidNonce() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);
        TransactionOutput invalidPopOutput = createPopOutput(paymentToProve.getHash(), 18, 0);
        testInvalidPopOutput(pop, invalidPopOutput);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOutputInvalidValue() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);
        TransactionOutput invalidPopOutput = createPopOutput(paymentToProve.getHash(), 19, 1);
        testInvalidPopOutput(pop, invalidPopOutput);
    }

    @Test
    public void testValidatePopOutputValid() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);
        TransactionOutput invalidPopOutput = createPopOutput(paymentToProve.getHash(), 19, 0);
        testInvalidPopOutput(pop, invalidPopOutput);
    }

    private void testInvalidPopOutput(Pop pop, TransactionOutput invalidPopOutput) throws IOException, InsufficientMoneyException, InvalidPopException {
        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>(pop.getOutputs());
        pop.clearOutputs();
        pop.addOutput(invalidPopOutput);
        pop.addOutput(outputs.get(1));
        signPop(pop);
        validatePop(pop);
    }

    private void validatePop(Pop pop) throws InvalidPopException {
        validatePop(pop, 19L);
    }

    private void validatePop(Pop pop, long nonce) throws InvalidPopException {
        try {
            sut.validatePop(pop, nonce);
        } catch (InvalidPopException e) {
            System.out.println("Invalid pop (maybe expected): " + e.getMessage());
            throw e;
        }
    }

    private void signPop(Pop pop) {
        payerWallet.signTransaction(SendRequest.forTx(pop));
    }

    private Pop createValidUnsignedPop(List<Transaction> funding, Transaction txToProve) throws IOException, InsufficientMoneyException {

        // Copy the txToProve
        Pop pop = new Pop(params, txToProve.bitcoinSerialize());
        pop.clearOutputs();
        pop.addOutput(createPopOutput(txToProve.getHash(), 19L, 0));
        copyOutputs(txToProve, pop);

        Map<Sha256Hash, Transaction> fundingMap = new HashMap<Sha256Hash, Transaction>();
        for (Transaction transaction : funding) {
            fundingMap.put(transaction.getHash(), transaction);
        }

        for (TransactionInput transactionInput : pop.getInputs()) {
            transactionInput.connect(fundingMap, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);
        }
        return pop;
    }

    private Transaction createPaymentToProve(List<Transaction> funding, Coin fee, int... outputValues) throws InsufficientMoneyException {

        SendRequest request = SendRequest.to(wallet.currentReceiveAddress(), Coin.ZERO);
        request.ensureMinRequiredFee = false;
        request.fee = fee;
        request.shuffleOutputs = false;
        request.tx.clearOutputs();
        for (int outputValue : outputValues) {
            if (outputValue <= 0) {
                // <= 0 indicates that we want an OP_RETURN output with value=-outputValue
                Script script = new Script(new byte[] {ScriptOpCodes.OP_RETURN});
                request.tx.addOutput(Coin.valueOf(-outputValue, 0), script);
                request.tx.getOutput(request.tx.getOutputs().size() - 1).toString();
            } else {
                Address destination = wallet.freshReceiveAddress();
                request.tx.addOutput(Coin.valueOf(outputValue, 0), destination);
            }
        }

        Transaction txToProve = payerWallet.sendCoinsOffline(request);
        wallet.receivePending(txToProve, funding);
        return txToProve;
    }

    private List<Transaction> createFundingTransaction(int... values) throws IOException {
        List<Transaction> fundingTransactions = new ArrayList<Transaction>();
        for (int value : values) {
            Transaction fakeTx = FakeTxBuilder.createFakeTx(params, Coin.valueOf(value, 0), payerWallet.freshReceiveAddress());
            Transaction sentTx = sendMoneyToWallet(payerWallet, fakeTx, AbstractBlockChain.NewBlockType.BEST_CHAIN);
            fundingTransactions.add(sentTx);
        }
        return fundingTransactions;
    }

    private void copyOutputs(Transaction txToProve, Transaction pop) {

        for (TransactionOutput transactionOutput : txToProve.getOutputs()) {
            if (transactionOutput.getScriptBytes()[0] == ScriptOpCodes.OP_RETURN) {
                pop.getOutput(0).setValue(pop.getOutput(0).getValue().add(transactionOutput.getValue()));
            } else {
                pop.addOutput(transactionOutput.duplicateDetached());
            }
        }
        pop.getOutput(0).setValue(pop.getOutput(0).getValue().add(txToProve.getFee()));
    }


    private TransactionOutput createPopOutput(Sha256Hash txidToProve, long nonce, long amount) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(41);
        byteBuffer.put((byte)ScriptOpCodes.OP_RETURN);
        byteBuffer.putShort((short)1); // version 1
        byteBuffer.put(txidToProve.getBytes()); // txid
        ByteBuffer nonceBuffer = ByteBuffer.allocate(8);
        nonceBuffer.putLong(nonce);
        byteBuffer.put(nonceBuffer.array(), 2, 6); // nonce
        TransactionOutput output = new TransactionOutput(params, null, Coin.valueOf(amount), byteBuffer.array());
        return output;
    }

}