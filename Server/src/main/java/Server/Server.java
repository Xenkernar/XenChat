package Server;



import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.net.*;

public class Server implements Runnable{
    private int port;
    private ServerSocket serverSocket;
    public Server(int port){
        this.port = port;
    }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            Socket connectionSocket = null;
            try {
                connectionSocket = serverSocket.accept();//如果监听到了
            } catch (IOException e) {
                System.out.println("服务器已关闭");
                break;
            }
            Socket finalConnectionSocket = connectionSocket;
            try {
                new Thread(new Session(finalConnectionSocket)).start();//创建一个线程来处理客户端的请求
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void stop(){
        try {

            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
