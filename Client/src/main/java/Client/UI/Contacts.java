package Client.UI;

import Client.Message;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static Client.UI.Assets.sizeBase;

public class Contacts extends ScrollableArea{
    private ArrayList<String> friends;
    private HashMap<String,FriendInfoArea> friendInfoAreaHashMap;
    public Contacts() {

        super(0,30*sizeBase,75*sizeBase,120*sizeBase);
        getScrollBar().setBackground(new Color(0xede4d3));
        friends = new ArrayList<>();
        friendInfoAreaHashMap = new HashMap<>();
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
        friendInfoAreaHashMap.clear();

    }

    public void pushFriendInfoArea(FriendInfoArea friendInfoArea) {
        super.pushComponent(friendInfoArea);
        friendInfoAreaHashMap.put(friendInfoArea.getFriendName(),friendInfoArea);
    }

    public void addMessage(Message message,boolean increaseUnreadMessageCount){
        friendInfoAreaHashMap.get(message.getSender()).addMessage(message,increaseUnreadMessageCount);
    }

    public FriendInfoArea getFriendInfoArea(String friendName){
        return friendInfoAreaHashMap.get(friendName);
    }

}
