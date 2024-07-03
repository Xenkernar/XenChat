package Server;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static Server.XenUtil.*;

public class FileSession implements Runnable {
    private Socket socket;
    private DataOutputStream outToClient;
    private DataInputStream inFromClient;

    private Socket userFileServerSocket;
    private DataInputStream inFromUserFileServer;
    private DataOutputStream outToUserFileServer;
    private String username;
    private boolean authenticated = true;
    public FileSession(Socket socket) throws IOException {
        this.socket = socket;
        outToClient = new DataOutputStream(socket.getOutputStream());
        inFromClient = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        while (authenticated){
            try {
                String jsonRequest = inFromClient.readUTF();
                String jsonResponse = generateResponse(jsonRequest).toString();
                if(!authenticated)return;
                outToClient.writeUTF(jsonResponse);
            } catch (IOException e) {
                return;
            }
        }
    }

    public JSONObject generateResponse(String jsonRequest) throws IOException {
        JSONObject jsonObject = new JSONObject(jsonRequest);
        int type = jsonObject.getInt("type");
        JSONObject response = new JSONObject();
        switch (type) {
            case REQUEST_LOGIN:
                username = jsonObject.getString("username");
                User user = DataManipulationUtil.getUser(username);
                connectToUserFileServer(user.getIP(),user.getFileServerPort());
                response.put("success",authenticated?1:0);
                DataManipulationUtil.addUserFileSession(username,this);
                if(!authenticated)break;
                new Thread(()->{
                    FileChunk fileChunk = DataManipulationUtil.getEarliestFileChunk(username);
                    while (fileChunk != null){
                        try {
                            JSONObject fileChunkJSON = new JSONObject();
                            fileChunkJSON.put("type",REQUEST_SEND_FILE);
                            fileChunkJSON.put("sender",fileChunk.getSender());
                            fileChunkJSON.put("receiver",fileChunk.getReceiver());
                            fileChunkJSON.put("fileMD5",fileChunk.getMD5());
                            fileChunkJSON.put("fileIndex",fileChunk.getChunkIndex());
                            fileChunkJSON.put("fileDataLength",fileChunk.getData().length);
                            outToUserFileServer.writeUTF(fileChunkJSON.toString());
                            outToUserFileServer.write(fileChunk.getData());
                            JSONObject responseFromUser = new JSONObject(inFromUserFileServer.readUTF());
                            if(responseFromUser.getInt("type") == RESPONSE_RECEIVED_ACK){
                                DataManipulationUtil.pollEarliestFileChunk(username);
                            }
                            fileChunk = DataManipulationUtil.getEarliestFileChunk(username);
                        } catch (IOException e) {
                            //用户断开连接
                            System.err.println(XenUtil.getCurrentTime() + " : " + username+" 的文件服务器断开连接");
                            authenticated = false;
                            break;
                        }
                    }
                }).start();
                break;
            case REQUEST_SEND_FILE://发送文件
                String sender = jsonObject.getString("sender");
                String receiver = jsonObject.getString("receiver");
                String fileMD5 = jsonObject.getString("fileMD5");
                int fileIndex = jsonObject.getInt("fileIndex");
                int fileDataLength = jsonObject.getInt("fileDataLength");
                byte[] fileData = inFromClient.readNBytes(fileDataLength);
                DataManipulationUtil.addFileChunk(sender,receiver,fileMD5,fileIndex,fileData);
                response.put("type",RESPONSE_RECEIVED_ACK);
                break;
        }
        return response;
    }
    public void connectToUserFileServer(String userIP,int fileServerPort){
        try {
            this.userFileServerSocket = new Socket(userIP,fileServerPort);
            this.outToUserFileServer = new DataOutputStream(userFileServerSocket.getOutputStream());
            this.inFromUserFileServer = new DataInputStream(userFileServerSocket.getInputStream());
            System.out.println(XenUtil.getCurrentTime() + " : " +"服务器连接到用户 "+ username+ " 的文件服务器");
        } catch (IOException e) {
            //无法连接到用户
            System.err.println(XenUtil.getCurrentTime() + " : " + username+" 无法连接到用户的文件服务器");
            authenticated = false;
        }
    }
    public void depriveAuthorization(){
        this.authenticated = false;
    }
}
