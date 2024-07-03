package Client.UI;

import Client.DataManipulationUtil;
import Client.Message;
import com.formdev.flatlaf.FlatLightLaf;
import org.json.JSONObject;
import org.opencv.core.Core;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import static Client.Configuration.*;
import static Client.UI.Assets.*;
import static Client.UI.XenUtil.*;
public class LoggedInFrame extends JFrame {
    private class WelcomePanel extends JComponent{
        WelcomePanel(){
            super();
            setBounds(75*sizeBase,0,175*sizeBase,150*sizeBase);
        }
        @Override
        public void paintComponent(Graphics g) {
            g.setColor(new Color(0xede4d3));
            g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(new Color(0x8f1e00));
            g.setFont(getSpecificFont(10*sizeBase,CAI978));
            FontMetrics fontMetrics = g.getFontMetrics();
            String[] slogans = {"Welcome","to","XenChat"};
            int[] strWidths = {
                    fontMetrics.charsWidth(slogans[0].toCharArray(),0,slogans[0].length()),
                    fontMetrics.charsWidth(slogans[1].toCharArray(),0,slogans[1].length()),
                    fontMetrics.charsWidth(slogans[2].toCharArray(),0,slogans[2].length()),
            };
            int strHeight = fontMetrics.getHeight();
            g.drawString(slogans[0],(getWidth()-strWidths[0])/2,(getHeight()+strHeight)/2 - strHeight - 10*sizeBase);
            g.drawString(slogans[1],(getWidth()-strWidths[1])/2,(getHeight()+strHeight)/2);
            g.drawString(slogans[2],(getWidth()-strWidths[0])/2,(getHeight()+strHeight)/2 + strHeight + 10*sizeBase);
            super.paintComponent(g);
        }
    }
    public class LetterBox extends JFrame {
        private JPanel lettersPanel;
        private ScrollableArea scrollableArea;
        public class FriendApplyFor extends JButton{
            public FriendApplyFor(String src,String time){
                setFont(new Font("微软雅黑",Font.PLAIN,5*sizeBase));
                setText("<html>\n" +
                        "   <div style='text-align: left;'>"+
                        "       <table>\n" +
                        "           <tr>\n" +
                        "             <td rowspan=\"2\"><font size = '"+ 10*sizeBase +"'>➕</font></td>\n" +
                        "             <td>"+ src+" 申请添加你为好友</td>\n" +
                        "           </tr>\n" +
                        "           <tr>\n" +
                        "             <td><font size='"+ sizeBase+"'>"+time+"</font></td>\n" +
                        "           </tr>\n" +
                        "        </table>\n" +
                        "   </div>"+
                        "</html>"
                );

                setBounds(0,0,77*sizeBase,20*sizeBase);
                setBorder(null);
                setForeground(new Color(0x8f1e00));
                setBackground(new Color(0xede4d3));
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(contacts.getFriends().contains(src)){
                            JOptionPane.showMessageDialog(null,"你们已经是好友");
                            scrollableArea.removeComponent(FriendApplyFor.this);
                            lettersPanel.repaint();
                            return;
                        }
                        int selected = JOptionPane.showConfirmDialog(null,"添加"+src+"为好友?","好友申请",JOptionPane.YES_NO_OPTION);
                        if(selected==JOptionPane.CLOSED_OPTION){
                            return;
                        }
                        if(selected == JOptionPane.YES_OPTION){
                            JSONObject applyACK = new JSONObject();
                            applyACK.put("type",REQUEST_FRIEND_APPLY_ACK);
                            applyACK.put("applicant",src);
                            applyACK.put("consenter",username);
                            try {
                                outToServer.writeUTF(applyACK.toString());
                                JSONObject response = new JSONObject(inFromServer.readUTF());
                                if(response.getInt("success")==1){
                                    renderFriendsArea();
                                }
                            } catch (IOException ex) {
                                System.err.println("服务器无响应");
                            }

                        }
                        //移除自身
                        scrollableArea.removeComponent(FriendApplyFor.this);
                        lettersPanel.repaint();
                        DataManipulationUtil.dropLetter(src,username);
                    }
                });
            }
        }
        public LetterBox(){
            super("收件箱");
            setSize(80*sizeBase,80*sizeBase);
            setResizable(false);
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            setLayout(null);
            setLocationRelativeTo(null);
            lettersPanel = new JPanel();
            lettersPanel.setBounds(0,0,500,500);
            lettersPanel.setLayout(null);
            scrollableArea = new ScrollableArea(0,0,80*sizeBase,70*sizeBase);
            lettersPanel.add(scrollableArea);
            add(lettersPanel);
        }
        public void addFriendApplyFor(String src,String time){
            scrollableArea.pushComponent(new FriendApplyFor(src,time));
            DataManipulationUtil.addMessage(username,new Message(src,username,time,REQUEST_ADD_FRIEND,"".getBytes()));
        }
    }
    private LoggedInUserInfoArea loggedInUserInfoArea;
    private FunctionalButton closeButton;
    private SearchArea searchArea;
    private Contacts contacts;
    private ConverseArea converseArea;
    private WelcomePanel welcomePanel;
    private Point mouseDownCompCoords;
    private JPanel panel;
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private DataInputStream inFromServer;
    private Socket clientFileSocket;
    private DataOutputStream outToFileServer;
    private DataInputStream inFromFileServer;
    private String username;
    private AtomicReference<Boolean> isLogout;
    private LetterBox letterBox;
    private boolean firstConnectFriend = true;
    private String currentTalkTo;


    public LoggedInFrame(String username,AtomicReference<Boolean> isLogout){
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            clientSocket = new Socket(SERVER_IP,20023);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new DataInputStream(clientSocket.getInputStream());
            clientFileSocket = new Socket(SERVER_IP,20024);
            outToFileServer = new DataOutputStream(clientFileSocket.getOutputStream());
            inFromFileServer = new DataInputStream(clientFileSocket.getInputStream());
            JSONObject greetToFileServer = new JSONObject();
            greetToFileServer.put("type",REQUEST_LOGIN);
            greetToFileServer.put("username",username);
            outToFileServer.writeUTF(greetToFileServer.toString());
            if (new JSONObject(inFromFileServer.readUTF()).getInt("success")==0){
                System.out.println("文件服务器连接失败");
            }else{
                System.out.println("文件服务器连接成功");
            }
            this.username = username;
            this.isLogout = isLogout;
        } catch (IOException e) {
            System.err.println("服务器连接失败");
        }
        loadAppearance();
        System.out.println(1);
        addListeners();

    }
    public void loadAppearance(){
        FlatLightLaf.setup();
        setSize(250*sizeBase, 150*sizeBase);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setUndecorated(true);
        setLocationRelativeTo(null);
        panel = new JPanel();
        panel.setSize(250*sizeBase, 150*sizeBase);
        panel.setLayout(null);
        loggedInUserInfoArea = new LoggedInUserInfoArea(username,0);
        contacts = new Contacts();
        renderFriendsArea();
        searchArea = new SearchArea(contacts,username);
        welcomePanel = new WelcomePanel();

        closeButton = new FunctionalButton(6);
        closeButton.setBackground(new Color(0xede4d3));
        closeButton.setLocation(242*sizeBase,0);
        panel.add(closeButton);
        panel.add(loggedInUserInfoArea);
        panel.add(searchArea);
        panel.add(welcomePanel);
        add(panel);
        letterBox = new LetterBox();
        //载入信件
        System.out.println(1);
        DataManipulationUtil.getFriendApplyFor(username).forEach((Message message)->addFriendApplyFor(message.getSender(),message.getTime()));
    }
    public void addListeners(){
        loggedInUserInfoArea.getAddFriendButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String friendName = JOptionPane.showInputDialog("请输入好友用户名");
                if(Objects.equals(friendName, username)){
                    JOptionPane.showMessageDialog(null,"不能添加自己为好友");
                    return;
                }

                if(contacts.getFriends().contains(friendName)){
                    JOptionPane.showMessageDialog(null,"你们已经是好友");
                    return;
                }
                if(friendName!=null){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type",REQUEST_ADD_FRIEND);
                    jsonObject.put("sender",username);
                    jsonObject.put("receiver",friendName);
                    jsonObject.put("time",getCurrentTime());
                    try {
                        outToServer.writeUTF(jsonObject.toString());
                        JSONObject response = new JSONObject( inFromServer.readUTF());
                        if(response.getInt("success")==1) {
                            JOptionPane.showMessageDialog(null, "请求已发送");
                        }
                        else{
                            JOptionPane.showMessageDialog(null, response.getString("message"));
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "请求发送失败");
                    }
                }
            }
        });
        loggedInUserInfoArea.getSettingButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newPassword = JOptionPane.showInputDialog("请输入新密码");
                if(newPassword!=null){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type",REQUEST_CHANGE_PASSWORD);
                    jsonObject.put("username",username);
                    jsonObject.put("newPasswordMD5",encryptMD5(newPassword));
                    try {
                        outToServer.writeUTF(jsonObject.toString());
                        JSONObject response = new JSONObject( inFromServer.readUTF());
                        if(response.getInt("success")==1) {
                            JOptionPane.showMessageDialog(null, "修改成功");
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "请求发送失败");
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "请求发送失败");
                    }
                }
            }
        });
        loggedInUserInfoArea.getMessageButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                letterBox.setVisible(true);
            }
        });
        closeButton.addActionListener(e -> {
            this.dispose();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type",REQUEST_LOGOUT);
            jsonObject.put("username",username);
            try {
                outToServer.writeUTF(jsonObject.toString());
                JSONObject response = new JSONObject(inFromServer.readUTF());
                isLogout.set(true);
                DataManipulationUtil.closeDatabaseConnection();
                System.exit(0);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
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

    }

    public int getStatusByName(String username){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type",REQUEST_GET_STATUS);
        jsonObject.put("username",username);
        int status;
        try {
            outToServer.writeUTF(jsonObject.toString());
            JSONObject response = new JSONObject(inFromServer.readUTF());
            status = response.getInt("status");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return status;
    }
    public void addFriendApplyFor(String src,String time){
        letterBox.addFriendApplyFor(src,time);
    }
    public void addMessage(Message message){
        contacts.addMessage(message,currentTalkTo==null || !currentTalkTo.equals(message.getSender()));
        DataManipulationUtil.addMessage(username,message);
    }

    public void updateFriends() throws IOException {
        panel.remove(contacts);
        contacts = new Contacts();
        JSONObject requestUser = new JSONObject();
        requestUser.put("type",REQUEST_GET_FRIEND_LIST);
        requestUser.put("username",username);
        outToServer.writeUTF(requestUser.toString());
        JSONObject response = new JSONObject(inFromServer.readUTF());
        String friendsString = response.getString("friends");
        ArrayList<String> friends = new ArrayList<>();
        if(!friendsString.isEmpty()) {
            friends.addAll(Arrays.asList(friendsString.split(",")));
        }
        contacts.setFriends(friends);
    }
    public void renderFriendsArea(){
        try {
            updateFriends();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(int i = 0;i<contacts.getFriends().size();i++){
            String friendName = contacts.getFriends().get(i);
            int status = getStatusByName(contacts.getFriends().get(i));
            FriendInfoArea friendInfoArea = new FriendInfoArea(friendName,status);
            if(friendName.equals(currentTalkTo)){
                renderConverseArea(friendInfoArea);
            }
            friendInfoArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                if(currentTalkTo!=null&&currentTalkTo.equals(friendInfoArea.getFriendName())){
                    return;
                }
                currentTalkTo = friendInfoArea.getFriendName();
                renderConverseArea(friendInfoArea);
                }
            });
            contacts.pushFriendInfoArea(friendInfoArea);
        }
        panel.add(contacts);
        panel.revalidate();
        panel.repaint();
    }
    public void renderConverseArea(FriendInfoArea friendInfoArea){
        String friendName = friendInfoArea.getFriendName();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type",REQUEST_CONNECT_TO);
        jsonObject.put("friendName",friendName);
        try {
            outToServer.writeUTF(jsonObject.toString());
            JSONObject response = new JSONObject(inFromServer.readUTF());
            boolean isOnline = response.getInt("success")==1;
            Socket friendServerSocket;
            Socket friendFileServerSocket;
            if(isOnline) {
                String ip = response.getString("friendIP");
                int friendServerPort = response.getInt("friendServerPort");
                int friendFileServerPort = response.getInt("friendFileServerPort");
                friendServerSocket = new Socket(ip, friendServerPort);
                friendFileServerSocket = new Socket(ip, friendFileServerPort);
                System.out.println(username+"连接到"+friendName+"的文件服务器");
            }else{
                friendServerSocket = clientSocket;
                friendFileServerSocket = clientFileSocket;
                System.out.println(username+"连接到服务器的文件服务器");
            }
            if(firstConnectFriend){
                panel.remove(welcomePanel);
                firstConnectFriend = false;
            }else{
                panel.remove(converseArea);
            }
            System.out.println(111);
            converseArea = new ConverseArea(username,friendName,friendServerSocket,friendFileServerSocket,clientFileSocket);
            //获取本地聊天记录
            System.out.println(222);
            DataManipulationUtil.getConverseRecord(username,friendName).forEach((Message message)->converseArea.addMessage(message));
            friendInfoArea.addReceivedMessageEventListener(converseArea.getReceivedMessageEventListener());
            System.out.println(333);
            panel.add(converseArea);
            panel.revalidate();
            panel.repaint();
            friendInfoArea.setUnreadMessageCount(0);
            //继续发送文件

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }


}
