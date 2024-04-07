package yumefusaka.game.fish;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FishGame {

    public static void main(String[] args) throws ClassNotFoundException, SQLException{
        if(Token.account==null)
            new Login();

        String account = Token.account;

        //游戏画框
        JFrame jf = new JFrame("捕鱼达人");
        jf.setSize(800, 480);
        jf.setLocationRelativeTo(null);
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setAlwaysOnTop(true);



        //1、导入驱动jar包
        //2、注册驱动
        Class.forName("com.mysql.cj.jdbc.Driver");

        //3、获取数据库的连接对象
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fish_game", "root", "j");

        //4、定义sql语句
        //String user_sql = "select * from user where ";
        String coin_select = "select num from coin where account = '" + account + "'";

        //5、获取执行sql语句的对象
        Statement stat = con.createStatement();

        //6、执行sql并接收返回结果
        ResultSet rs = stat.executeQuery(coin_select);
        rs.next();
        int num = rs.getInt("num");

        //8、释放资源
        stat.close();
        con.close();

        //加载鱼池
        Pool pool = new Pool(num,account,jf);
        pool.setLayout(null);
        jf.getContentPane().add(pool);


        //显示框架
        jf.setVisible(true);
        pool.action();
    }
}

//鱼池类
class Pool extends JPanel{
    String account="";
    int coin = 0;
    JFrame jf;
    private static final long serialVersionUID = 1L;
    BufferedImage bgImage;    //背景图片
    Fish[] fishs = new Fish[20];    //所有的鱼
    Net net = new Net();    //渔网
    boolean isExit;    //鼠标是否在游戏界面

    Connection con;

    public Pool(int coin,String account,JFrame jFrame) throws ClassNotFoundException, SQLException {
        super();
        this.jf = jFrame;
        this.coin = coin;
        this.account = account;
        //1、导入驱动jar包
        //2、注册驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        //3、获取数据库的连接对象
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fish_game", "root", "j");
        File bg = new File("images/bg.jpg");
        try {
            bgImage = ImageIO.read(bg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //0-8,9-17对应1-9号鱼
        for (int i = 0; i < fishs.length / 2 - 1; i++) {
            fishs[i] = new Fish(i + 1);
            fishs[i + 9] = new Fish(i + 1);
        }
        fishs[18] = new Fish(10);
        fishs[19] = new Fish(11);

        ImageIcon icon = new ImageIcon("images/shangdian.png");
        ImageIcon finalIcon = icon;
        JButton shop = new JButton(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(finalIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        icon = new ImageIcon("images/chongzhi.png");
        ImageIcon finalIcon1 = icon;
        JButton recharge = new JButton(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(finalIcon1.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        this.add(shop);
        this.add(recharge);
        shop.setBounds(710,380,50,50);
        recharge.setBounds(630,380,50,50);

        shop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Button clicked!");
            }
        });

        recharge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recharge();
            }
        });

    }

    //画游戏元素
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgImage, 0, 0, null);    //画背景
        for (Fish fish : fishs) {
            g.drawImage(fish.fishImage, fish.fish_x, fish.fish_y, null);    //画鱼
        }
        if (!isExit) {
            g.drawImage(net.netImage, net.netX, net.netY, null);    //画网
        }

        //画游戏说明文字
        g.setColor(Color.PINK);
        g.setFont(new Font("宋体", Font.BOLD, 18));
        g.drawString("捕鱼达人", 10, 25);
        g.drawString("用户名:" + account, 150, 25);
        g.drawString("金币数:" + coin , 330, 25);
        g.drawString("右键切换渔网    渔网等级:" + (net.power % 7 + 1), 480, 25);
        if (coin <= 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("宋体", Font.PLAIN, 100));
            g.drawString("Game Over", 150, 250);
            coin = 0;
            String coin_update = "update coin set num = 0 where account = '" + account + "'";
            try {
                Statement stat = con.createStatement();
                stat.executeUpdate(coin_update);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            isExit = true;
            net.power = -1;
        }
    }

    //游戏启动方法
    public void action() {
        for (Fish fish : fishs) {
            fish.start();
        }
        //鼠标监听器
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int event = e.getModifiers();
                if (event == 4) {
                    net.change();    //切换网大小
                    super.mousePressed(e);
                } else if (event == 16) {
                    //减子弹
                    if (coin - (net.power % 7 + 1) <= 0) {
                        coin = 0;
                        recharge();
                    } else {
                        coin -= (net.power % 7 + 1);
                    }
                    String coin_update = "update coin set num = " + coin +" where account = '" + account + "'";
                    try {
                        Statement stat = con.createStatement();
                        stat.executeUpdate(coin_update);
                    } catch (SQLException e1) {
                        throw new RuntimeException(e1);
                    }
                    //捕鱼
                    for (Fish fish : fishs) {
                        if (!fish.catched) {
                            catchFish(fish);
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                net.moveTo(e.getX(), e.getY());
                super.mouseMoved(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isExit = false;
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isExit = true;
                super.mouseExited(e);
            }
        };
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
        while (true) {
            repaint();
        }
    }

    //封装捕鱼的方法
    public void catchFish(Fish fish) {
        fish.catched = net.catchFish(fish);
        if (fish.catched) {
            coin += 2 * fish.k;
            String coin_update = "update coin set num = " + coin +" where account = '" + account + "'";
            try {
                Statement stat = con.createStatement();
                stat.executeUpdate(coin_update);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    int reCoin = 0;
    int[] val= new int[]{0, 50, 100, 500, 1000, 2000, 5000};
    JButton []b = new JButton[7];
    JButton complete;
    JLabel label3;
    JDialog jd;
    //充值的方法
    public void recharge(){
        jd = new JDialog(jf,"充值",true);
        jd.setLocationRelativeTo(null);
        jd.setLayout(new GridLayout(2, 1));
        jd.setSize(600,600);
        JPanel jp = new JPanel();
        jp.setLayout(null);
        ImageIcon icon = new ImageIcon("images/jinbiduihuan1.png");
        JLabel label;
        for(int i=1;i <= 6; i++){
            int row=i/4,col=i-row*3;
            b[i]=new JButton(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            };
            label= new JLabel("    "+val[i]+"金币");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setBackground(new Color(0, 0, 255, 125)); // RGBA values
            // 创建一个新的字体对象
            Font font = new Font("宋体", Font.BOLD, 20);
            // 使用setFont方法设置JLabel的字体
            label.setFont(font);
            label.setForeground(Color.WHITE);
            b[i].add(label);
            int finalI = i;
            b[i].addActionListener(e->{
                reCoin = val[finalI];
                label3.setText("    您将支付:"+ reCoin +"人民币");
            });
            jp.add(b[i]);
            b[i].setBounds(180*(col-1)+col*15,120*row+20*(row+1),180,120);
            
        }
        complete = new JButton(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, Color.RED, width, height, Color.BLUE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };

        label = new JLabel("       已完成支付");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        // 创建一个新的字体对象
        Font font = new Font("宋体", Font.BOLD, 20);
        // 使用setFont方法设置JLabel的字体
        label.setFont(font);
        label.setForeground(Color.WHITE);

        complete.addActionListener(e->{
            JDialog over =new JDialog(jf);
            over.setLocationRelativeTo(null);
            JLabel text = new JLabel("您已成功充值"+reCoin+"金币,请继续游玩");
            over.setSize(250,100);
            coin=reCoin;
            reCoin=0;
            String coin_update = "update coin set num = "+coin+" where account = '" + account + "'";
            Statement stat = null;
            try {
                stat = con.createStatement();
                stat.executeUpdate(coin_update);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            over.add(text);
            over.setVisible(true);
            jd.dispose();
        });
        complete.add(label);
        jd.add(jp);
        jp.setBounds(0,0,300,300);
        JPanel jp2 = new JPanel();
        jp2.setLayout(new GridLayout(1,2));
        label = new JLabel();
        label.setIcon(new ImageIcon("images/666.png"));//文件路径
        jp2.add(label);
        JPanel jp3 =new JPanel();
        jp3.setLayout(new GridLayout(3,1));
        JLabel label2 = new JLabel();
        label2.setText("    请扫码左侧二维码付款 (10金币=1人民币)");
        label3 = new JLabel();
        label3.setText("    您将支付:"+ reCoin +"人民币");
        jp3.add(label2);
        jp3.add(label3);
        jp3.add(complete);
        jp2.add(jp3);
        jd.add(jp2);
        jp2.setBounds(0,300,300,300);
        jd.setVisible(true);
    }

}

//鱼类
class Fish extends Thread {
    int fish_x, fish_y;    //鱼的坐标
    BufferedImage fishImage;    //鱼的图片
    BufferedImage[] fishImages = new BufferedImage[10];    //鱼动作的图片
    BufferedImage[] catchImages;    //鱼的被捕的图片
    int fish_width, fish_height;    //鱼的宽高
    Random r = new Random();    //鱼y坐标的随机数
    int blood;    //鱼的血量值
    boolean catched;    //鱼是否被捕
    int k, step_size;    //鱼的血量等级,移动速度

    public Fish(int m) {
        super();
        String preName = m > 9 ? m + "" : "0" + m;
        //通过for循环读取鱼动作图片数组
        for (int i = 0; i < fishImages.length; i++) {
            int j = i + 1;
            String lastName = j > 9 ? "10" : "0" + j;
            File file = new File("images/fish" + preName + "_" + lastName + ".png");
            try {
                fishImages[i] = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fishImage = fishImages[0];
        fish_width = this.fishImage.getWidth();
        fish_height = this.fishImage.getHeight();
        fish_x = 800;
        fish_y = r.nextInt(450 - fish_height);
        blood = m * 3;
        this.k = m;
        step_size = r.nextInt(5) + 1;
        //初始化catchImages
        if (m > 7) {
            catchImages = new BufferedImage[4];
        } else if (m <= 7) {
            catchImages = new BufferedImage[2];
        }
        //通过for循环读取鱼被捕图片数组
        for (int i = 1; i <= catchImages.length; i++) {
            File file = new File("images/fish" + preName + "_catch_0" + i + ".png");
            try {
                catchImages[i - 1] = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //鱼移动的方法
    public void move() {
        fish_x -= step_size;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            move();    //调用鱼移动的方法
            //如果鱼出界,重新生成
            if (fish_x < -fish_width || catched) {
                turnOut();    //鱼被捕,颤动
                newFish();
            }
            change();    //调用鱼摇摆游动的方法
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //重新生成一条鱼
    public void newFish() {
        fish_x = 800;
        fish_y = r.nextInt(450 - fish_height);
        if (fish_y >= 450) {
            // System.out.println(fish_height + "  " + fish_y);
        }
        catched = false;
        blood = k * 3;
        step_size = r.nextInt(5) + 1;
    }

    // 鱼摇摆游动的方法
    int index = 0;

    public void change() {
        index++;
        fishImage = fishImages[index / 3 % 10];
    }

    //鱼被捕动画的方法
    public void turnOut() {
        for (int i = 0; i < catchImages.length; i++) {
            fishImage = catchImages[i];
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

//渔网类
class Net {
    int netX, netY;//网的坐标
    int netWidth, netHeight;//网的宽高
    BufferedImage netImage;//网的图片

    public Net() {
        super();
        File file = new File("images/net_" + 1 + ".png");
        try {
            netImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        netX = 300;
        netY = 300;
        netWidth = netImage.getWidth();
        netHeight = netImage.getHeight();
    }

    // 鱼网切换的方法
    int power = 0;

    public void change() {
        power++;
        int x = this.netX + this.netWidth / 2;
        int y = this.netY + this.netHeight / 2;
        File file = new File("images/net_" + (power % 7 + 1) + ".png");
        try {
            netImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.netWidth = netImage.getWidth();
        this.netHeight = netImage.getHeight();
        moveTo(x, y);
    }

    //渔网移动的方法
    public void moveTo(int x, int y) {
        this.netX = x - this.netWidth / 2;
        this.netY = y - this.netHeight / 2;
    }

    //捕鱼
    public boolean catchFish(Fish fish) {
        //网的中心坐标
        int zX = netX + netWidth / 2;
        int zY = netY + netHeight / 2;
        //鱼的身体部分中心坐标
        int fX = fish.fish_x + fish.fish_width * 2 / 3;
        int fY = fish.fish_y + fish.fish_height / 2;
        //如果网的中心坐标在鱼的身体部分
        if (zX > fish.fish_x && zX < fish.fish_x + fish.fish_width * 2 / 3
                && zY > fish.fish_y && zY < fish.fish_y + fish.fish_height) {
            fish.blood -= ((power % 7 + 1) * 2);
            // System.out.println(fish.blood);
        } else if (fX > netX && fX < netX + netWidth
                && fY > netY && fY < netY + netHeight) {
            //如果鱼的身体部分中心坐标在网里
            fish.blood -= ((power % 7 + 1) * 2);
            // System.out.println(fish.blood);
        }
        return fish.blood <= 0;
    }
}


