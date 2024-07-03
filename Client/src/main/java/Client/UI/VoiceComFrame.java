package Client.UI;

import Client.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;

import static Client.UI.Assets.sizeBase;
public class VoiceComFrame extends JFrame {
    private class LoopSlider extends JComponent{
        private int beginX;
        private int slideWidth;
        private Timer timer;
        LoopSlider(int slideWidth){
            super();
            this.slideWidth = slideWidth;
            this.beginX = 0;
        }
        @Override
        public void paint(Graphics g) {
            g.setColor(new Color(0xede4d3));
            g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(new Color(0x8f1e00));
            g.fillRect(beginX,0,slideWidth,getHeight());
            super.paint(g);
        }
        public void startSlide(int timeSpan,int moveDistance){
            timer = new Timer(timeSpan,e -> {
                beginX += moveDistance;
                if(beginX>getWidth()){
                    beginX = -slideWidth;
                }
                repaint();
            });
            timer.start();
        }
        public void stopSlide(){
            timer.stop();
        }
    }
    private boolean isSaying = true;
    private boolean isListen = true;
    private String remoteIP;
    private int remoteUDPPort;
    private JButton stopButton;
    public VoiceComFrame(String remoteIP,int remoteUDPPort){
        this.remoteIP = remoteIP;
        this.remoteUDPPort = remoteUDPPort;
        this.setSize(sizeBase*80,sizeBase*40);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setBounds(0,0,sizeBase*80,sizeBase*40);
        panel.setBackground(new Color(0xede4d3));
        panel.setLayout(null);
        this.add(panel);
        //自动来回滑动的滑块
        LoopSlider slider = new LoopSlider(20*sizeBase);
        slider.setBounds(sizeBase*5,sizeBase*5,sizeBase*70,sizeBase*5);
        panel.add(slider);
        JButton closeMicButton = new JButton("<html><font size = '"+Assets.sizeBase*2+"' >🎤</font></html>");
        closeMicButton.setBounds(sizeBase*5,sizeBase*10,sizeBase*16,sizeBase*16);
        closeMicButton.setBackground(new Color(0xede4d3));
        closeMicButton.setForeground(new Color(0xFF7F50));
        closeMicButton.setBorder(null);
        panel.add(closeMicButton);
        closeMicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isSaying){
                    Main.stopSaying();
                    isSaying = false;
                    closeMicButton.setForeground(new Color(0x2c2c2c));
                }else{
                    Main.startSaying(remoteIP,remoteUDPPort);
                    isSaying = true;
                    closeMicButton.setForeground(new Color(0xFF7F50));
                }
            }
        });

        stopButton = new JButton("<html><font size = '"+Assets.sizeBase*2+"' >📞</font></html>");
        stopButton.setBounds(sizeBase*32,sizeBase*10,sizeBase*16,sizeBase*16);
        stopButton.setBackground(new Color(0xede4d3));
        stopButton.setForeground(Color.RED);
        stopButton.setBorder(null);
        panel.add(stopButton);

        JButton closeSpeakerButton = new JButton("<html><font size = '"+Assets.sizeBase*2+"' >🎧</font></html>");
        closeSpeakerButton.setBounds(sizeBase*59,sizeBase*10,sizeBase*16,sizeBase*16);
        closeSpeakerButton.setBackground(new Color(0xede4d3));
        closeSpeakerButton.setForeground(new Color(0xFF7F50));
        closeSpeakerButton.setBorder(null);
        panel.add(closeSpeakerButton);
        closeSpeakerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isListen){
                    Main.stopListening();
                    isListen = false;
                    closeSpeakerButton.setForeground(new Color(0x2c2c2c));
                }else{
                    Main.startListenVoice();
                    isListen = true;

                    closeSpeakerButton.setForeground(new Color(0xFF7F50));
                }
            }
        });


        new Thread(()->{
            slider.startSlide(10,5);
        }).start();
        setVisible(true);
    }
    public JButton getStopButton(){
        return stopButton;
    }
}
