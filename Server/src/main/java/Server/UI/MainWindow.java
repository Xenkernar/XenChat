package Server.UI;

import Server.Server;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.TimerTask;

import Server.*;
import static Server.XenUtil.*;

public class MainWindow extends JFrame {
    private static final int sizeBase = 1;
    private JTable userTable;
    private DefaultTableModel model;
    private Server server;
    private FileServer fileServer;
    public MainWindow(){
        super("Server");
        server = new Server(20023);
        fileServer = new FileServer(20024);
        setSize(735*sizeBase,500*sizeBase);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLayout(null);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel();
        panel.setSize(735*sizeBase,500*sizeBase);
        panel.setLayout(null);
        //用户表格
        userTable = new JTable(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable.addMouseListener(new DoubleClickMouseListener());
        model = new DefaultTableModel();
        userTable.setModel(model);
        JScrollPane userTableScrollPane = new JScrollPane(userTable);
        userTableScrollPane.setBounds(10*sizeBase,10*sizeBase,600*sizeBase,445*sizeBase);
        userTable.setFont(new Font("微软雅黑",Font.PLAIN,15*sizeBase));
        model.setColumnIdentifiers(new Object[]{"用户名","状态","IP地址","服务端口","文件服务端口","最近上线时间"});
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < userTable.getColumnCount(); i++) {
            userTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        userTable.getColumnModel().getColumn(5).setPreferredWidth(200*sizeBase);

        String[] functions = new String[]{"添加用户","注销用户","禁言用户","启动服务器","关闭服务器"};
        JButton buttons[] = new JButton[functions.length];
        for (int i = 0; i < functions.length; i++) {
            buttons[i] = new JButton(functions[i]);
            buttons[i].setBounds(620*sizeBase,10*sizeBase+i*50*sizeBase,100*sizeBase,40*sizeBase);
            panel.add(buttons[i]);
        }
        panel.add(userTableScrollPane);
        add(panel);
        renderTable();
        repaint();
        JButton trigger = new JButton();
        trigger.addActionListener(e->{
            String[] params = trigger.getText().split(",");
            if(DataManipulationUtil.isUserExist(params[0])){
                JOptionPane.showMessageDialog(null,"用户已存在");
                return;
            }
            DataManipulationUtil.addUser(params[0],encryptMD5(params[1]),STATUS_OFFLINE,params[2],"",-1,-1);
            model.addRow(new Object[]{params[0],"离线","Null","Null","Null",params[2]});
        });
        AddUserFrame addUserFrame = new AddUserFrame(trigger);
        buttons[0].addActionListener(e->{
            addUserFrame.setVisible(true);
        });

        buttons[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = userTable.getSelectedRow();
                if(row == -1){
                    JOptionPane.showMessageDialog(null,"请先选择用户");
                    return;
                }
                String username = (String) model.getValueAt(row,0);
                DataManipulationUtil.deleteUser(username);
                model.removeRow(row);
            }
        });
        buttons[4].setEnabled(false);
        buttons[3].addActionListener(e->{
            new Thread(server).start();
            new Thread(fileServer).start();
            buttons[3].setEnabled(false);
            buttons[4].setEnabled(true);
            buttons[3].setText("服务已启动");
        });
        buttons[4].addActionListener(e->{
            server.stop();
            fileServer.stop();
            buttons[3].setEnabled(true);
            buttons[4].setEnabled(false);
            buttons[3].setText("启动服务器");

        });
        new Timer(2000, e->{
            model.setRowCount(0);
            renderTable();
        }).start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DataManipulationUtil.closeDatabaseConnection();
                dispose();
                System.exit(0);
            }
        });
    }
    private void renderTable(){
        DataManipulationUtil.getUsers().forEach(user -> {
            model.addRow(new Object[]{
                    user.getUsername(),
                    getStringOfStatus(user.getStatus()),
                    user.getIP().isEmpty() ?"Null":user.getIP(),
                    user.getServerPort() == -1?"Null":user.getServerPort(),
                    user.getFileServerPort() == -1?"Null":user.getFileServerPort(),
                    user.getLastLoginTime()
            });
        });
    }


}
