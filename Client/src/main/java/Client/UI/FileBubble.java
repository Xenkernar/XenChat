package Client.UI;


import Client.DataManipulationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileBubble extends Bubble {
    private String fileName;
    private JProgressBar progressBar;
    private JButton openButton;
    private int totalSize;
    private int downloadedSize;
    private Font font;
    private int charsNum;
    private String fileMD5;

    public FileBubble(String fileName, String MD5,int totalSize, String time){
        super(time);
        setLayout(null);
        int width = 85*Assets.sizeBase;
        int height = width/3;
        setSize(width,height);
        openButton = new JButton();
        openButton.setBounds(width- height*2/3,height/6,height/2,height/2);
        openButton.setForeground(new Color(0x8f1e00));
        openButton.setBackground(new Color(0xede4d3));
        openButton.setText("<html><font size = '"+Assets.sizeBase*2+"' >📂</font></html>");
        progressBar = new JProgressBar();
        progressBar.setBounds(height/6,height*19/24,width-height/3,height/12);
        progressBar.setBackground(new Color(0xede4d3));
        progressBar.setForeground(new Color(0x8f1e00));
        progressBar.setValue(0);
        progressBar.setMaximum(totalSize);
        add(openButton);
        add(progressBar);
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.downloadedSize = 0;
        this.fileMD5 = MD5;
        font = new Font("微软雅黑",Font.BOLD,getHeight()/6);
        calcParams();
        openButton.setEnabled(false);
    }

    public void flashProgress(int downloadedSize){
        this.downloadedSize =downloadedSize;
        progressBar.setValue(downloadedSize);
        repaint();
    }

    public int getTotalSize(){
        return totalSize;
    }
    public JButton getOpenButton(){
        return openButton;
    }
    public void setReceived(){
        progressBar.setValue(totalSize);
        this.downloadedSize = totalSize;
        repaint();
        openButton.setEnabled(true);
    }
    public void setSent(){
        progressBar.setValue(totalSize);
        this.downloadedSize = totalSize;
        repaint();
        openButton.setEnabled(true);

    }
    private void calcParams(){
        FontMetrics fontMetrics = getFontMetrics(font);
        int fileNameWidth = fontMetrics.charsWidth(fileName.toCharArray(), 0, fileName.length());
        int maxWidth = getWidth()-getHeight();
        String name = fileName;
        charsNum = fileName.length();
        if(fileNameWidth<=maxWidth){return;}
        while(fileNameWidth>maxWidth){
            name = name.substring(0,name.length()-1);
            fileNameWidth = fontMetrics.charsWidth(name.toCharArray(), 0, name.length());
        }
        charsNum = name.length()-2;
    }
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2d.setColor(new Color(0xffd299));
        g2d.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
        g2d.setColor(new Color(0x8f1e00));
        g2d.setFont(font);
        g2d.drawString(charsNum == fileName.length()?fileName:fileName.substring(0,charsNum)+"...",getHeight()/6,getHeight()/3);
        g2d.setFont(new Font("微软雅黑",Font.BOLD,getHeight()/8));
        g2d.drawString(String.format("%.2fMB / %.2fMB",1.0*downloadedSize/1024/1024,1.0*totalSize/1024/1024),getHeight()/6,getHeight()*2/3);
        super.paint(g);
    }
}
