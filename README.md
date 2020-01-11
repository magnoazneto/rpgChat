# Funcionamento

Este projeto funciona com um sistema Server-Clients,
onde o servidor fica responsável por receber os dados
de cada client e enviar para todos os outros.

### Server

A classe _server.java_ criar uma conexão local partindo
de um server socket, usando uma porta local definida pelo
próprio usuário.

```java
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
```

O número da porta é captado na própria função Main, bem
como a definição da janela de interface gráfica:

```java
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
```

Após isso, o server estará iniciado e pronto para 
receber alguma mensagem de Client.

```java
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
```

Assim que uma mensagem for captada, ele irá se encarregar
de enviar esta mensagem para todos os clients:

```java
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
```


### Client

O client deve ser aberto para cada jogador e mestre presente
na sessão de RPG. Cada um deles terá permissão de rolagem de
dados e de fala, tornando assim possível o desenrolar do jogo.

Para criar o Client, o usuário deve informar alguns campos como
porta, IP e nickname. O construtor se encarregará então de 
construir a janela de visualização gráfica com um histórico de 
mensagens, um campo de digitação e alguns botões de ação:

```java
// public class Client extends JFrame implements ActionListener, KeyListener
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
```

Após isso, o cliente se conecta no servidor atráves da função
connect:
```java
public void connect() throws IOException{
    socket = new Socket(txtIP.getText(),Integer.parseInt(txtPort.getText()));
    ou = socket.getOutputStream();
    ouw = new OutputStreamWriter(ou);
    bfw = new BufferedWriter(ouw);
    bfw.write(txtName.getText()+"\r\n");
    bfw.flush();
}
```

Então, ele está pronto para esperar por mensagens enviadas por
esse servidor:

```java
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
```

Enquanto "ouvinte", o client também pode performar suas ações
como enviar mensagens, sair da conexão, ou rolar algum dado:
```java
public void exit() throws IOException{

    sendMessage("Sair");
    bfw.close();
    ouw.close();
    ou.close();
    socket.close();
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
```

Essas duas funções são implementadas desta forma apenas
para satisfazer a condição abstrata de KeyListener:
```java
@Override
public void keyReleased(KeyEvent arg0) {
    // TODO Auto-generated method stub
}

@Override
public void keyTyped(KeyEvent arg0) {
    // TODO Auto-generated methor stub
}
```

Essa é a função responsável por enviar as mensagens para o 
server:
```java
 public void sendMessage(String msg) throws IOException{

    if(msg.equals("Sair")){
        bfw.write("Desconectado \r\n");
        text.append("Desconectado \r\n");
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
```

Note que ela checa se a mensagem digitada pelo usuário não é 
um comando de rolagem de dados, pois, caso seja, ela terá um tratamento
especial:

```java
private boolean isCommand(String msg) {
    boolean result = false;
    String parts[] = msg.split(" ");
    if(parts[0].equals("/r")){
        result = true;
    }
    return result;
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
```

A rolagem de dados em um RPG consiste em 2 partes principais:
> A obtenção de um número aleatório de acordo com o número de
faces do dado:

```java
 private int getRandomRoll(int diceMax){
    Random diceRoller = new Random();
    return diceRoller.nextInt(diceMax) + 1;
}
```

> A soma/subtração de modificadores de resultado possuídos por um
player ou determinados pelo mestre:

```java
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
```