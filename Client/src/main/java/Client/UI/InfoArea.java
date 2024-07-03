package Client.UI;

import javax.swing.*;
import java.awt.*;

import static Client.UI.Assets.sizeBase;

public class InfoArea extends JComponent {
    protected String username;
    protected Avatar avatar;
    protected StatusIndicator statusIndicator;
    protected Color backgroundColor;
    protected JLabel usernameLabel;
    protected InfoArea(String username,int status){
        this.username = username;
        avatar = new Avatar(username.substring(0,1).toUpperCase());
        avatar.setLocation(4*sizeBase,4*sizeBase);
        statusIndicator = new StatusIndicator(status);
        statusIndicator.setLocation(20*sizeBase,10*sizeBase);
        usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("微软雅黑",Font.BOLD,4*sizeBase));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setBounds(20*sizeBase,4*sizeBase,45*sizeBase,5*sizeBase);
        usernameLabel.setForeground(new Color(0x8f1e00));
        setBounds(0,0,75*sizeBase,20*sizeBase);
        setLayout(null);
        add(avatar);
        add(statusIndicator);
        add(usernameLabel);
        backgroundColor = new Color(0xede4d3);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0,0,getWidth(),getHeight());
        super.paintComponent(g);
    }


}
