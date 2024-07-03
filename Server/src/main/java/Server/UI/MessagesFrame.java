package Server.UI;

import Server.Message;
import Server.XenUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedList;

public class MessagesFrame extends JFrame {
    private static final int sizeBase = 1;
    public MessagesFrame(LinkedList<Message> messages){
        setTitle("消息记录 -- 截至  " + XenUtil.getCurrentTime() + "  共有"+messages.size()+"条消息");
        setSize(500,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JTable table =  new JTable(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        DefaultTableModel model = new DefaultTableModel();
        table.setModel(model);
        JScrollPane tableScrollPane = new JScrollPane(table);
        table.setFont(new Font("微软雅黑",Font.PLAIN,15*sizeBase));
        model.setColumnIdentifiers(new Object[]{"接收者","发送者","消息类型","内容"});
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        messages.forEach(message -> {
            model.addRow(new Object[]{message.getReceiver(),message.getSender(),message.getType(),new String(message.getData())});
        });
        add(tableScrollPane);


    }
}
