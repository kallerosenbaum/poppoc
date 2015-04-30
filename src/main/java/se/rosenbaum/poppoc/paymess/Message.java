package se.rosenbaum.poppoc.paymess;

/**
 * User: kalle
 * Date: 4/28/15 7:49 PM
 */
public class Message {
    String message;

    public Message(String message) {
        if (message == null) {
            throw new NullPointerException();
        }
        if (message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message must not be null");
        }
        this.message = message;
    }

    public String getHtmlSafeMessage() {
        return message;
    }
}
