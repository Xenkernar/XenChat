package Client;

import Client.UI.LoggedInFrame;
import Client.UI.LoginFrame;
import Client.UI.XenUtil;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import static Client.UI.XenUtil.*;
public class FileSession implements Runnable{
    private Socket socket;
    private DataOutputStream outTo;
    private DataInputStream inFrom;
    private String username;
    private boolean isOnlien = true;
    private LoggedInFrame clientFrame;
    public FileSession(Socket socket) throws IOException {
        this.socket = socket;
        outTo = new DataOutputStream(socket.getOutputStream());
        inFrom = new DataInputStream(socket.getInputStream());
        this.clientFrame = Main.loggedInFrameReference.get();
    }

    @Override
    public void run() {
        while (isOnlien){
            try {
                String jsonRequest = inFrom.readUTF();
                String jsonResponse = generateResponse(jsonRequest).toString();
                outTo.writeUTF(jsonResponse);
            } catch (IOException e) {
                isOnlien = false;
            }
        }
    }

    public JSONObject generateResponse(String jsonRequest) throws IOException {
        JSONObject jsonObject = new JSONObject(jsonRequest);
        int type = jsonObject.getInt("type");
        JSONObject response = new JSONObject();
        switch (type) {
            case REQUEST_SEND_FILE://发送文件
                String sender = jsonObject.getString("sender");
                String receiver = jsonObject.getString("receiver");
                String fileMD5 = jsonObject.getString("fileMD5");
                int fileIndex = jsonObject.getInt("fileIndex");
                int fileDataLength = jsonObject.getInt("fileDataLength");
                byte[] fileData = inFrom.readNBytes(fileDataLength);
                DataManipulationUtil.addReceiveFileChunk(sender,receiver,fileMD5,fileIndex,fileData);
                response.put("type",RESPONSE_RECEIVED_ACK);
                break;
        }
        return response;
    }
}
