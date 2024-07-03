package Client;

import java.util.Arrays;
import java.util.Objects;

public class Message implements java.io.Serializable{
    private String sender;
    private String receiver;
    private String time;
    private int type;
    private byte[] data;
    public Message(String sender, String receiver, String time, int type, byte[] data) {
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.type = type;
        this.data = data;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return type == message.type && Objects.equals(sender, message.sender) && Objects.equals(receiver, message.receiver) && Objects.equals(time, message.time) && Arrays.equals(data, message.data);
    }

}
