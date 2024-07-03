package Server;

public class FileChunk {
    private String sender;
    private String receiver;
    private String MD5;
    private int chunkIndex;
    private byte[] data;
    public FileChunk(String sender, String receiver, String MD5,int chunkIndex, byte[] data) {
        this.sender = sender;
        this.receiver = receiver;
        this.MD5 = MD5;
        this.chunkIndex = chunkIndex;
        this.data = data;

    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMD5() {
        return MD5;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public byte[] getData() {
        return data;
    }
}
