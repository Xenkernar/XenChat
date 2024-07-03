package Server;

public class Message {
    private String sender;
    private String receiver;
    private String time;
    private int type;
    private byte[] data;
    public Message(String sender, String receiver, String time,int type, byte[] data) {
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
}
