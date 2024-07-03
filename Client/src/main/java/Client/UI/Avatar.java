package Client.UI;


import static Client.UI.Assets.*;
import javax.swing.*;
import java.awt.*;

public class Avatar extends JComponent {
    private String firstLetter;
    public Avatar(String firstLetter) {
        super();
        setSize(12*sizeBase,12*sizeBase);
        this.firstLetter = firstLetter;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2d.setColor(new Color(0xffd299));
        g2d.fillRoundRect(0,0,getWidth(),getHeight(),getWidth()/3,getHeight()/3);
        g2d.setColor(new Color(0x8f1e00));
        g2d.setFont(XenUtil.getSpecificFont(getWidth()*7/11,XenUtil.CAI978));
        g2d.drawString(firstLetter.toUpperCase(),getWidth()/10,getHeight()*8/11);
        super.paint(g);
    }
}
