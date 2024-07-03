package Client;

import Client.UI.LoggedInFrame;
import Client.UI.LoginFrame;
import Client.UI.VoiceComFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import static Client.Configuration.*;
import static Client.UI.XenUtil.*;

public class Main {

    public static ServerSocket serverSocket;
    public static DatagramSocket voiceUDPSocket;
    public static DatagramSocket videoUDPSocket;
    public static SourceDataLine sourceDataLine;
    public static TargetDataLine targetDataLine;
    public static AtomicReference<Boolean> shouldListening = new AtomicReference<>(false);
    public static AtomicReference<Boolean> shouldSaying = new AtomicReference<>(false);
    public static AtomicReference<Boolean> shouldBeingLooking = new AtomicReference<>(false);
    public static AtomicReference<Boolean> shouldSeeing = new AtomicReference<>(false);
    public static AtomicReference<LoggedInFrame> loggedInFrameReference = new AtomicReference<>();
    private static AtomicReference<Boolean> isLogout = new AtomicReference<>(false);
    public static AtomicReference<VoiceComFrame> voiceComFrameReference = new AtomicReference<>();
    //文件接收socket
    public static ServerSocket fileReceiveSocket;


    public static void main(String[] args) {
        //设置主题
        FlatDarkLaf.setup();
        //启动本地服务
        try {
            serverSocket = new ServerSocket(PORT);
            fileReceiveSocket = new ServerSocket(FILE_SERVER_PORT);
            voiceUDPSocket = new DatagramSocket(VOICE_UDP_PORT);
            videoUDPSocket = new DatagramSocket(VIDEO_UDP_PORT);
            // 音频输出设备
            sourceDataLine = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
            sourceDataLine.open(AUDIO_FORMAT);
            sourceDataLine.start();
            // 音频输入设备
//            targetDataLine = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
//            targetDataLine.open(AUDIO_FORMAT);
//            targetDataLine.start();
        } catch (IOException  e) {
            JOptionPane.showMessageDialog(null, "程序已经在运行中！", "提示", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } catch (LineUnavailableException e) {
            JOptionPane.showMessageDialog(null, "音频设备初始化失败", "提示", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }


        //创建登录触发器，登录成功后触发，text为用户名
        JButton loginTrigger = new JButton();
        loginTrigger.addActionListener((e)-> SwingUtilities.invokeLater(()-> {
            loggedInFrameReference.set(new LoggedInFrame(loginTrigger.getText(),isLogout));
            loggedInFrameReference.get().setVisible(true);
            listen();
            listenFile();
        }));

        //创建登录窗口
        SwingUtilities.invokeLater(()->new LoginFrame(loginTrigger).setVisible(true));
    }
    private static void listen(){
        new Thread(()->{
            while (!isLogout.get()) {
                Socket connectionSocket = null;
                try {
                    connectionSocket = serverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Socket finalConnectionSocket = connectionSocket;
                try {
                    new Thread(new Session(finalConnectionSocket)).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    private static void listenFile(){
        new Thread(()->{
            while (!isLogout.get()) {
                Socket connectionSocket = null;
                try {
                    connectionSocket = fileReceiveSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Socket finalConnectionSocket = connectionSocket;
                try {
                    new Thread(new FileSession(finalConnectionSocket)).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public static void startListenVoice(){
        shouldListening.set(true);
        new Thread(() -> {
            try {
                byte[] buffer = new byte[4096];
                while (shouldListening.get()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    voiceUDPSocket.receive(packet);
                    sourceDataLine.write(packet.getData(), 0, packet.getLength());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void startSaying(String remoteIP,int remotePort){
        shouldSaying.set(true);
        InetAddress remoteAddress = null;
        try {
            remoteAddress = InetAddress.getByName(remoteIP);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        // 发送本地音频数据给远程方
        InetAddress finalRemoteAddress = remoteAddress;
        new Thread(() -> {
            try {
                byte[] buffer = new byte[4096];
                while (shouldSaying.get()) {
                    int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, finalRemoteAddress, remotePort);
                    voiceUDPSocket.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void stopSaying(){
        shouldSaying.set(false);
    }
    public static void stopListening(){
        shouldListening.set(false);
    }
    public static void startBeingLooked(String remoteIP,int remoteVideoPort){
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("无法打开摄像头！");
            return;
        }
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 320);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        new Thread(()->{
            Mat frame = new Mat();
            while (shouldBeingLooking.get()) {
                if (capture.read(frame)) {
                    byte[] bytes = matToByte(frame);
                    try {
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(remoteIP), remoteVideoPort);
                        videoUDPSocket.send(packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            capture.release();
            System.out.println("摄像头已关闭");
        }).start();
    }
    public static void stopBeingLooked(){
        shouldBeingLooking.set(false);
    }
}
