package Client.UI;

import Client.Message;

public class ReceivedMessageEvent {
    private Message message;
    private Object source;
    public ReceivedMessageEvent(Object source, Message message) {
        this.source = source;
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
    public Object getSource() {
        return source;
    }
}
