package Client.UI;

import java.awt.*;

import static Client.UI.Assets.*;
public class LoggedInUserInfoArea extends InfoArea{
    private FunctionalButton addFriendButton;
    private FunctionalButton settingButton;
    private FunctionalButton messageButton;
    private FunctionalButton logoutButton;
    public LoggedInUserInfoArea(String username,int status){
        super(username,status);
        addFriendButton = new FunctionalButton(0);
        addFriendButton.setLocation(66*sizeBase,sizeBase);
        settingButton = new FunctionalButton(1);
        settingButton.setLocation(66*sizeBase,11*sizeBase);
        logoutButton = new FunctionalButton(10);
        logoutButton.setLocation(57*sizeBase,sizeBase);
        messageButton = new FunctionalButton(9);
        messageButton.setLocation(57*sizeBase,11*sizeBase);
        add(addFriendButton);
        add(settingButton);
        add(logoutButton);
        add(messageButton);
        setBackground(new Color(0xede4d3));
    }

    public FunctionalButton getAddFriendButton() {
        return addFriendButton;
    }

    public FunctionalButton getSettingButton() {
        return settingButton;
    }

    public FunctionalButton getMessageButton() {
        return messageButton;
    }

    public FunctionalButton getLogoutButton() {
        return logoutButton;
    }
}
