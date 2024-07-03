package Server;

import java.util.ArrayList;
import java.util.HashSet;
import static Server.XenUtil.*;
public class User implements java.io.Serializable{
    private String username;
    private String passwordMD5;
    private int status;
    private String lastLoginTime;
    private String IP;
    private int serverPort;
    private int fileServerPort;

    public User(String username, String passwordMD5, int status, String lastLoginTime, String IP, int serverPort,int fileServerPort) {
        this.username = username;
        this.passwordMD5 = passwordMD5;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
        this.IP = IP;
        this.serverPort = serverPort;
        this.fileServerPort = fileServerPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordMD5() {
        return passwordMD5;
    }

    public int getStatus() {
        return status;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public String getIP() {
        return IP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getFileServerPort() {
        return fileServerPort;
    }

}
