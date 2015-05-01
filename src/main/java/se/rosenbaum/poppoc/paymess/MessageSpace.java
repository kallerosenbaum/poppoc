package se.rosenbaum.poppoc.paymess;

import java.io.Serializable;

public class MessageSpace implements Serializable{
    Long id;
    Message message;

    public MessageSpace(Long id, Message message) {
        this.id = id;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Message getMessage() {
        return message;
    }
}
