package rpgGame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Server extends Thread {
    private static ArrayList<BufferedWriter>clients;
    private static ServerSocket server;
    private String name;
    private Socket connection;
    private InputStreamReader inStreamReader;
    private BufferedReader buffReader;

    public Server(Socket connection){
        this.connection = connection;
        try {
            inStreamReader = new InputStreamReader(connection.getInputStream());
            buffReader = new BufferedReader(inStreamReader);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            String msg;
            Writer outStreamWriter = new OutputStreamWriter(this.connection.getOutputStream());
            BufferedWriter buffWriter = new BufferedWriter(outStreamWriter);
            clients.add(buffWriter);
            name = msg = buffReader.readLine();

            while(!"Sair".equalsIgnoreCase(msg) && msg != null){
                msg = buffReader.readLine();
                sendToAll(buffWriter, msg);
                System.out.println(msg);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void sendToAll(BufferedWriter bwExit, String msg) throws IOException{
        BufferedWriter bwStream;

        for(BufferedWriter bw: clients){
            bwStream = (BufferedWriter)bw;
            if(!(bwExit == bwStream)){
                bw.write(name + ": " + msg+"\r\n");
                bw.flush();
            }
        }
    }

    public static void main(String[] args) {
        try{
            JLabel labelMessage = new JLabel("Porta do servidor: ");
            JTextField textPort = new JTextField("5000");
            Object[] texts = {labelMessage, textPort};
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(textPort.getText()));
            clients = new ArrayList<BufferedWriter>();
            JOptionPane.showMessageDialog(null, "Servidor ativo na porta: "+ textPort.getText());

            while (true){
                System.out.println("Aguardando conexão...");
                Socket con = server.accept();
                System.out.println("Cliente conectado...");
                Thread t = new Server(con);
                t.start();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
