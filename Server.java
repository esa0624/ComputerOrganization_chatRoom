import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;


public class Server extends JFrame{
    JTextArea jta = new JTextArea();
    JScrollPane jsp = new JScrollPane(jta);
    private JPanel panel = new JPanel();
    private JLabel jb = new JLabel();
    private static JMenuBar menu = new JMenuBar();

    private JButton stopBtn = new JButton("Close Server");
    //put all the connected sockets in a list
    protected static ArrayList<ServerThread> serverthread = new ArrayList<>();
    private static boolean flag = true;
    //build server
    static ServerSocket server = null;
    static int peopleOnline = 0;
    private JMenu jm;


    public static void main(String[] args) throws IOException {
        new Server();

    }

    public Server() throws IOException {
        this.setTitle("Server");
        this.add(jsp, BorderLayout.CENTER);
        jta.setEditable(false);
        panel.add(jb);
        jb.setText("Online clients: "+peopleOnline+"   ");
        panel.add(stopBtn);
        this.add(panel,BorderLayout.SOUTH);
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flag = false;
                try {
                    jta.append("Server is going to close..."+System.lineSeparator());
                    //send message
                    String strSend = "Server is going to close..."+System.lineSeparator();
                    Iterator<ServerThread> it = serverthread.iterator();
                    while(it.hasNext()) {
                        ServerThread o = it.next();
                        o.send(strSend);
                    }
                    if (server != null){
                        server.close();
                        flag = false;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }finally {
                    System.exit(0);
                }
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                flag = false;
                try {
                    jta.append("Server is going to close..."+System.lineSeparator());
                    //send message
                    String strSend = "Server is going to close..."+System.lineSeparator();
                    Iterator<ServerThread> it = serverthread.iterator();
                    while(it.hasNext()) {
                        ServerThread o = it.next();
                        o.send(strSend);
                    }
		    if (server != null){
                        server.close();
                        flag = false;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }finally {
                    System.exit(0);
                }
            }
        });
        this.setBounds(200,100,500,500);
        this.setVisible(true);

        startServer();
    }
    public void startServer(){
        try{
            try {
                server = new ServerSocket(5200);
                flag = true;
                jta.append("Waiting for clients..."+System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(flag){
                Socket accept = server.accept();
                serverthread.add(new ServerThread(accept));
                jta.append("Client"+"["+accept.getInetAddress()+"-"+accept.getPort()+"]"+" joined the chat room"+System.lineSeparator());
                jb.setText("Online clients: "+(++peopleOnline)+"   ");
            }
        }catch (SocketException e){
            System.out.println("Server disconnected");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    class ServerThread implements Runnable {
        Socket socket = null;
        public ServerThread(Socket socket) {
            this.socket = socket;
            (new Thread(this)).start();
        }

        @Override
        public void run() {
            String name = socket.getInetAddress() + "-" + socket.getPort();
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                while (flag) {
                    //read the message from client
                    String str = dis.readUTF();
                    if (str.startsWith("##")) {
                        name = str.substring("##".length(), str.length());
                        jta.append("Client" + "[" + socket.getInetAddress() + "-" + socket.getPort() + "]" + " username is " + "[" + name + "]" + System.lineSeparator());
                        Iterator<ServerThread> it = serverthread.iterator();
                        while (it.hasNext()) {
                            ServerThread o = it.next();
                            o.send(name + " joined the chat room" + System.lineSeparator());
                        }
                    } else if (str.startsWith("!!")){
                        String msg = str.substring("!!".length(), str.length());
                        System.out.println(msg);
                        jta.append( msg +System.lineSeparator());
                        Iterator<ServerThread> it = serverthread.iterator();
                        while (it.hasNext()) {
                            ServerThread o = it.next();
                            o.send(msg + System.lineSeparator());
                        }

                    }else {
                        jta.append("[" + name + "]" + ": " + str + System.lineSeparator());
                        //send the message
                        String strSend = "[" + name + "]" + ": " + str + System.lineSeparator();
                        //send to all the clients
                        Iterator<ServerThread> it = serverthread.iterator();
                        while (it.hasNext()) {
                            ServerThread o = it.next();
                            o.send(strSend);
                        }
                    }
                }
            } catch (SocketException e) {
                jta.append("Client" + "[" + name + "]" + " left the chat room" + System.lineSeparator());
                Iterator<ServerThread> it = serverthread.iterator();
                while (it.hasNext()) {
                    ServerThread o = it.next();
                    o.send(name + " left the chat room" + System.lineSeparator());
                }
                jb.setText("Online client: " + (--peopleOnline) + "   ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void send(String str){
            try {
                DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
                dos.writeUTF(str);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

