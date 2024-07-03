package Client.UI;

import Client.DataManipulationUtil;
import Client.FileChunk;
import Client.Main;
import Client.Message;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import static Client.Configuration.*;

import static Client.UI.Assets.*;
import static Client.UI.XenUtil.*;
public class ConverseArea extends JComponent {
    private String username;
    private String friendName;
    private ArrayList<Message> messages;
    private ScrollableArea presentationArea;
    private FunctionBar functionBar;
    private JTextArea inputArea;
    private JLabel usernameLabel;
    private Socket friendServerSocket;
    private Socket clientFileSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket friendFileServerSocket;
    private ReceivedMessageEventListener receivedMessageEventListener;
    private HashMap<String,FilePresentation> filePresentationHashMap = new HashMap<>();
    private static Object lock = new Object();
    private class FunctionBar extends JComponent{
        private FunctionalButton sendFileButton;
        private FunctionalButton voiceComButton;
        private FunctionalButton videoComButton;
        FunctionBar(int x,int y,int width,int height){
            setBounds(x,y,width,height);
            setLayout(null);
            sendFileButton = new FunctionalButton(3);
            voiceComButton = new FunctionalButton(4);
            videoComButton = new FunctionalButton(5);
            sendFileButton.setLocation(sizeBase,2*sizeBase);
            voiceComButton.setLocation(10*sizeBase,2*sizeBase);
            videoComButton.setLocation(19*sizeBase,2*sizeBase);
            add(sendFileButton);
            add(voiceComButton);
            add(videoComButton);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0xede4d3));
            g.fillRect(0,0,getWidth(),getHeight());
            super.paintComponent(g);
        }
    }

    public ConverseArea(
            String username,
            String friendName,
            Socket friendServerSocket,
            Socket friendFileServerSocket,
            Socket clientFileSocket
    ){
        super();
        setFriendServerSocket(friendServerSocket);
        setFriendFileServerSocket(friendFileServerSocket);
        this.clientFileSocket = clientFileSocket;
        setLayout(null);
        setBounds(75*sizeBase,0,175*sizeBase,150*sizeBase);
        this.username = username;
        this.friendName = friendName;
        messages = new ArrayList<>();
        usernameLabel = new JLabel(" "+friendName);
        usernameLabel.setFont(new Font("微软雅黑",Font.BOLD,4*sizeBase));
        usernameLabel.setForeground(new Color(0x8f1e00));
        usernameLabel.setBounds(2*sizeBase,0,165*sizeBase,8*sizeBase);
        add(usernameLabel);
        presentationArea = new ScrollableArea(0,8*sizeBase,175*sizeBase,82*sizeBase);
        presentationArea.getScrollBar().setBackground(new Color(0xF7EEDD));
        presentationArea.getScrollBar().setForeground(new Color(0xffd299));
        add(presentationArea);
        functionBar = new FunctionBar(0,90*sizeBase,175*sizeBase,10*sizeBase);
        add(functionBar);
        inputArea = new JTextArea();
        inputArea.setBounds(sizeBase,102*sizeBase,173*sizeBase,38*sizeBase);
        inputArea.setFont(new Font("微软雅黑",Font.PLAIN,5*sizeBase));
        inputArea.setLineWrap(true);
        inputArea.setBorder(null);
        inputArea.setBackground(new Color(0xF7EEDD));
        inputArea.setForeground(new Color(0x8f1e00));
        JScrollPane scrollPane = new JScrollPane(inputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(sizeBase,102*sizeBase,173*sizeBase,38*sizeBase);
        add(scrollPane);
        FunctionalButton sendButton = new FunctionalButton(7);
        sendButton.setLocation(166*sizeBase,141*sizeBase);
        sendButton.setBackground(new Color(0xede4d3));
        add(sendButton);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONObject request = new JSONObject();
                Message willSend = new Message(username,friendName,getCurrentTime(),REQUEST_SEND_TEXT,inputArea.getText().getBytes());
                request.put("type",willSend.getType());
                request.put("sender",willSend.getSender());
                request.put("receiver",willSend.getReceiver());
                request.put("time",willSend.getTime());
                request.put("data",new String(willSend.getData()));
                try {
                    outputStream.writeUTF(request.toString());
                    inputStream.readUTF();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,"发送失败");
                }
                addMessage(willSend);
                inputArea.setText("");
                DataManipulationUtil.addMessage(username,willSend);
            }
        });
        receivedMessageEventListener = new ReceivedMessageEventListener() {
            @Override
            public void onReceivedOccurred(ReceivedMessageEvent event) {
                addMessage(event.getMessage());
            }
        };
        //文件发送按钮
        functionBar.sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                // 显示文件选择器对话框
                int result = fileChooser.showOpenDialog(null);

                // 处理用户选择的文件
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String time = getCurrentTime();
                    String fileName = selectedFile.getName();
                    String fileMD5 = getFileMD5(selectedFile);
                    String fileLength = selectedFile.length()+"";
                    JSONObject request = new JSONObject();
                    request.put("type",REQUEST_SEND_FILE_INFO);
                    request.put("sender",username);
                    request.put("receiver",friendName);
                    request.put("time",time);
                    request.put("data",fileMD5+","+fileLength+","+fileName);
                    try {
                        outputStream.writeUTF(request.toString());
                        inputStream.readUTF();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Message willSend = new Message(username,friendName,time,REQUEST_SEND_FILE_INFO,(fileMD5+","+fileLength+","+fileName).getBytes());
                    DataManipulationUtil.addMessage(username,willSend);
                    DataManipulationUtil.addSendFileInfo(username,friendName,time,fileName,fileMD5,Integer.parseInt(fileLength));
                    addMessage(willSend);
                    new Thread(()->{
                        //将文件分块放入数据库
                        DataManipulationUtil.breakDownFileToDatabase(username,friendName,selectedFile,fileMD5);
                        System.out.println("文件分解完成");
                        startSendFile(username,friendName,fileMD5);
                    }).start();
                }
            }
        });
        functionBar.voiceComButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientFileSocket == friendFileServerSocket){
                    JOptionPane.showMessageDialog(null,"对方不在线");
                    return;
                }
                new Thread(()->{
                    JSONObject request = new JSONObject();
                    request.put("type",REQUEST_VOICE_COMM);
                    request.put("friendName",username);
                    request.put("remoteIP",clientFileSocket.getLocalAddress().getHostAddress());
                    request.put("remoteVoicePort",VOICE_UDP_PORT);
                    try {
                        outputStream.writeUTF(request.toString());
                        JSONObject response = new JSONObject(inputStream.readUTF());
                        functionBar.voiceComButton.setEnabled(true);
                        if(response.getInt("remoteVoicePort") != -1){
                            String remoteIP = friendServerSocket.getInetAddress().getHostAddress();
                            int remoteUDPPort = response.getInt("remoteVoicePort");
                            Main.startSaying(remoteIP,remoteUDPPort);
                            Main.startListenVoice();
                            Main.voiceComFrameReference.set(new VoiceComFrame(remoteIP, remoteUDPPort));
                            Main.voiceComFrameReference.get().getStopButton().addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Main.stopListening();
                                    Main.stopSaying();
                                    Main.voiceComFrameReference.get().dispose();
                                    JSONObject request = new JSONObject();
                                    request.put("type",INFORM_STOP_VOICE_COMM);
                                    try {
                                        outputStream.writeUTF(request.toString());
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            });
                        }
                        else{
                            JOptionPane.showMessageDialog(null,"对方拒绝了您的请求");
                        }

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
                functionBar.voiceComButton.setEnabled(false);
            }
        });
        functionBar.videoComButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientFileSocket == friendFileServerSocket){
                    JOptionPane.showMessageDialog(null,"对方不在线");
                    return;
                }
                new Thread(()->{
                    JSONObject request = new JSONObject();
                    request.put("type",REQUEST_VIDEO_COMM);
                    request.put("friendName",username);
                    request.put("remoteIP",clientFileSocket.getLocalAddress().getHostAddress());
                    request.put("remoteVideoPort",VIDEO_UDP_PORT);
                    try {
                        outputStream.writeUTF(request.toString());
                        JSONObject response = new JSONObject(inputStream.readUTF());
                        functionBar.videoComButton.setEnabled(true);
                        if(response.getInt("remoteVideoPort") != -1){
                            String remoteIP = friendServerSocket.getInetAddress().getHostAddress();
                            int remoteUDPPort = response.getInt("remoteVideoPort");
                            Main.shouldBeingLooking.set(true);
                            Main.startBeingLooked(remoteIP,remoteUDPPort);
                            SwingUtilities.invokeLater(VideoComFrame::getInstance);
                            VideoComFrame.getInstance().addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosing(WindowEvent e) {
                                    Main.stopBeingLooked();
                                    JSONObject request = new JSONObject();
                                    request.put("type",INFORM_STOP_VIDEO_COMM);
                                    try {
                                        outputStream.writeUTF(request.toString());
                                    } catch (IOException ex) {}
                                }
                            });
                        }else{
                            JOptionPane.showMessageDialog(null,"对方拒绝了您的请求");
                        }

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
                functionBar.videoComButton.setEnabled(false);
            }
        });
    }

    public void setFriendServerSocket(Socket friendServerSocket){
        try {
            this.friendServerSocket = friendServerSocket;
            inputStream = new DataInputStream(friendServerSocket.getInputStream());
            outputStream = new DataOutputStream(friendServerSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setFriendFileServerSocket(Socket friendFileServerSocket){
        this.friendFileServerSocket = friendFileServerSocket;
    }
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0xede4d3));
        g.fillRect(0,0,getWidth(),getHeight());
        super.paintComponent(g);
    }

    public void addMessage(Message message){
        messages.add(message);
        String sender = message.getSender();
        boolean isReceived = !sender.equals(username);
        switch (message.getType()){
            case  REQUEST_SEND_TEXT:
                TextPresentation textPresentation = new TextPresentation(
                        sender,
                        new String(message.getData()),
                        !sender.equals(username),
                        message.getTime()
                );
                textPresentation.getBubble().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(e.getButton() == MouseEvent.BUTTON3){
                            //弹出右键PopupMenu菜单
                            JPopupMenu popupMenu = new JPopupMenu();
                            JMenuItem copyItem = new JMenuItem("Copy");
                            JMenuItem deleteItem = new JMenuItem("Delete");
                            popupMenu.add(copyItem);
                            popupMenu.add(deleteItem);
                            add(popupMenu);
                            popupMenu.show(textPresentation.getBubble(),e.getX(),e.getY());
                            copyItem.addActionListener(e1 -> {
                                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(new String(message.getData())),null);
                            });
                            deleteItem.addActionListener(e1 -> {
                                DataManipulationUtil.deleteMessage(username,message);
                                presentationArea.removeComponent(textPresentation);
                                messages.remove(message);
                                presentationArea.repaint();
                            });
                        }
                    }
                });
                presentationArea.pushComponent(textPresentation);

            break;
            case REQUEST_SEND_FILE_INFO:
                String[] fileInfos = new String(message.getData()).split(",");
                FilePresentation filePresentation = new FilePresentation(
                        sender,
                        fileInfos[0],
                        Integer.parseInt(fileInfos[1]),
                        fileInfos[2],
                        !sender.equals(username),
                        message.getTime()
                );
                presentationArea.pushComponent(filePresentation);
                filePresentationHashMap.put(filePresentation.getMD5(),filePresentation);

                if(isReceived){
                    filePresentation.getOpenButton().addActionListener((e -> DataManipulationUtil.openReceivedFile(username,friendName,fileInfos[0])));
                }else{
                    filePresentation.setSendIcon();
                }
                if(isReceived){
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            int receviedSize = DataManipulationUtil.getReceivedFileProgress(username,friendName,fileInfos[0]);
                            if(receviedSize >= filePresentation.getTotalSize()){
                                if(!DataManipulationUtil.isMerged(username,friendName,fileInfos[0])){
                                    DataManipulationUtil.mergeFileFromDatabase(fileInfos[0],defaultFileSavePath+"\\"+fileInfos[2]);
                                    DataManipulationUtil.deleteReceiveFileChunks(username,friendName,fileInfos[0]);
                                }
                                filePresentation.setReceived();
                                cancel();
                                return;
                            }
                            filePresentation.flashProgress(receviedSize);
                        }
                    },0,500);
                }
                else {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            int sendSize = DataManipulationUtil.getSentFileProgress(username, friendName, fileInfos[0]);
                            if (sendSize >= filePresentation.getTotalSize()) {
                                filePresentation.setSent();
                                cancel();
                                System.out.println("文件发送完成");
                                return;
                            }
                            filePresentation.flashProgress(sendSize);
                        }
                    }, 0, 500);
                    System.out.println(444);
                    if (DataManipulationUtil.getEarliestSendFileChunk(username, friendName, fileInfos[0]) != null) {
                        if(clientFileSocket == friendFileServerSocket){
                            System.out.println(555);
                            startSendFileToServer(username, friendName, fileInfos[0]);
                        }else{
                            startSendFile(username, friendName, fileInfos[0]);
                        }

                    }
                }
            break;
        }
    }

    public ReceivedMessageEventListener getReceivedMessageEventListener() {
        return receivedMessageEventListener;
    }

    public void startSendFileToServer(String username,String friendName,String fileMD5){
        new Thread(() -> {
            System.out.println("向服务器的文件发送线程启动");
            DataOutputStream fileOutputStream = null;
            DataInputStream fileInputStream = null;
            try {
                fileOutputStream = new DataOutputStream(clientFileSocket.getOutputStream());
                fileInputStream = new DataInputStream(clientFileSocket.getInputStream());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            while(true){
                synchronized (lock){
                    FileChunk chunk = DataManipulationUtil.getEarliestSendFileChunk(username,friendName,fileMD5);
                    if(chunk == null)break;
                    JSONObject fileChunk = new JSONObject();
                    fileChunk.put("type",REQUEST_SEND_FILE);
                    fileChunk.put("sender",username);
                    fileChunk.put("receiver",friendName);
                    fileChunk.put("fileMD5",fileMD5);
                    fileChunk.put("fileIndex",chunk.getChunkIndex());
                    fileChunk.put("fileDataLength",chunk.getData().length);
                    try {
                        fileOutputStream.writeUTF(fileChunk.toString());
                        fileOutputStream.write(chunk.getData());
                        fileInputStream.readUTF();
                        DataManipulationUtil.pollEarliestSendFileChunk(username,friendName,fileMD5);
                    } catch (IOException ex) {
                        System.out.println( "文件服务器断开连接，文件发送失败");
                        return;
                    }
                }
            }
            System.out.println("文件发送完成");
        }).start();
    }
    public void startSendFile(String username,String friendName,String fileMD5){
        new Thread(()->{
            DataOutputStream fileOutputStream = null;
            DataInputStream fileInputStream = null;
            try {
                fileOutputStream = new DataOutputStream(friendFileServerSocket.getOutputStream());
                fileInputStream = new DataInputStream(friendFileServerSocket.getInputStream());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JSONObject fileChunk = new JSONObject();
            FileChunk chunk = DataManipulationUtil.getEarliestSendFileChunk(username,friendName,fileMD5);
            while(chunk!= null){
                fileChunk.put("type",REQUEST_SEND_FILE);
                fileChunk.put("sender",username);
                fileChunk.put("receiver",friendName);
                fileChunk.put("fileMD5",fileMD5);
                fileChunk.put("fileIndex",chunk.getChunkIndex());
                fileChunk.put("fileDataLength",chunk.getData().length);
                try {
                    fileOutputStream.writeUTF(fileChunk.toString());
                    fileOutputStream.write(chunk.getData());
                    fileInputStream.readUTF();
                    DataManipulationUtil.pollEarliestSendFileChunk(username,friendName,fileMD5);
                } catch (IOException ex) {
                    System.out.println( "对方断开了连接，文件发送中断，发送了 "+chunk.getChunkIndex()+" 块,剩余已发送至服务器");
                    return;
                }
                chunk = DataManipulationUtil.getEarliestSendFileChunk(username,friendName,fileMD5);
            }
            System.out.println("文件发送完成");
        }).start();
    }

}
