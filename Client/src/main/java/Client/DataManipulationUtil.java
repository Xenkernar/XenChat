package Client;

import Client.UI.LoggedInFrame;
import Client.UI.XenUtil;
import org.json.JSONObject;

import javax.swing.*;

import static Client.UI.XenUtil.*;
import static Client.Configuration.*;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class DataManipulationUtil {
    private static Connection connection;
    static{
        try {
            connection = DriverManager.getConnection(databaseURL, databaseUsername,databasePassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<Message> getFriendApplyFor(String receiver){
        ArrayList<Message> messages = new ArrayList<>();
        try {
            String query = "SELECT sender,time FROM message WHERE receiver = '"+receiver+"' AND type = "+REQUEST_ADD_FRIEND;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                String sender = resultSet.getString("sender");
                String time = resultSet.getString("time");
                messages.add(new Message(sender,receiver,time,REQUEST_ADD_FRIEND,"".getBytes()));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }

    public static void dropLetter(String sender,String receiver){
        try {
            String query = "DELETE FROM message WHERE  sender = '"+sender+"' AND receiver = '"+receiver+"' AND type = "+REQUEST_ADD_FRIEND;
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Message> getConverseRecord(String username,String friendName){
        ArrayList<Message> messages = new ArrayList<>();
        try {
            String query = "SELECT sender,receiver,time,type,data FROM message WHERE username = '" +username + "' AND ((sender = '"+username+"' AND receiver = '"+friendName+"') OR (sender = '"+friendName+"' AND receiver = '"+username+"')) AND NOT (type = 7) ORDER BY id ASC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                String sender = resultSet.getString("sender");
                String receiver = resultSet.getString("receiver");
                String time = resultSet.getString("time");
                int type = resultSet.getInt("type");
                Blob dataBlob = resultSet.getBlob("data");
                InputStream inputStream = dataBlob.getBinaryStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] data = outputStream.toByteArray();
                messages.add(new Message(sender,receiver,time,type,data));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messages;

    }

    public static void addMessage(String username,Message message){
        try {
            String query = "INSERT INTO message(username,sender,receiver,time,type,data) VALUES ( ? , ? , ? , ? , ? , ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            InputStream inputStream = new ByteArrayInputStream(message.getData());
            statement.setString(1,username);
            statement.setString(2,message.getSender());
            statement.setString(3,message.getReceiver());
            statement.setString(4,message.getTime());
            statement.setInt(5,message.getType());
            statement.setBlob(6,inputStream);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteMessage(String username,Message message){
        try {
            String query = "DELETE FROM message WHERE username = ? AND sender = ? AND receiver = ? AND time = ? AND type = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,message.getSender());
            statement.setString(3,message.getReceiver());
            statement.setString(4,message.getTime());
            statement.setInt(5,message.getType());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addSendFileInfo(String username,String receiver,String time,String fileName,String fileMD5,int fileLength){
        try {
            String query = "INSERT INTO SendFileInfo(username,receiver,time,fileName,MD5,totalSize,sentSize) VALUES ( ? , ? , ? , ? , ? , ? , ? )";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,receiver);
            statement.setString(3,time);
            statement.setString(4,fileName);
            statement.setString(5,fileMD5);
            statement.setInt(6,fileLength);
            statement.setInt(7,0);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void increaseSendFileSize(String username,String receiver,String fileMD5){
        try {
            String query = "UPDATE SendFileInfo SET sentSize = sentSize + ? WHERE MD5 = ? AND username = ? AND receiver = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,CHUNK_SIZE);
            statement.setString(2,fileMD5);
            statement.setString(3,username);
            statement.setString(4,receiver);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static int getSentFileProgress(String username,String receiver,String fileMD5){
        int sentSize = 0;
        try {
            String query = "SELECT sentSize FROM SendFileInfo WHERE MD5 = ? AND username = ? AND receiver = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,fileMD5);
            statement.setString(2,username);
            statement.setString(3,receiver);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                sentSize = resultSet.getInt("sentSize");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return  sentSize;
    }


    public static void addReceiveFileInfo(String username,String sender,String time,String fileName,String fileMD5,int fileLength){
        try {
            String query = "INSERT INTO ReceiveFileInfo(username,sender,time,fileName,MD5,totalSize,receivedSize) VALUES ( ? , ? , ? , ? , ? , ? , ? )";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,sender);
            statement.setString(3,time);
            statement.setString(4,fileName);
            statement.setString(5,fileMD5);
            statement.setInt(6,fileLength);
            statement.setInt(7,CHUNK_SIZE);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void increaseReceiveFileSize(String username,String sender,String fileMD5){
        try {
            String query = "UPDATE ReceiveFileInfo SET receivedSize = receivedSize + ? WHERE MD5 = ? AND username = ? AND sender = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,CHUNK_SIZE);
            statement.setString(2,fileMD5);
            statement.setString(3,username);
            statement.setString(4,sender);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static int getReceivedFileProgress(String username,String sender,String fileMD5){
        int receivedSize = 0;
        try {
            String query = "SELECT receivedSize FROM ReceiveFileInfo WHERE MD5 = ? AND username = ? AND sender = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,fileMD5);
            statement.setString(2,username);
            statement.setString(3,sender);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                receivedSize = resultSet.getInt("receivedSize");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return  receivedSize;
    }


    public static void addReceiveFileChunk(String sender,String receiver,String fileMD5,int chunkIndex,byte[] chunkData){
        try {
            String query = "INSERT INTO ReceiveFiles(username,sender,MD5,chunkIndex,data) VALUES ( ? , ? , ? , ? , ? )";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,receiver);
            statement.setString(2,sender);
            statement.setString(3,fileMD5);
            statement.setInt(4,chunkIndex);
            statement.setBytes(5,chunkData);
            statement.executeUpdate();
            increaseReceiveFileSize(receiver,sender,fileMD5);
            statement.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void deleteReceiveFileChunks(String username,String sender,String MD5){
        try {
            String query = "DELETE FROM ReceiveFiles WHERE username = ? AND sender = ? AND MD5 = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,sender);
            statement.setString(3,MD5);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public static FileChunk getEarliestSendFileChunk(String username,String receiver,String MD5){
        FileChunk fileChunk = null;
        try {
            String query = "SELECT chunkIndex,data FROM SendFiles WHERE username = '"+username+"' AND receiver = '"+receiver+"' AND MD5 = '"+MD5+"' ORDER BY chunkIndex ASC LIMIT 1";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()){
                int chunkIndex = resultSet.getInt("chunkIndex");
                Blob blobData = resultSet.getBlob("data");
                InputStream inputStream = blobData.getBinaryStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] data = outputStream.toByteArray();
                fileChunk = new FileChunk(username,receiver,MD5,chunkIndex,data);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileChunk;
    } //？
    public static void pollEarliestSendFileChunk(String username,String receiver,String MD5){
        try {
            String query = "DELETE FROM SendFiles WHERE username = '"+username+"' AND receiver = '"+receiver+"' AND MD5 = '"+MD5+"' ORDER BY chunkIndex ASC LIMIT 1";
            increaseSendFileSize(username,receiver,MD5);
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }   //？

    public static void breakDownFileToDatabase(String username,String receiver ,File file,String MD5){
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead = 0;
        int chunkIndex = 0;
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            String sql = "INSERT INTO SendFiles(username,receiver, MD5,chunkIndex,data) VALUES (?, ?,?,? ,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                statement.setString(1, username);
                statement.setString(2,receiver);
                statement.setString(3, MD5);
                statement.setInt(4, chunkIndex);
                statement.setBytes(5, buffer);
                statement.executeUpdate();
                chunkIndex++;
            }
        }
        catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void mergeFileFromDatabase(String MD5,String saveFileName){
        try (Statement statement = connection.createStatement();
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(saveFileName))) {
            ResultSet resultSet = statement.executeQuery("SELECT data FROM ReceiveFiles WHERE  MD5 = '"+MD5+"' ORDER BY chunkIndex ASC");
            while (resultSet.next()) {
                byte[] buffer = resultSet.getBytes("data");
                outputStream.write(buffer);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void openReceivedFile(String username,String sender,String MD5){
        String fileName = null;
        try {
            String query = "SELECT fileName FROM ReceiveFileInfo WHERE MD5 = ? AND sender = ? AND username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,MD5);
            statement.setString(2,sender);
            statement.setString(3,username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                fileName = resultSet.getString("fileName");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        fileName = defaultFileSavePath + "\\" + fileName;
        try {
            java.awt.Desktop.getDesktop().open(new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isMerged(String username,String sender,String MD5){
        boolean isMerged = false;
        try {
            String query = "SELECT count(*) FROM ReceiveFiles WHERE MD5 = ? AND sender = ? AND username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,MD5);
            statement.setString(2,sender);
            statement.setString(3,username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                int count = resultSet.getInt(1);
                isMerged = count == 0;
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return isMerged;
    }

    public static void closeDatabaseConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
