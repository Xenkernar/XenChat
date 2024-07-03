package Server;

import java.awt.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class XenUtil {
    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_OFFLINE = 2;
    public static final int STATUS_BUSY = 3;
    public static final int STATUS_MUTE = 4;

    public static final int REQUEST_LOGIN = 1;
    public static final int REQUEST_SIGN_UP = 2;
    public static final int REQUEST_LOGOUT = 3;
    public static final int REQUEST_GET_STATUS = 4;
    public static final int REQUEST_GET_FRIEND_LIST = 5;
    public static final int REQUEST_GET_IP = 6;
    public static final int REQUEST_ADD_FRIEND = 7;
    public static final int REQUEST_SEND_TEXT = 8;
    public static final int REQUEST_SEND_FILE_INFO = 9;
    public static final int REQUEST_SEND_FILE = 10;
    public static final int REQUEST_CONNECT_TO = 11;
    public static final int REQUEST_CHANGE_PASSWORD = 23;

    public static final int RESPONSE_RECEIVED_ACK = 12;
    public static final int REQUEST_FRIEND_APPLY_ACK = 13;

    public static final int INFORM_UPDATE_FRIENDS_AREA = 15;

    public static final int INFORM_REMOTE_LOGIN = 17;

    private XenUtil(){}
    public static Font getSpecificFont(int size, String fontFilePath){
        InputStream is = null;
        Font baseTTF = null;
        try {
            is = new BufferedInputStream(new FileInputStream(fontFilePath));
            baseTTF = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        }
        return baseTTF.deriveFont(Font.PLAIN,size);
    }
    public static String getCurrentTime(){
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentDate);
    }
    public static boolean timeSpanOut(String time1,String time2,int spanMins){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = dateFormat.parse(time1);
            Date date2 = dateFormat.parse(time2);
            long span = date2.getTime()-date1.getTime();
            return span > spanMins*60*1000;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;

    }
    public static String encryptMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStringOfStatus(int status){
        switch(status){
            case STATUS_ONLINE:
                return "在线";
            case STATUS_OFFLINE:
                return "离线";
            case STATUS_BUSY:
                return "忙碌";
            case STATUS_MUTE:
                return "禁言";
            default:
                return "未知";
        }
    }


}
