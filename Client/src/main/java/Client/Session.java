package Client;

import Client.UI.LoggedInFrame;
import Client.UI.VideoComFrame;
import Client.UI.VoiceComFrame;
import Client.UI.XenUtil;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
import static Client.Configuration.*;
import static Client.UI.XenUtil.*;
import static Client.UI.XenUtil.INFORM_STOP_VOICE_COMM;

public class Session implements Runnable{
    private Socket socket;
    private DataInputStream inFromClient;
    private DataOutputStream outToClient;
    private boolean isLogout = false;
    private LoggedInFrame clientFrame;
    public Session(Socket socket) throws IOException {
        this.socket = socket;
        inFromClient = new DataInputStream(socket.getInputStream());
        outToClient = new DataOutputStream(socket.getOutputStream());
        this.clientFrame = Main.loggedInFrameReference.get();
    }
    public JSONObject generateResponse(String jsonRequest,AtomicReference<Boolean> needResponse){
        JSONObject jsonObject = new JSONObject(jsonRequest);
        int type = jsonObject.getInt("type");
        JSONObject response = new JSONObject();
        switch (type) {
            case REQUEST_ADD_FRIEND://添加好友
                String sender = jsonObject.getString("sender");
                String receiver = jsonObject.getString("receiver");
                String time = jsonObject.getString("time");
                clientFrame.addFriendApplyFor(sender,time);
                response.put("type",RESPONSE_RECEIVED_ACK);
                break;

            case REQUEST_SEND_TEXT://发送文本
                sender = jsonObject.getString("sender");
                receiver = jsonObject.getString("receiver");
                time = jsonObject.getString("time");
                String text = jsonObject.getString("data");
                Message message = new Message(sender, receiver, time, REQUEST_SEND_TEXT, text.getBytes());
                clientFrame.addMessage(message);
                response.put("type",RESPONSE_RECEIVED_ACK);
                break;

            case REQUEST_SEND_FILE_INFO://发送文件信息
                sender = jsonObject.getString("sender");
                receiver = jsonObject.getString("receiver");
                time = jsonObject.getString("time");
                String fileParams = jsonObject.getString("data");
                String fileMD5 = fileParams.split(",")[0];
                String fileLength = fileParams.split(",")[1];
                String fileName = fileParams.split(",")[2];
                message = new Message(sender, receiver, time, REQUEST_SEND_FILE_INFO, fileParams.getBytes());
                clientFrame.addMessage(message);
                DataManipulationUtil.addReceiveFileInfo(receiver,sender,time,fileName,fileMD5,Integer.parseInt(fileLength));
                response.put("type",RESPONSE_RECEIVED_ACK);
                break;


            case INFORM_UPDATE_FRIENDS_AREA:
                needResponse.set(false);
                clientFrame.renderFriendsArea();
                break;

            case INFORM_REMOTE_LOGIN:
                needResponse.set(false);
                String remoteIP = jsonObject.getString("remoteIP");
                int remoteServerPort  = jsonObject.getInt("remoteServerPort");
                String loginTime = jsonObject.getString("time");
                JOptionPane.showMessageDialog(null,"你的账号于 "+loginTime+" 在另一台机器("+remoteIP+":"+ remoteServerPort+")登录");
                System.exit(0);

            case REQUEST_VOICE_COMM:
                String friendName = jsonObject.getString("friendName");
                remoteIP = jsonObject.getString("remoteIP");
                int remoteVoicePort = jsonObject.getInt("remoteVoicePort");
                int option = JOptionPane.showConfirmDialog(null,"您的好友" + friendName+"想和你进行语音通话","语音通话邀请",JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION){
                    String finalRemoteIP = remoteIP;
                    new Thread(()->{
                        Main.startListenVoice();
                        Main.startSaying(finalRemoteIP,remoteVoicePort);
                        Main.voiceComFrameReference.set(new VoiceComFrame(finalRemoteIP,remoteVoicePort));
                        Main.voiceComFrameReference.get().getStopButton().addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Main.stopListening();
                                Main.stopSaying();
                                Main.voiceComFrameReference.get().dispose();
                                JSONObject request = new JSONObject();
                                request.put("type",INFORM_STOP_VOICE_COMM);
                                try {
                                    outToClient.writeUTF(request.toString());
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
                    }).start();
                    response.put("type",RESPONSE_RECEIVED_ACK);
                    response.put("remoteVoicePort",VOICE_UDP_PORT);

                }else{
                    response.put("type",RESPONSE_RECEIVED_ACK);
                    response.put("remoteVoicePort",-1);
                }
                break;

            case REQUEST_VIDEO_COMM:
                friendName = jsonObject.getString("friendName");
                remoteIP = jsonObject.getString("remoteIP");
                int remoteVideoPort = jsonObject.getInt("remoteVideoPort");
                option = JOptionPane.showConfirmDialog(null,"您的好友" + friendName+"想和你进行视频通信","视频通信邀请",JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION) {
                    Main.shouldBeingLooking.set(true);
                    SwingUtilities.invokeLater(VideoComFrame::getInstance);
                    VideoComFrame.getInstance().addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            Main.stopBeingLooked();
                            JSONObject request = new JSONObject();
                            request.put("type",INFORM_STOP_VIDEO_COMM);
                            try {
                                outToClient.writeUTF(request.toString());
                            } catch (IOException ex) {}
                        }
                    });
                    Main.startBeingLooked(remoteIP,remoteVideoPort);
                    response.put("type",RESPONSE_RECEIVED_ACK);
                    response.put("remoteVideoPort",VIDEO_UDP_PORT);
                }else{
                    response.put("type",RESPONSE_RECEIVED_ACK);
                    response.put("remoteVideoPort",-1);
                }
                break;

            case INFORM_STOP_VOICE_COMM:
                needResponse.set(false);
                Main.stopSaying();
                Main.stopListening();
                Main.voiceComFrameReference.get().dispose();
                JOptionPane.showMessageDialog(null,"对方已挂断","语音通话",JOptionPane.INFORMATION_MESSAGE);
                break;

            case INFORM_STOP_VIDEO_COMM:
                needResponse.set(false);
                JOptionPane.showMessageDialog(null,"对方已挂断","视频通信",JOptionPane.INFORMATION_MESSAGE);
                Main.stopBeingLooked();
                VideoComFrame.close();
                break;
            default:
                break;
        }
        return response;
    }

    @Override
    public void run() {
        AtomicReference<Boolean> needResponse = new AtomicReference<>(true);
        while (!isLogout){
            try {
                String jsonRequest = inFromClient.readUTF();
                needResponse.set(true);
                String jsonResponse = generateResponse(jsonRequest,needResponse).toString();
                if(needResponse.get()){
                    outToClient.writeUTF(jsonResponse);
                }
                if(isLogout){
                    socket.close();
                }
            } catch (IOException e) {
                //用户断开连接
                isLogout = true;
            }
        }

    }

}

