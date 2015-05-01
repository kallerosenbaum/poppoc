package se.rosenbaum.poppoc.paymess;

import java.io.Serializable;

/**
 * User: kalle
 * Date: 4/28/15 7:49 PM
 */
public class Message implements Serializable{
    String message;

    public Message(String messageText) {
        if (messageText == null) {
            throw new NullPointerException();
        }
        if (messageText.trim().isEmpty()) {
            throw new IllegalArgumentException("Message must not be null");
        }
        this.message = messageText;
    }

    public String getMessageText() {
        return message;
    }
}
