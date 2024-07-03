package Client.UI;

import javax.swing.*;

public class FilePresentation extends Presentation{
    private FileBubble fileBubble;
    private String MD5;
    private String fileName;
    public FilePresentation(String username,String MD5,int fileSize,String fileName, boolean leftAlignment, String time){
        super(username);
        this.MD5 = MD5;
        this.fileName = fileName;
        fileBubble = new FileBubble(fileName,MD5,fileSize,time);
        bubble = fileBubble;
        putComponent(leftAlignment);
    }
    public void flashProgress(int downloadedSize){
        fileBubble.flashProgress(downloadedSize);
    }
    public String getMD5(){
        return MD5;
    }
    public String getFileName(){
        return fileName;
    }
    public int getTotalSize(){
        return fileBubble.getTotalSize();
    }
    public void setReceived(){
        fileBubble.setReceived();
    }
    public void setSent(){
        fileBubble.setSent();
    }
    public void setSendIcon(){
        fileBubble.getOpenButton().setText("<html><font size = '"+Assets.sizeBase*2+"' >✔</font></html>");
    }
    public JButton getOpenButton(){
        return fileBubble.getOpenButton();
    }
}
