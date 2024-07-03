package Client.UI;

import javax.swing.*;
import java.awt.*;
import static Client.UI.Assets.*;

public class StatusIndicator extends JComponent {
    int status;
    public StatusIndicator(int status){
        super();
        this.status = status;
        setSize(5*sizeBase,5*sizeBase);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2d.setColor(STATUS_INDICATOR_COLOR[status]);
        g2d.fillOval(0,0,getWidth(),getHeight());
    }

    public void setStatus(int status) {
        this.status = status;
        repaint();
    }

    public int getStatus() {
        return status;
    }
}
