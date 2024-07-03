package Client.UI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


import static Client.UI.Assets.*;

public class ScrollableArea extends JComponent {
    private ArrayList<JComponent> components;
    private ComponentArea componentArea;
    private JScrollBar scrollBar;
    private class ComponentArea extends JComponent {
        private int maxHeight;
        private int currentHeight;
        private int initialHeight;
        public ComponentArea(int width,int height){
            setBounds(0,0,width,height);
            this.initialHeight = height;
            this.maxHeight = height;
            currentHeight = 0;
            setLayout(null);
        }

        public void pushComponent(JComponent component){
            if(currentHeight+component.getHeight()>maxHeight){
                dilate();
            }
            component.setLocation(0,currentHeight);
            add(component);
            currentHeight += component.getHeight();
            repaint();
        }

        private void dilate(){
            setSize(getWidth(),getHeight()*2);
            maxHeight = getHeight();
        }


        @Override
        public void paint(Graphics g) {
            g.setColor(XenUtil.SCROLLABLE_AREA_BACKGROUND_COLOR);
            g.fillRect(0,0,getWidth(),getHeight());
            super.paint(g);
        }
    }
    public ScrollableArea(int x,int y,int width,int height){
        setBounds(x,y,width,height);
        components = new ArrayList<>();
        componentArea = new ComponentArea(width,height);
        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        scrollBar.setBounds(width-3*sizeBase,0,3*sizeBase,height);
        //scrollBar.setBackground(Server.XenUtil.SCROLLABLE_AREA_BACKGROUND_COLOR);
        scrollBar.setForeground(XenUtil.BUBBLE_TEXT_COLOR);
        scrollBar.setMaximum(0);
        scrollBar.addAdjustmentListener(e->{
            componentArea.setLocation(0,-scrollBar.getValue());
        });
        add(scrollBar);
        add(componentArea);
    }

    public void pushComponent(JComponent component){
        componentArea.pushComponent(component);
        components.add(component);
        if(componentArea.currentHeight<getHeight())return;
        scrollBar.setMaximum(componentArea.currentHeight-getHeight());
        //scrollBar.setVisibleAmount(10*sizeBase);
        scrollBar.setValue(scrollBar.getMaximum());
    }
    public void removeComponent(JComponent component){
        remove(componentArea);
        scrollBar.removeAdjustmentListener(scrollBar.getAdjustmentListeners()[0]);

        components.remove(component);
        componentArea = new ComponentArea(getWidth(),getHeight());
        scrollBar.addAdjustmentListener(e->{
            componentArea.setLocation(0,-scrollBar.getValue());
        });
        add(componentArea);
        for (JComponent c : components) {
            componentArea.pushComponent(c);
        }
    }

    public JScrollBar getScrollBar(){
        return scrollBar;
    }

    public ArrayList<JComponent> getItems(){
        return components;
    }

    public void scrollTo(int componentIndex){
        scrollBar.setValue(components.get(componentIndex).getY());
    }
}
