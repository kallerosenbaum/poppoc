package se.rosenbaum.poppoc.core.validate;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
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

public class PopValidatorTest extends TestWithWallet {
    public static final byte[] MAX_NONCE = bLength(6, 0xFF);
    public static final int LOCK_TIME = 499999999;
    PopValidator sut;
    Wallet payerWallet;

    private static byte[] bLength(int times, int value) {
        byte[] bytes = new byte[times];
        for (int i = 0; i < times; i++) {
            bytes[i] = (byte)value;
        }
        return bytes;

    }

    private static byte[] b(int... byteValues) {
        byte[] bytes = new byte[byteValues.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)byteValues[i];
        }
        return bytes;
    }

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
    public void testValidatePopTooManyOutputs() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);

        // Add another output.
        pop.addOutput(new TransactionOutput(params, pop, Coin.CENT, wallet.currentReceiveAddress()));
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopPositiveOutputValue() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);
        pop.getOutput(0).setValue(Coin.CENT);
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopNegativeOutputValue() throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);
        pop.getOutput(0).setValue(Coin.CENT.negate());
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
    public void testValidatePopLockTime0() throws Exception {
        Pop pop = getPop(2, Coin.COIN, 1, 0);
        pop.setLockTime(0);
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopLockTime499999998() throws Exception {
        Pop pop = getPop(2, Coin.COIN, 1, 0);
        pop.setLockTime(LOCK_TIME - 1);
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOneSeqNr1() throws Exception {
        Pop pop = getPop(new int[] {2, 3}, Coin.COIN, 4, 0);
        pop.getInput(1).setSequenceNumber(1L);
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOneSeqNrfeffffff() throws Exception {
        Pop pop = getPop(new int[] {2, 3}, Coin.COIN, 4, 0);
        pop.getInput(0).setSequenceNumber(Utils.readUint32(b(254, 255, 255, 255), 0));
        signPop(pop);
        validatePop(pop);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOneSeqNrffffffff() throws Exception {
        Pop pop = getPop(new int[] {2, 3}, Coin.COIN, 4, 0);
        pop.getInput(0).setSequenceNumber(Utils.readUint32(bLength(4, 255), 0));
        signPop(pop);
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

    @Test(expected = InvalidPopException.class)
    public void testValidatePopMismatchingNonce() throws Exception {
        testNonce(b(1, 2, 4, 4, 5, 6), b(1, 2, 3, 4, 5, 6));
    }

    @Test
    public void testValidatePop3ByteNonce() throws Exception {
        testNonce(b(0, 0, 0, 255, 255, 255), b(0, 0, 0, 255, 255, 255));
    }

    @Test
    public void testValidatePopMinNonce() throws Exception {
        testNonce(bLength(6, 0), bLength(6, 0));
    }

    @Test
    public void testValidatePopMaxNonce() throws Exception {
        testNonce(MAX_NONCE, MAX_NONCE);
    }

    private void testNonce(byte[] requestedNonce, byte[] nonce) throws Exception {
        Pop pop = getPop(1, Coin.ZERO, 1);

        byte[] scriptBytes = pop.getOutput(0).getScriptBytes();
        System.arraycopy(nonce, 0, scriptBytes, 35, 6);

        signPop(pop);
        validatePop(pop, requestedNonce);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersion0() throws Exception {
        testVersion(0, 0);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersion01() throws Exception {
        testVersion(0, 1);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersion02() throws Exception {
        testVersion(0, 2);
    }

    @Test
    public void testVersion10() throws Exception {
        testVersion(1, 0);
    }

    @Test(expected = InvalidPopException.class)
    public void testVersion20() throws Exception {
        testVersion(2, 0);
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
        TransactionOutput invalidPopOutput = createPopOutput(Sha256Hash.create(bytes), 19);

        testInvalidPopOutput(pop, invalidPopOutput);
    }

    @Test(expected = InvalidPopException.class)
    public void testValidatePopOutputInvalidNonce() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);
        TransactionOutput invalidPopOutput = createPopOutput(paymentToProve.getHash(), 18);
        testInvalidPopOutput(pop, invalidPopOutput);
    }

    @Test
    public void testValidatePopOutputValid() throws IOException, InsufficientMoneyException, InvalidPopException {
        List<Transaction> fundingTransaction = createFundingTransaction(1);
        Transaction paymentToProve = createPaymentToProve(fundingTransaction, Coin.ZERO, 1);
        Pop pop = createValidUnsignedPop(fundingTransaction, paymentToProve);
        TransactionOutput invalidPopOutput = createPopOutput(paymentToProve.getHash(), 19);
        testInvalidPopOutput(pop, invalidPopOutput);
    }

    private void testInvalidPopOutput(Pop pop, TransactionOutput invalidPopOutput) throws IOException, InsufficientMoneyException, InvalidPopException {
        pop.clearOutputs();
        pop.addOutput(invalidPopOutput);
        signPop(pop);
        validatePop(pop);
    }

    private void validatePop(Pop pop) throws InvalidPopException {
        validatePop(pop, new byte[] {0, 0, 0, 0, 0, 19});
    }

    private void validatePop(Pop pop, byte[] nonce) throws InvalidPopException {
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
        pop.addOutput(createPopOutput(txToProve.getHash(), 19L));

        Map<Sha256Hash, Transaction> fundingMap = new HashMap<Sha256Hash, Transaction>();
        for (Transaction transaction : funding) {
            fundingMap.put(transaction.getHash(), transaction);
        }

        for (TransactionInput transactionInput : pop.getInputs()) {
            transactionInput.connect(fundingMap, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);
            transactionInput.setSequenceNumber(0L);
        }

        pop.setLockTime(LOCK_TIME);
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

    private TransactionOutput createPopOutput(Sha256Hash txidToProve, long nonce) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(41);
        byteBuffer.put((byte)ScriptOpCodes.OP_RETURN);

        // version 0x01 0x00 (1 little endian)
        byteBuffer.put((byte)1);
        byteBuffer.put((byte)0);

        byteBuffer.put(txidToProve.getBytes()); // txid

        ByteBuffer nonceBuffer = ByteBuffer.allocate(8);
        nonceBuffer.putLong(nonce);
        byteBuffer.put(nonceBuffer.array(), 2, 6); // nonce

        TransactionOutput output = new TransactionOutput(params, null, Coin.ZERO, byteBuffer.array());
        return output;
    }

}