package yumefusaka.game.fish;

import javax.swing.*;
import javax.xml.transform.Result;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class Login implements ActionListener {
    private JDialog dialog;
    private JTextField jTextField;
    private JPasswordField passwordField;
    private JButton loginButton, exitButton,registerButton;
    private JLabel label, label2;

    Connection con;


    public Login() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fish_game", "root", "j");


        dialog = new JDialog((Frame) null, "Login", true); // 创建一个模态对话框
        dialog.setSize(400, 180);
        dialog.setLayout(new GridLayout(3, 2)); // 设置布局管理器

        // 添加登录表单的组件
        label = new JLabel("用户名");
        label2 = new JLabel("密码");
        jTextField = new JTextField(15);//设置文本框的长度
        passwordField = new JPasswordField(15);//设置密码框

        loginButton = new JButton("登录");
        loginButton.addActionListener(this);//监听事件
        exitButton = new JButton("退出");
        exitButton.addActionListener(this);//监听事件
        registerButton = new JButton("注册");
        registerButton.addActionListener(this);//监听事件


        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        dialog.add(label);
        dialog.add(jTextField);
        dialog.add(label2);
        dialog.add(passwordField);
        dialog.add(loginButton);
        dialog.add(exitButton);
        // dialog.add(registerButton);
        dialog.setVisible(true);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            // 验证用户名和密码...
            String account = jTextField.getText();
            String password = passwordField.getText();
            String user_select = "select * from user where account = '"+ account +"' and password = '"+password+"'" ;
            Statement stat = null;
            try {
                stat = con.createStatement();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            try {
                ResultSet rs = stat.executeQuery(user_select);
                if (rs!=null && rs.next()) {
                    JOptionPane.showMessageDialog(null,"登录成功！" );
                    Token.account=account;
                    dialog.dispose(); // 如果登录成功，关闭对话框
                }else {
                    JOptionPane.showMessageDialog(null, "用户名或密码错误！");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } else if (e.getSource() == exitButton) {
            System.exit(0); // 如果点击退出按钮，结束程序
        } else if (e.getSource()== registerButton){

        }
    }
}
