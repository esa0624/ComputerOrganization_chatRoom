import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class  Client extends JFrame implements KeyListener {
    private JTextArea area;
    private JScrollPane pane;
    private JPanel panel;
    private JTextField field;
    private JButton button;
    private Socket socket = null;
    private DataOutputStream dos = null;
    private boolean flag = false;
    String name = "";

    public Client(){
        area = new JTextArea();
        area.setEditable(false);
        pane = new JScrollPane(area);
        panel = new JPanel();
        field = new JTextField(15);
        button = new JButton("Send message");
    }
    public void startClient(){
        panel.add(field);
        field.addKeyListener(this);
        panel.add(button);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = field.getText();
                send(text);
                if (text.length()==0){
                    return;
                }
                field.setText("");
            }
        });
        this.add(pane, BorderLayout.CENTER);
        this.add(panel, BorderLayout.SOUTH);

        this.setBounds(700, 300, 400, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        try{
            socket = new Socket("127.0.0.1",5200);
            flag = true;
            name = JOptionPane.showInputDialog(this.getContentPane(), "Please enter your username:");
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("##"+name);
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
        this.setTitle("Chat Room--"+name);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                flag = false;
                try {
                    dos.writeUTF("!!"+ name + " left the chat room");
                    dos.flush();
                    socket.close();
                    System.exit(0);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        new Thread(new Receive()).start();
    }
    public void send(String msg){
        try{
            if (field.getText().length() == 0){
                return;
            }
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(msg);
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    class Receive implements Runnable{

        @Override
        public void run() {
            try{
                while(flag){
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String msg = in.readUTF();
                    String check = "Server is going to close...";
                    area.append(msg);
                    if (msg.equals(check)){
                        flag = false;
                        in.close();
                        socket.close();
                    }
                }
            } catch (EOFException e) {
                System.out.println("Server closed");
                area.append("Server closed"+System.lineSeparator());
                System.exit(0);

            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Client().startClient();

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            String msg = field.getText();
            send(msg);
            field.setText("");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

