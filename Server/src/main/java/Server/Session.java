package Server;

import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Timer;

import static Server.XenUtil.*;
public class Session implements Runnable{
    private Socket socket;
    private DataInputStream inFromClient;
    private DataOutputStream outToClient;

    private Socket userServerSocket;
    private DataInputStream inFromUserServer;
    private DataOutputStream outToUserServer;
    private boolean isLogout = false;
    private String username;
    private boolean authenticated = true;
    public Session(Socket socket) throws IOException {
        this.socket = socket;
        inFromClient = new DataInputStream(socket.getInputStream());
        outToClient = new DataOutputStream(socket.getOutputStream());
    }
    public JSONObject generateResponse(String jsonRequest){
        JSONObject jsonObject = new JSONObject(jsonRequest);
        int type = jsonObject.getInt("type");
        JSONObject response = new JSONObject();
        switch (type) {
            case REQUEST_LOGIN://登录
                String username = jsonObject.getString("username");
                String passwordMD5 = jsonObject.getString("passwordMD5");
                int serverPort = jsonObject.getInt("serverPort");
                int fileServerPort = jsonObject.getInt("fileServerPort");
                boolean userExist = DataManipulationUtil.isUserExist(username);
                if(!userExist){
                    response.put("success",0);
                    response.put("message","用户不存在");
                    System.out.println("用户不存在");
                    break;
                }

                boolean passwordCorrect = passwordMD5.equals(DataManipulationUtil.getUser(username).getPasswordMD5());

                if(!passwordCorrect){
                    response.put("success",0);
                    response.put("message","密码错误");
                    System.out.println("密码错误");
                    break;
                }

                response.put("success",1);
                response.put("message","登录成功");
                this.username = username;
                Socket userServerSocket1 = DataManipulationUtil.getUserServerSocket(username);
                if(userServerSocket1!=null){
                    try {
                        JSONObject remoteLoginInform = new JSONObject();
                        remoteLoginInform.put("type",INFORM_REMOTE_LOGIN);
                        remoteLoginInform.put("remoteIP",socket.getInetAddress().getHostAddress());
                        remoteLoginInform.put("remoteServerPort",serverPort);
                        remoteLoginInform.put("time",getCurrentTime());
                        new DataOutputStream(userServerSocket1.getOutputStream()).writeUTF(remoteLoginInform.toString());
                        DataManipulationUtil.getSession(username).depriveAuthorization();
                        DataManipulationUtil.getFileSession(username).depriveAuthorization();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                DataManipulationUtil.setUserStatus(username,STATUS_ONLINE);
                DataManipulationUtil.setUserIP(username,socket.getInetAddress().getHostAddress());
                DataManipulationUtil.setUserServerPort(username,serverPort);
                DataManipulationUtil.setUserFileServerPort(username,fileServerPort);
                DataManipulationUtil.setUserLastLoginTime(username,XenUtil.getCurrentTime());
                connectToUserServer(socket.getInetAddress().getHostAddress(),serverPort);

                //记录socket
                DataManipulationUtil.addUserSession(username,this);
                DataManipulationUtil.addUserServerSocket(username,userServerSocket);

                //转发消息
                new Thread(()->{
                    Message message = DataManipulationUtil.getEarliestMessage(username);
                    while (message != null){
                        try {
                            JSONObject messageJSON = new JSONObject();
                            messageJSON.put("type",message.getType());
                            messageJSON.put("sender",message.getSender());
                            messageJSON.put("receiver",message.getReceiver());
                            messageJSON.put("time",message.getTime());
                            messageJSON.put("data",new String(message.getData()));
                            outToUserServer.writeUTF(messageJSON.toString());
                            JSONObject responseFromUser = new JSONObject(inFromUserServer.readUTF());
                            if(responseFromUser.getInt("type") == RESPONSE_RECEIVED_ACK){
                                DataManipulationUtil.pollEarliestMessage(username);
                            }
                            message = DataManipulationUtil.getEarliestMessage(username);
                        } catch (IOException e) {
                            //用户断开连接
                            System.err.println(XenUtil.getCurrentTime() + " : " + username+" 断开连接");
                            doAfterLogout();
                            isLogout = true;
                            break;
                        }
                    }
                }).start();
                //提醒该用户的在线好友ta上线了
                informFriendUpdataFriendsArea();

                break;
            case REQUEST_SIGN_UP://注册
                username = jsonObject.getString("username");
                passwordMD5 = jsonObject.getString("passwordMD5");
                serverPort = jsonObject.getInt("serverPort");
                fileServerPort = jsonObject.getInt("fileServerPort");
                userExist = DataManipulationUtil.isUserExist(username);
                if(userExist){
                    response.put("success",0);
                    response.put("message","用户已存在");
                    break;
                }
                response.put("success",1);
                response.put("message","注册成功");
                this.username = username;
                DataManipulationUtil.addUser(
                        username,
                        passwordMD5,
                        STATUS_ONLINE,
                        XenUtil.getCurrentTime(),
                        socket.getInetAddress().getHostAddress(),
                        serverPort,
                        fileServerPort
                );

                connectToUserServer(socket.getInetAddress().getHostAddress(),serverPort);
                DataManipulationUtil.addUserSession(username,this);
                DataManipulationUtil.addUserServerSocket(username,userServerSocket);
                break;
            case REQUEST_LOGOUT://下线
                username = jsonObject.getString("username");
                response.put("success",1);
                doAfterLogout();
                isLogout = true;
                break;
            case REQUEST_GET_STATUS://获取状态
                username = jsonObject.getString("username");
                response.put("status",DataManipulationUtil.getUser(username).getStatus());
                break;

            case REQUEST_GET_FRIEND_LIST://获取好友列表
                username = jsonObject.getString("username");
                ArrayList<String> friendsList = DataManipulationUtil.getUserFriends(username);
                StringBuilder friends = new StringBuilder();
                if(!friendsList.isEmpty()){
                    for(String friend : friendsList){
                        friends.append(friend).append(",");
                    }
                }
                response.put("friends",friends.toString());
                break;

            case REQUEST_ADD_FRIEND://添加好友
                String sender = jsonObject.getString("sender");
                String receiver = jsonObject.getString("receiver");
                String time = jsonObject.getString("time");
                if(DataManipulationUtil.isUserExist(receiver)){
                    if(DataManipulationUtil.getUserFriends(receiver).contains(sender)){
                        response.put("success",0);
                        response.put("message","对方已经是你的好友");
                        break;
                    }
                    Socket receiverServerSocket = DataManipulationUtil.getUserServerSocket(receiver);
                    if(receiverServerSocket!=null){
                        JSONObject request = new JSONObject();
                        request.put("type",REQUEST_ADD_FRIEND);
                        request.put("sender",sender);
                        request.put("receiver",receiver);
                        request.put("time",time);
                        request.put("data","".getBytes());
                        try {
                            new DataOutputStream(receiverServerSocket.getOutputStream()).writeUTF(request.toString());
                            new DataInputStream(receiverServerSocket.getInputStream()).readUTF();
                        } catch (IOException e) {
                            System.err.println("向在线用户发送好友请求失败");
                            DataManipulationUtil.addMessage(sender,receiver,time,REQUEST_ADD_FRIEND,"".getBytes());
                        }
                    } else{
                        DataManipulationUtil.addMessage(sender,receiver,time,REQUEST_ADD_FRIEND,"".getBytes());
                    }
                    response.put("success",1);
                    break;
                }
                else{
                    response.put("success",0);
                    response.put("message","用户不存在");
                    break;
                }

            case REQUEST_SEND_TEXT://发送文本
                sender = jsonObject.getString("sender");
                receiver = jsonObject.getString("receiver");
                time = jsonObject.getString("time");
                String data = jsonObject.getString("data");
                DataManipulationUtil.addMessage(sender,receiver,time,REQUEST_SEND_TEXT,data.getBytes());
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
                DataManipulationUtil.addMessage(sender,receiver,time,REQUEST_SEND_FILE_INFO,fileParams.getBytes());
                DataManipulationUtil.addFileInfo(sender,receiver,time,fileName,fileMD5,Integer.parseInt(fileLength));
                response.put("type",RESPONSE_RECEIVED_ACK);
                break;

            case REQUEST_CONNECT_TO://连接到用户
                String friendName = jsonObject.getString("friendName");
                String friendIP = DataManipulationUtil.getUser(friendName).getIP();
                if(DataManipulationUtil.getUser(friendName).getStatus() == STATUS_OFFLINE){
                    response.put("success",0);
                    break;
                }
                response.put("success",1);
                response.put("friendIP",friendIP);
                response.put("friendServerPort",DataManipulationUtil.getUser(friendName).getServerPort());
                response.put("friendFileServerPort",DataManipulationUtil.getUser(friendName).getFileServerPort());
                break;
            case REQUEST_FRIEND_APPLY_ACK:
                String applicant = jsonObject.getString("applicant");
                String consenter = jsonObject.getString("consenter");
                DataManipulationUtil.addFriend(applicant,consenter);
                DataManipulationUtil.addFriend(consenter,applicant);
                //如果申请人在线，通知申请人更新好友列表
                int applicantStatus = DataManipulationUtil.getUser(applicant).getStatus();
                if(applicantStatus == STATUS_ONLINE || applicantStatus == STATUS_BUSY){
                    JSONObject inform = new JSONObject();
                    inform.put("type",INFORM_UPDATE_FRIENDS_AREA);
                    try {
                        new DataOutputStream(DataManipulationUtil.getUserServerSocket(applicant).getOutputStream()).writeUTF(inform.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                response.put("success",1);
                break;
            case REQUEST_CHANGE_PASSWORD:
                System.out.println("准备修改密码");
                username = jsonObject.getString("username");
                String newPasswordMD5 = jsonObject.getString("newPasswordMD5");
                DataManipulationUtil.changePassword(username,newPasswordMD5);
                response.put("success",1);
                break;
            default:
                break;
        }
        return response;
    }

    @Override
    public void run() {
        while (!isLogout){
            if(authenticated){
                try {
                    String jsonRequest = inFromClient.readUTF();
                    System.out.println(jsonRequest);
                    String jsonResponse = generateResponse(jsonRequest).toString();
                    System.out.println(jsonResponse);
                    outToClient.writeUTF(jsonResponse);
                    if(isLogout){
                        socket.close();
                    }
                } catch (IOException e) {
                    //用户断开连接
                    if(username != null) {
                        System.out.println(XenUtil.getCurrentTime() + " : " + username + " 断开连接");
                    }
                    isLogout = true;
                }
            }
        }
        if(username != null && authenticated) {
            doAfterLogout();
        }
    }

    public void connectToUserServer(String userIP,int serverPort){
        try {
            this.userServerSocket = new Socket(userIP,serverPort);
            this.outToUserServer = new DataOutputStream(userServerSocket.getOutputStream());
            this.inFromUserServer = new DataInputStream(userServerSocket.getInputStream());
            System.out.println(XenUtil.getCurrentTime() + " : " +"服务器连接到用户 "+ username);
        } catch (IOException e) {
            //无法连接到用户
            System.err.println(XenUtil.getCurrentTime() + " : " + username+" 无法连接到用户");
            doAfterLogout();
            isLogout = true;
        }
    }


    public void doAfterLogout(){
        if(username!=null){
            DataManipulationUtil.setUserStatus(username,STATUS_OFFLINE);
            DataManipulationUtil.setUserIP(username,"");
            DataManipulationUtil.setUserServerPort(username,-1);
            DataManipulationUtil.setUserFileServerPort(username,-1);
            DataManipulationUtil.removeUserServerSocket(username);
            DataManipulationUtil.removeUserSession(username);
            DataManipulationUtil.getFileSession(username).depriveAuthorization();
            informFriendUpdataFriendsArea();
        }
    }

    public void informFriendUpdataFriendsArea(){
        if(username == null)return;
        for(String friend : DataManipulationUtil.getUserFriends(username)){
            int friendStatus = DataManipulationUtil.getUser(friend).getStatus();
            if(friendStatus == STATUS_ONLINE || friendStatus == STATUS_BUSY){
                JSONObject inform = new JSONObject();
                inform.put("type",INFORM_UPDATE_FRIENDS_AREA);
                try {
                    new DataOutputStream(DataManipulationUtil.getUserServerSocket(friend).getOutputStream()).writeUTF(inform.toString());
                } catch (IOException e) {
                    //好友断开连接
                    System.err.println(XenUtil.getCurrentTime() + " : " + friend+" 断开连接101");
                    doAfterLogout();
                }
            }
        }
    }

    public void depriveAuthorization(){
        this.authenticated = false;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                isLogout = true;
            }
        },5000);
    }
}
