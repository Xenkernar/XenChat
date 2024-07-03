package Client.UI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static Client.UI.Assets.*;
public class SearchArea extends JComponent {
    private HashMap<String,Integer> friendsIndexMap;
    private ScrollableArea contacts;
    private String username;
    public SearchArea(ScrollableArea contacts,String username){
        super();
        this.contacts = contacts;
        this.username = username;
        friendsIndexMap = new HashMap<>();
        ArrayList<JComponent> items = contacts.getItems();
        for (int i = 0; i < items.size(); i++) {
            friendsIndexMap.put(((FriendInfoArea)items.get(i)).username,i);
        }
        setBounds(0,20*sizeBase,75*sizeBase,10*sizeBase);
        setLayout(null);
        JFormattedTextField textField = new JFormattedTextField();
        textField.setBounds(sizeBase,sizeBase,64*sizeBase,8*sizeBase);
        textField.setForeground(new Color(0x8f1e00));
        textField.setBackground(new Color(0xF7EEDD));
        FunctionalButton serachButton = new FunctionalButton(2);
        serachButton.setBounds(66*sizeBase,sizeBase,8*sizeBase,8*sizeBase);
        add(textField);
        add(serachButton);
        serachButton.addActionListener(e -> {
            String targetName = textField.getText();
            if(friendsIndexMap.containsKey(targetName)) {
                contacts.scrollTo(friendsIndexMap.get(targetName));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0xede4d3));
        g.fillRect(0,0,getWidth(),getHeight());
        super.paintComponent(g);
    }
}
