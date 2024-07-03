package Client.UI;

import Client.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import static Client.UI.Assets.sizeBase;
import static Client.UI.XenUtil.*;
public class FriendInfoArea extends InfoArea {
    private ArrayList<Message> messages;
    private int unreadMessageCount ;
    private JLabel unreadMessageLabel;
    private ArrayList<ReceivedMessageEventListener> fieldUpdateListeners;
    public FriendInfoArea(String username, int status){
        super(username,status);
        messages = new ArrayList<>();
        fieldUpdateListeners = new ArrayList<>();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                backgroundColor = new Color(0xFF7F50);
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                backgroundColor = new Color(0xede4d3);
                repaint();
            }
        });
        unreadMessageCount = 0;
        unreadMessageLabel = new JLabel("未读消息 "+unreadMessageCount+" 条");
        unreadMessageLabel.setBounds(30*sizeBase,10*sizeBase,20*sizeBase,5*sizeBase);
        add(unreadMessageLabel);
    }
    public String getFriendName(){
        return username;
    }
    public void addMessage(Message message,boolean increaseUnreadMessageCount){
        messages.add(message);
        if(message.getType() != REQUEST_SEND_FILE && increaseUnreadMessageCount){
            unreadMessageCount++;
        }
        render();
        fieldUpdateListeners.forEach(fieldUpdateListener -> fieldUpdateListener.onReceivedOccurred(new ReceivedMessageEvent(this,message)));
    }
    public ArrayList<Message> getMessages(){
        return messages;
    }

    private void render(){
        unreadMessageLabel.setText("未读消息 "+unreadMessageCount+" 条");
    }
    public void setUnreadMessageCount(int count){
        unreadMessageCount = count;
        render();
    }

    public void addReceivedMessageEventListener(ReceivedMessageEventListener listener){
        fieldUpdateListeners.add(listener);
    }

}
