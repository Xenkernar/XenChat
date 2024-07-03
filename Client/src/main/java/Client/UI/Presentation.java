package Client.UI;

import javax.swing.*;

import static Client.UI.Assets.*;

public class Presentation extends JComponent {
    protected String username;
    protected Bubble bubble;
    protected Avatar avatar;

    public Presentation(String username){
        super();
        this.username = username;
    }

    protected void putComponent(boolean leftAlignment){

        avatar = new Avatar(username.substring(0,1));
        setLayout(null);
        setSize(175*sizeBase,6*sizeBase+bubble.getHeight());
        if(leftAlignment){
            avatar.setLocation(3*sizeBase,3*sizeBase);
            bubble.setLocation(17*sizeBase,3*sizeBase);
        }else{
            avatar.setLocation(160*sizeBase,3*sizeBase);
            bubble.setLocation(158*sizeBase-bubble.getWidth(),3*sizeBase);
        }
        add(avatar);
        add(bubble);
    }

}
