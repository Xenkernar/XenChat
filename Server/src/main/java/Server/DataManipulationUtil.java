package Server;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;


public class DataManipulationUtil {
    private static Connection connection;
    private static HashMap<String, Socket> userServerSockets = new HashMap<>();
    private static HashMap<String, Session> userSession = new HashMap<>();
    private static HashMap<String, FileSession> userFileSession = new HashMap<>();
    static{
        String url = "jdbc:mysql://localhost:3306/xenchatserver";
        String username = "root";
        String password = "root";
        // 建立数据库连接
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void setUserStatus(String username,int status){
        try {
            String query = "UPDATE user SET status = "+status+" WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setUserLastLoginTime(String username,String lastLoginTime){
        try {
            String query = "UPDATE user SET lastLoginTime = '"+lastLoginTime+"' WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setUserIP(String username,String IP){
        try {
            String query = "UPDATE user SET IP = '"+IP+"' WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setUserServerPort(String username,int serverPort){
        try {
            String query = "UPDATE user SET serverPort = "+serverPort+" WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void setUserFileServerPort(String username,int fileServerPort){
        try {
            String query = "UPDATE user SET fileServerPort = "+fileServerPort+" WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static User getUser(String username){
        User user = null;
        try {
            String query = "SELECT * FROM user WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()){
                String passwordMD5 = resultSet.getString("passwordMD5");
                int status = resultSet.getInt("status");
                String lastLoginTime = resultSet.getString("lastLoginTime");
                String IP = resultSet.getString("IP");
                int serverPort = resultSet.getInt("serverPort");
                int fileServerPort = resultSet.getInt("fileServerPort");
                user = new User(username,passwordMD5,status,lastLoginTime,IP,serverPort,fileServerPort);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public static void changePassword(String username,String newPasswordMD5){
        try {
            String query = "UPDATE user SET passwordMD5 = '"+newPasswordMD5+"' WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static ArrayList<String> getUserFriends(String username){
        ArrayList<String> friends = new ArrayList<>();
        try {
            String query = "SELECT friendName FROM friend WHERE username = '"+username+"'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                friends.add(resultSet.getString("friendName"));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friends;
    }

    public static ArrayList<User> getUsers(){
        ArrayList<User> users = new ArrayList<>();
        try {
            String query = "SELECT username,passwordMD5,status,lastLoginTime,IP,serverPort,fileServerPort FROM user";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                String username = resultSet.getString("username");
                String passwordMD5 = resultSet.getString("passwordMD5");
                int status = resultSet.getInt("status");
                String lastLoginTime = resultSet.getString("lastLoginTime");
                String IP = resultSet.getString("IP");
                int serverPort = resultSet.getInt("serverPort");
                int fileServerPort = resultSet.getInt("fileServerPort");
                users.add(new User(username,passwordMD5,status,lastLoginTime,IP,serverPort,fileServerPort));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
    //添加用户
    public static void addUser(String username,String passwordMD5,int status,String lastLoginTime,String IP,int serverPort,int fileServerPort){
        try {
            Statement statement = connection.createStatement();
            String query = "INSERT INTO user VALUES ('"+username+"','"+passwordMD5+"',"+status+",'"+lastLoginTime+"','"+IP+"',"+serverPort+","+fileServerPort+")";
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addFriend(String username,String friendName){
        try {
            Statement statement = connection.createStatement();
            String query = "INSERT INTO friend VALUES ('"+username+"','"+friendName+"')";
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addUserServerSocket(String username,Socket socket){
        userServerSockets.put(username,socket);
    }

    public static void  removeUserServerSocket(String username){
        userServerSockets.remove(username);
    }

    public static Socket getUserServerSocket(String username){
        return userServerSockets.get(username);
    }

    public static void addUserSession(String username,Session session){
        userSession.put(username,session);
    }

    public static void removeUserSession(String username){
        userSession.remove(username);
    }

    public static Session getSession(String username){
        return userSession.get(username);
    }

    public static void addUserFileSession(String username,FileSession fileSession){
        userFileSession.put(username,fileSession);
    }
    public static void removeUserFileSession(String username){
        userFileSession.remove(username);
    }
    public static FileSession getFileSession(String username){
        return userFileSession.get(username);
    }

    public static void addFileInfo(String sender,String receiver,String time,String fileName,String fileMD5,int fileLength){
        try {
            String query = "INSERT INTO FileInfos(sender,receiver,time,fileName,MD5,totalSize,sentSize,receivedSize) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? )";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,sender);
            statement.setString(2,receiver);
            statement.setString(3,time);
            statement.setString(4,fileName);
            statement.setString(5,fileMD5);
            statement.setInt(6,fileLength);
            statement.setInt(7,0);
            statement.setInt(8,0);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addFileChunk(String sender,String receiver,String fileMD5,int chunkIndex,byte[] chunkData){
        try {
            String query = "INSERT INTO FileChunks(sender,receiver,MD5,chunkIndex,data) VALUES ( ? , ? , ? , ? , ? )";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,sender);
            statement.setString(2,receiver);
            statement.setString(3,fileMD5);
            statement.setInt(4,chunkIndex);
            statement.setBytes(5,chunkData);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileChunk getEarliestFileChunk(String username){
        FileChunk fileChunk = null;
        try {
            //id最小且receiver为username的fileChunk
            String query = "SELECT sender,receiver,MD5,chunkIndex,data FROM FileChunks WHERE receiver = '"+username+"' ORDER BY id ASC LIMIT 1";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()){
                String sender = resultSet.getString("sender");
                String receiver = resultSet.getString("receiver");
                String MD5 = resultSet.getString("MD5");
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
                fileChunk = new FileChunk(sender,receiver,MD5,chunkIndex,data);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileChunk;
    }

    public static void pollEarliestFileChunk(String username){
        try {
            //id最小且receiver为username的fileChunk
            String query = "DELETE FROM FileChunks WHERE receiver = '"+username+"' ORDER BY id ASC LIMIT 1";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //删除用户
    public static void deleteUser(String username){
        try {
            Statement statement = connection.createStatement();
            String query = "DELETE FROM user WHERE username = '"+username+"'";
            statement.executeUpdate(query);
            query = "DELETE FROM friend WHERE username = '"+username+"' OR friendName = '"+username+"'";
            statement.executeUpdate(query);
            query = "DELETE FROM message WHERE receiver = '"+username+"'";
            statement.executeUpdate(query);
            query = "DELETE FROM message WHERE sender = '"+username+"'";
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    //判断用户是否存在
    public static boolean isUserExist(String username){
        boolean isExist = false;
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM user WHERE username = '"+username+"'";
            isExist = statement.executeQuery(query).next();

            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return isExist;
    }

    public static void addMessage(String sender,String receiver,String time,int type,byte[] data){
        try {
            String query = "INSERT INTO Message(sender,receiver,time,type,data) VALUES ( ? , ? , ? , ? , ?)";
            PreparedStatement  statement = connection.prepareStatement(query);
            InputStream inputStream = new ByteArrayInputStream(data);
            statement.setString(1,sender);
            statement.setString(2,receiver);
            statement.setString(3,time);
            statement.setInt(4,type);
            statement.setBlob(5,inputStream);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static LinkedList<Message> getMessages(String username){
        LinkedList<Message> messages = new LinkedList<>();
        try {
            String query = "SELECT sender,receiver,time,type,data FROM message WHERE receiver = '"+username+"' ORDER BY id ASC";
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

    public static Message getEarliestMessage(String username){
        Message message = null;
        try {
            //id最小且receiver为username的message
            String query = "SELECT sender,receiver,time,type,data FROM message WHERE receiver = '"+username+"' ORDER BY id ASC LIMIT 1";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()){
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
                message = new Message(sender,receiver,time,type,data);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return message;
    }
    public static void pollEarliestMessage(String username){
        try {
            //id最小且receiver为username的message
            String query = "DELETE FROM message WHERE receiver = '"+username+"' ORDER BY id ASC LIMIT 1";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void closeDatabaseConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
