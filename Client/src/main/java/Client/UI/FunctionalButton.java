package Client.UI;

import javax.swing.*;
import java.awt.*;

import static Client.UI.Assets.*;
public class FunctionalButton extends JButton {
    public FunctionalButton(int function){
        super(FUNCTION_ICONS[function]);
        setSize(8*sizeBase,8*sizeBase);
        setBorder(null);
        setBackground(new Color(0xede4d3));
        setForeground(new Color(0x8f1e00));
    }

}
