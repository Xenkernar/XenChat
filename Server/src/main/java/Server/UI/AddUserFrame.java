package Server.UI;

import Server.XenUtil;

import javax.swing.*;

public class AddUserFrame extends JFrame {
    public AddUserFrame(JButton trigger){
        super("添加用户");
        setSize(260,180);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel();
        panel.setBounds(0,0,260,200);
        panel.setLayout(null);

        JLabel usernameLabel = new JLabel("用户名");
        JLabel passwordLabel = new JLabel("密码");
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton confirm = new JButton("添加");

        usernameLabel.setBounds(20,10,100,30);
        passwordLabel.setBounds(20,50,100,30);
        usernameField.setBounds(80,10,150,30);
        passwordField.setBounds(80,50,150,30);
        confirm.setBounds(80,100,100,30);
        confirm.addActionListener(e->{
            trigger.setText(
                    usernameField.getText() + "," + new String(passwordField.getPassword()) + "," + XenUtil.getCurrentTime()
            );
            trigger.doClick();
            usernameField.setText("");
            passwordField.setText("");
        });

        panel.add(usernameLabel);
        panel.add(passwordLabel);
        panel.add(usernameField);
        panel.add(passwordField);
        panel.add(confirm);

        add(panel);

    }

}
