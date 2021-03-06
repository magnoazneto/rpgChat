package rpgGame;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Random;
import javax.swing.*;

public class Client extends JFrame implements ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private JTextArea text;
    private JTextField txtMsg;
    private JButton btnSend;
    private JButton btnExit;
    private JLabel lblHistoric;
    private JLabel lblMsg;
    private JPanel pnlContent;
    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;
    private JTextField txtIP;
    private JTextField txtPort;
    private JTextField txtName;

    public Client() throws IOException{
        // dados de login
        JLabel lblMessage = new JLabel("Dados de conexão: ");
        JLabel ipLabel = new JLabel("Endereço IP");
        txtIP = new JTextField("127.0.0.1");
        JLabel portLabel = new JLabel("Porta do servidor");
        txtPort = new JTextField("5000");
        JLabel nickLabel = new JLabel("Nickname");
        txtName = new JTextField("Nickname");
        Object[] texts = {lblMessage, ipLabel, txtIP, portLabel, txtPort, nickLabel, txtName };
        JOptionPane.showMessageDialog(null, texts);

        // histórico
        text = new JTextArea(10,20);
        text.setEditable(false);
        text.setBackground(new Color(240,240,240));
        lblHistoric = new JLabel("Histórico");
        lblHistoric.setForeground(Color.LIGHT_GRAY);
        JScrollPane scroll = new JScrollPane(text);
        text.setLineWrap(true);

        // área de ações
        txtMsg = new JTextField(20);
        lblMsg = new JLabel("Rolagem / Mensagem");
        lblMsg.setForeground(Color.LIGHT_GRAY);
        btnSend = new JButton("Enviar");
        btnSend.setToolTipText("Enviar Mensagem");
        btnExit = new JButton("Sair");
        btnExit.setToolTipText("Sair do Game");

        btnSend.addActionListener(this);
        btnExit.addActionListener(this);
        btnSend.addKeyListener(this);
        txtMsg.addKeyListener(this);

        //construção de painel de mensagens
        pnlContent = new JPanel();
        pnlContent.add(lblHistoric);
        pnlContent.add(scroll);
        pnlContent.add(lblMsg);
        pnlContent.add(txtMsg);
        pnlContent.add(btnExit);
        pnlContent.add(btnSend);
        pnlContent.setBackground(Color.DARK_GRAY);
        text.setBorder(BorderFactory.createEtchedBorder(Color.BLACK,Color.RED));
        txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLACK, Color.RED));
        setTitle(txtName.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250,300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void connect() throws IOException{
        socket = new Socket(txtIP.getText(),Integer.parseInt(txtPort.getText()));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(txtName.getText()+"\r\n");
        bfw.flush();
    }

    private boolean isCommand(String msg) {
        boolean result = false;
        String parts[] = msg.split(" ");
        if(parts[0].equals("/r")){
            result = true;
        }
        return result;
    }

    private int getRandomRoll(int diceMax){
        Random diceRoller = new Random();
        return diceRoller.nextInt(diceMax) + 1;
    }

    private int getModifiersResult(String modifiers){
        if (modifiers.length() == 0){ return 0; }
        boolean validModifiers = true;
        int acumulator = 0;
        String subAcumulator = "";
        String previousOperation = modifiers.charAt(0) + "";
        for (int i = 1; i<modifiers.length(); i++){
            if (java.lang.Character.isDigit(modifiers.charAt(i))){
                subAcumulator += modifiers.charAt(i) + "";
            }
            else{
                if (previousOperation.charAt(0) == '+') {
                    acumulator += Integer.parseInt(subAcumulator);
                    subAcumulator = "";
                }
                else if (previousOperation.charAt(0) == '-') {
                    acumulator -= Integer.parseInt(subAcumulator);
                    subAcumulator = "";
                }
                if(modifiers.charAt(i) == '+'){
                    previousOperation = "+";
                }
                else if(modifiers.charAt(i) == '-'){
                    previousOperation = "-";
                }
                else{
                    validModifiers = false;
                    break;
                }
            }
        }
        if (validModifiers) {
            if (subAcumulator.length() > 0) {
                if (previousOperation.charAt(0) == '+') {
                    acumulator += Integer.parseInt(subAcumulator);
                } else if (previousOperation.charAt(0) == '-') {
                    acumulator -= Integer.parseInt(subAcumulator);
                }
            }
        }
        else{
            acumulator = 0;
        }
        return acumulator;
    }

    private String getRoll(String msg){
        String parts[] = msg.split(" ");
        String command = parts[1].toLowerCase();
        String returnRoll = "";
        String dice = "";
        String modifiers = "";
        if (command.charAt(0) == 'd'){
            for (int i = 1; i< command.length(); i++){
                if (java.lang.Character.isDigit(command.charAt(i))){
                    dice += command.charAt(i);
                }
                else{
                    modifiers = command.substring(i); // pega sempre do primeiro operador
                    break;
                }
            }
            int rollResult = getRandomRoll(Integer.parseInt(dice));
            int finalResult = rollResult + getModifiersResult(modifiers);
            JOptionPane.showMessageDialog(null, "Resultado: " + rollResult + modifiers + " = " + finalResult);
            returnRoll = rollResult + modifiers + " = " + finalResult;

        }
        else{
            returnRoll = "Rolagem inválida";
        }

        return returnRoll;
    }

    public void sendMessage(String msg) throws IOException{
        //System.out.println(bfw);
        if(msg.equals("Sair")){
            bfw.write("Desconectado \r\n");
            text.append("Player desconectado \r\n");
        }else{
            if(isCommand(msg)){
                String roll = getRoll(msg);
                bfw.write(roll + "\r\n");
                text.append("Sua rolagem: "+ roll + "\r\n");
            }
            else {
                bfw.write(msg + "\r\n");
                text.append(txtName.getText() + ": " + txtMsg.getText() + "\r\n");
            }
        }
        bfw.flush();
        txtMsg.setText("");
    }

    public void listen() throws IOException{

        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";

        while(!"Sair".equalsIgnoreCase(msg))
            if(bfr.ready()){
                msg = bfr.readLine();
                if(msg.equals("Sair"))
                    text.append("Servidor caiu! \r\n");
                else
                    text.append(msg+"\r\n");
            }
    }

    public void exit() throws IOException{

        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
        setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if(e.getActionCommand().equals(btnSend.getActionCommand()))
                sendMessage(txtMsg.getText());
            else
            if(e.getActionCommand().equals(btnExit.getActionCommand()))
                exit();
        } catch (IOException e1) {
            System.out.println(e1.getMessage());;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try{
                sendMessage(txtMsg.getText());
            } catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }


    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated methor stub
    }

    public static void main(String[] args) throws IOException {
        Client app = new Client();
        app.connect();
        app.listen();
    }
}
