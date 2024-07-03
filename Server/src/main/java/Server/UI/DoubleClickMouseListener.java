package Server.UI;

import Server.DataManipulationUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoubleClickMouseListener extends MouseAdapter {
    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            JTable table = (JTable) event.getSource();
            int row = table.getSelectedRow();
            String username = (String)table.getValueAt(row, 0);
            new MessagesFrame(DataManipulationUtil.getMessages(username)).setVisible(true);
        }
    }
}
