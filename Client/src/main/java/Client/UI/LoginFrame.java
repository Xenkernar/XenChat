package Client.UI;

import com.formdev.flatlaf.FlatLightLaf;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static Client.UI.XenUtil.*;
import static Client.Configuration.*;

public class LoginFrame extends JFrame {
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 500;
    private static final int BUTTON_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 60;
    private BufferedImage image;
    private Point mouseDownCompCoords;
    private Socket clientSocket;
    private DataInputStream inFromServer;
    private DataOutputStream outToServer;
    public LoginFrame(JButton loginTrigger){
        FlatLightLaf.setup();
        try {
            this.clientSocket = new Socket(SERVER_IP,20023);
            this.inFromServer = new DataInputStream(clientSocket.getInputStream());
            this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"无法连接到服务器,即将退出程序","错误",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.setLayout(null);
        this.setBackground(new Color(0,0,0,0));
        this.setFocusableWindowState(true);
        try {
            image = ImageIO.read(new File("src/main/resources/LoginBackground1.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //输入框
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        usernameField.setBounds((FRAME_WIDTH-BUTTON_WIDTH)/2,180,BUTTON_WIDTH,BUTTON_HEIGHT-10);
        passwordField.setBounds((FRAME_WIDTH-BUTTON_WIDTH)/2,240,BUTTON_WIDTH,BUTTON_HEIGHT-10);
        usernameField.setBackground(new Color(0,0,0,128));
        passwordField.setBackground(new Color(0,0,0,128));
        usernameField.setForeground(new Color(0xffc7ff));
        passwordField.setForeground(new Color(0xffc7ff));
        usernameField.setFont(getSpecificFont(40,ASTERX));
        passwordField.setFont(getSpecificFont(40,ASTERX));
        usernameField.setHorizontalAlignment(JTextField.CENTER);
        passwordField.setHorizontalAlignment(JTextField.CENTER);
        usernameField.enableInputMethods(false);
        passwordField.setEchoChar((char)0);
        usernameField.setText("@Username");
        passwordField.setText("@Password");
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(usernameField.getText().equals("@Username")){
                    usernameField.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if(usernameField.getText().equals("")){
                    usernameField.setText("@Username");
                }
            }
        });
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(passwordField.getText().equals("@Password")){
                    passwordField.setText("");
                    passwordField.setFont(getSpecificFont(60,ASTERX));
                    passwordField.setEchoChar('-');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if(passwordField.getText().equals("")){
                    passwordField.setFont(getSpecificFont(40,ASTERX));
                    passwordField.setEchoChar((char)0);
                    passwordField.setText("@Password");
                }
            }
        });

        this.add(usernameField);
        this.add(passwordField);

        //按钮
        JButton signUpBtn = new JButton("S i g n   U p");
        JButton loginBtn = new JButton("L o g i n");
        signUpBtn.setBounds((FRAME_WIDTH-BUTTON_WIDTH)/2,400,BUTTON_WIDTH,BUTTON_HEIGHT);
        loginBtn.setBounds((FRAME_WIDTH-BUTTON_WIDTH)/2,330,BUTTON_WIDTH,BUTTON_HEIGHT);
        signUpBtn.setBackground(new Color(0,0,0,156));
        loginBtn.setBackground(new Color(0,0,0,156));
        signUpBtn.setFont(getSpecificFont(40,ASTERX));
        loginBtn.setFont(getSpecificFont(40,ASTERX));
        signUpBtn.setForeground(new Color(0x6c35de));
        loginBtn.setForeground(new Color(0x6c35de));
        this.add(signUpBtn);
        this.add(loginBtn);
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseDownCompCoords = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                mouseDownCompCoords = null;
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
            public void mouseMoved(MouseEvent e) {
                if(!getFocusableWindowState()){
                    setFocusableWindowState(true);
                }
            }
        });

        //按钮事件
        loginBtn.addActionListener((e)->{
            String username = usernameField.getText();
            String passwordMD5 = encryptMD5(passwordField.getText());
            try {
                JSONObject respone = getResponse(REQUEST_LOGIN, username, passwordMD5,PORT,FILE_SERVER_PORT);
                boolean success = respone.getInt("success") == 1;
                String message = respone.getString("message");
                if(success) {
                    loginTrigger.setText(username);
                    loginTrigger.doClick();
                    this.dispose();
                }else{
                    JOptionPane.showMessageDialog(null,message,"登录失败",JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        signUpBtn.addActionListener((e)-> {
            String username = usernameField.getText();
            String passwordMD5 = encryptMD5(passwordField.getText());
            try {
                JSONObject respone = getResponse(REQUEST_SIGN_UP, username, passwordMD5,PORT,FILE_SERVER_PORT);
                boolean success = respone.getInt("success") == 1;
                String message = respone.getString("message");
                if (success) {
                    loginTrigger.setText(username);
                    loginTrigger.doClick();
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(null,message,"注册失败",JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

    }

    private JSONObject getResponse(int type, String username, String passwordMD5,int port,int fileServerPort) throws IOException {
        JSONObject request = new JSONObject();
        request.put("type", type);
        request.put("username", username);
        request.put("passwordMD5", passwordMD5);
        request.put("serverPort",port);
        request.put("fileServerPort",fileServerPort);
        outToServer.writeUTF(request.toString());
        JSONObject response = new JSONObject(inFromServer.readUTF());
        return response;
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(
                image,
                0,
                0,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                null
        );
        g.setFont(getSpecificFont(100,ASTERIX));
        g.setColor(new Color(0xcb80ff));
        g.drawString("XEN CHAT",FRAME_WIDTH/2-140,110);
        super.paintComponents(g);
    }


}

