package se.rosenbaum.poppoc.paymess;

public class MessageSpace {
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
