package Client.UI;

import javax.swing.*;

public class Bubble extends JComponent {
    private String time;
    public Bubble(String time){
        super();
        this.time = time;
    }
    public String getTime(){
        return time;
    }
}
