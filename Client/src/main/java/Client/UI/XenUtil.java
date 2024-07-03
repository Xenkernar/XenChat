package Client.UI;


import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.sound.sampled.AudioFormat;
import java.awt.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class XenUtil {
    public static final String ABADIMTSTD = "src/main/resources/AbadiMTStd-ExtraBold.otf";
    public static final String ASTERX = "src/main/resources/Asterx-Regular.ttf";
    public static final String ASTERIX = "src/main/resources/Asterix.ttf";
    public static final String ANATEVKA = "src/main/resources/Anatevka.otf";
    public static final String CAI978 = "src/main/resources/204-CAI978.ttf";
    public static final Color BUBBLE_TEXT_COLOR = new Color(0xe6c3ff);
    public static final Color SCROLLABLE_AREA_BACKGROUND_COLOR = new Color(0xF7EEDD);

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


    public static final int REQUEST_VOICE_COMM = 18;
    public static final int REQUEST_VIDEO_COMM = 19;
    public static final int INFORM_STOP_VOICE_COMM = 20;
    public static final int INFORM_STOP_VIDEO_COMM = 21;

    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_OFFLINE = 2;
    public static final int STATUS_BUSY = 3;
    public static final int STATUS_MUTE = 4;

    public static final int CHUNK_SIZE = 1024*64-1;//65536Bytes



    public static AudioFormat AUDIO_FORMAT = new AudioFormat(8000.0f, 16, 1, true, true);


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

    public static String getFileMD5(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
            fis.close();

            byte[] md5Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                sb.append(Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] matToByte(Mat mat){
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",mat,matOfByte);
        return matOfByte.toArray();
    }
    public static Mat bytesToMat(byte[] bytes){
        MatOfByte matIntOfByte = new MatOfByte(bytes);
        return Imgcodecs.imdecode(matIntOfByte, Imgcodecs.IMREAD_UNCHANGED);
    }

}
