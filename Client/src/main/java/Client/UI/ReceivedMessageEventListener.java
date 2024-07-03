package Client.UI;
@FunctionalInterface
public interface ReceivedMessageEventListener {
    void onReceivedOccurred(ReceivedMessageEvent event);
}
