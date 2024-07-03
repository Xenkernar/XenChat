package Server;

import Server.UI.MainWindow;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

public class Main {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        new MainWindow().setVisible(true);
    }
}
