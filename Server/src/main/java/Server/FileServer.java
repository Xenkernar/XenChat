package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer implements Runnable{
    private int fileServerPort;
    private ServerSocket serverSocket;
    public FileServer(int fileServerPort){
        this.fileServerPort = fileServerPort;
    }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(fileServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            Socket connectionSocket = null;
            try {
                connectionSocket = serverSocket.accept();//如果监听到了
            } catch (IOException e) {
                System.out.println("文件服务器已关闭");
                break;
            }
            Socket finalConnectionSocket = connectionSocket;
            try {
                new Thread(new FileSession(finalConnectionSocket)).start();//创建一个线程来处理客户端的请求
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
