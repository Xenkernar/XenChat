package Client.UI;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static Client.UI.Assets.*;
public class TextBubble extends Bubble {
    private String content;
    //params
    private int[] rowCapacitys;
    private int height;
    private int width;
    private int charHeight;

    public TextBubble(String content, String time){
        super(time);
        this.content = content;
        setFont(new Font("微软雅黑", Font.PLAIN, 4*sizeBase));
        calcParams();
        setSize(width,height);

    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2d.setColor(new Color(0xffd299));
        g2d.fillRoundRect(0,0,getWidth()-1,getHeight()-1,sizeBase*4,sizeBase*4);
        g2d.setColor(new Color(0x8f1e00));
        for(int i = 0;i < rowCapacitys.length; ++i){
            if(rowCapacitys[i] == 0){break;}
            int beginIndex = i == 0 ? 0 : rowCapacitys[i-1];
            int endIndex = rowCapacitys[i];
            g.drawString(content.substring(beginIndex,endIndex), sizeBase*3, sizeBase*2+charHeight+(charHeight+sizeBase)*i);
        }
    }
    private void calcParams(){
        FontMetrics fontMetrics = getFontMetrics(getFont());
        int strWidth = fontMetrics.charsWidth(content.toCharArray(),0,content.length());
        width = Math.min(strWidth+sizeBase*6,139*sizeBase);
        int rows = strWidth/(width-sizeBase*6)+2;
        rowCapacitys = new int[rows];
        int totalWidth = 0;
        int rowIndex = 0;
        for(int i = 0;i<content.length();i++){
            int currentCharWidth = fontMetrics.charWidth(content.charAt(i));
            totalWidth += currentCharWidth;
            if(totalWidth>width-sizeBase*6){
                rowCapacitys[rowIndex++] = i;
                totalWidth = currentCharWidth;
            }
            if(i == content.length()-1){
                rowCapacitys[rowIndex++] = i+1;
            }

        }
        charHeight = fontMetrics.getHeight();
        height = (charHeight+sizeBase)*rowIndex+sizeBase*6;

    }

}
