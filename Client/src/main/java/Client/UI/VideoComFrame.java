package Client.UI;

import Client.Main;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class VideoComFrame extends JFrame {
    private static byte[] buffer = new byte[57600];
    private static JLabel label;
    private static boolean isSetSized;
    private static VideoComFrame instance;
    private VideoComFrame(){
        setTitle("视频通信");
        label  = new JLabel();
        getContentPane().add(label);
        isSetSized = false;
        new Thread(()->{
            while (Main.shouldBeingLooking.get()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                BufferedImage image = null;
                try {
                    Main.videoUDPSocket.receive(packet);
                    image = ImageIO.read(new ByteArrayInputStream(packet.getData()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                label.setIcon(new ImageIcon(image));
                if (!isSetSized) {
                    setSize(image.getWidth(), image.getHeight());
                    setVisible(true);
                    isSetSized = true;
                }
            }
        }).start();
    }
    public static VideoComFrame getInstance(){
        if (instance == null) {
            synchronized (VideoComFrame.class) {
                if (instance == null) {
                    instance = new VideoComFrame();
                }
            }
        }
        return instance;
    }

    public static void close(){
        if(instance != null){
            instance.dispose();
            instance = null;
        }
    }

}
