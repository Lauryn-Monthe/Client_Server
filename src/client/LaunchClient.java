package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.*;

import common.Anweisung;
import server.LaunchServer;

/**
 * <p>Diese Klasse implementiert alles, was der Gui beim Ausdrucken eines Buttons passiert werden muss. bwz. der Gui an sich und die Interaktion des Guis.</p>
 */
public class LaunchClient{

    DataOutputStream sending;
    DataInputStream reader;
    Socket socket=null;
    byte[] buffer ;
    int counter =1;
    //Inhalte des Fensters
    JFrame fenster = new JFrame();
    JPanel content1 = new JPanel();
    JPanel content2 = new JPanel();
    JPanel content3 = new JPanel();

    //Combobox zur auswahl der Instruction.
    JComboBox comboBox;

    //Felder zum Eingeben und Erhalten einer Nachricht.
    TextArea text = new TextArea(30,60);
    TextField field = new TextField(30);
    String absolutePath;

    /**
     * <p> Der Konstruktor dieser Klasse bestimmt, wie der Gui jedes Clients aussehen soll und ausgefuehrt werden soll</p>
     * <p> Dazu gibt es zwei button: ein, der eine Nachricht oder eine Datei an Server schickt und ein anderer, der der Pfad zur Auswahl einer Datei dient</p>
     */
    //Konstruktor

    public LaunchClient(){
        buffer = LaunchServer.buffer;
        absolutePath = new File("").getAbsolutePath()+"\\src\\pis\\hue2\\client\\";
        //Eigenschafte des Fensters.
        fenster.setTitle("GuiClient");
        fenster.setLayout(new BorderLayout());
        fenster.setVisible(true);
        fenster.setSize(650,550);
        fenster.setBackground(Color.GRAY);
        fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenster.add(content1,BorderLayout.NORTH);
        JButton button = new JButton("Senden");
        JButton Datei = new JButton("File");
        field.setEnabled(false);
        String[]  options = {"CON","LST","GET","PUT","DEL","DSC"};
        comboBox = new JComboBox(options);
        content1.add(text);
        content3.add(comboBox);
        content3.add(field);
        content3.add(Datei);
        content3.add(button);
        fenster.add(content2,BorderLayout.CENTER);
        fenster.add(content3,BorderLayout.SOUTH);
        //Button zum Abschicken der Nachricht oder einer Datei.
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String msg = (String)comboBox.getSelectedItem();
                    sending.writeUTF(msg);
                    Anweisung instruction = Anweisung.valueOf(msg);
                    switch(instruction){
                        case PUT:
                            if(field.getText().isEmpty()) {
                                JOptionPane.showMessageDialog(text,"Bitte geben Sie ein Pfad einer Datei ein !");
                            }
                            else
                            {
                                text.append("Client: PUT " + field.getText() + " " + new Date() + " " + socket.getLocalAddress().getHostAddress() + "\n");
                                File file = new File(field.getText());
                                InputStream is = new FileInputStream(file);

                                String[] str = field.getText().split(Pattern.quote(File.separator));

                                sending.writeUTF(str[str.length - 1]);// nom du fichier a transferer
                                sending.writeLong(file.length());

                                while (is.available() > 0) {
                                    sending.write(buffer, 0, is.read(buffer));
                                }
                                is.close();
                            }
                            break;
                        case GET:
                            text.append("Client: GET "+ field.getText() +" "+new Date() + " " + socket.getLocalAddress().getHostAddress() + "\n");
                            if(!field.getText().isEmpty()) {
                                sending.writeUTF(field.getText());
                            }
                            else
                            {
                                JOptionPane.showMessageDialog(text,"Bitte geben Sie ein, was für eine Datei Sie haben würden !!");
                            }
                            break;
                        case DEL:
                            text.append("Client: Delete "+ field.getText() + " " +new Date() + " " + socket.getLocalAddress().getHostAddress() + "\n");
                            sending.writeUTF(field.getText());
                            break;
                        case DSC:
                            text.append("Client: DSC " + field.getText() + " " + new Date() + " " +  socket.getLocalAddress().getHostAddress() + "\n");
                            sending.writeUTF(field.getText());
                            break;
                    }
                    //          sending.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        //CON, LST et DSC:
        comboBox.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                Anweisung tmp = Anweisung.valueOf((String)comboBox.getSelectedItem());
                switch(tmp){
                    case CON:
                    case LST:
                    case DSC:
                        field.setEnabled(false);
                        break;
                    default:
                        field.setEnabled(true);
                        break;
                }
            }
        });
        //Button zur Auswahl des Pfades einer Datei.
        Datei.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser file = new JFileChooser();
                file.showOpenDialog(Datei);
                String Pfad = file.getSelectedFile().getAbsolutePath();
                field.setText(Pfad);
            }
        });
        startClient();
    }

    /**
     * <p> Die Methode startClient sorgt dafuer, dass der/die Client(s) in Verbindung mit dem Server setzt/setzen, damit er/sie in der Lage ist/sein, Informationen vom Server zu erhalten und darauf zu reagieren.</p>
     * <p> Die Verbindung trifft nur zu, wenn der Server und der Client den gleichen Port haben.</p>
     */
    public void startClient() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    socket = new Socket("localhost", 8080);
                    sending = new DataOutputStream(socket.getOutputStream()); // Send Message to Server

                    while (true) {
                        reader = new DataInputStream(socket.getInputStream());
                        Anweisung instruction = Anweisung.valueOf(reader.readUTF());
                        switch (instruction) {
                            case PUT:
                                String receive = reader.readUTF();//nom du ficher recu
                                if(!receive.isEmpty()) {
                                    FileOutputStream fos = new FileOutputStream(absolutePath + receive);
                                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                                    int bytesRead = 0;
                                    long totalSize = reader.readLong();
                                    int alreadyReadSize = 0;
                                    while (alreadyReadSize != totalSize) {
                                        bytesRead = reader.read(buffer, 0, Math.min(buffer.length, (int) totalSize - alreadyReadSize));
                                        bos.write(buffer, 0, bytesRead);
                                        alreadyReadSize += bytesRead;
                                    }
                                    text.append("Client: " + receive + " was received " + new Date() + " " + socket.getLocalAddress().getHostAddress() + "\n");

                                    bos.flush();
                                    bos.close();
                                    fos.close();
                                }
                                break;
                            case DND:
                                text.append("Server: " + instruction + " etwas ist schiefgelaufen" + " " + new Date() + " " + socket.getLocalAddress().getHostAddress() + "\n");
                                break;
                            case ACK:
                                text.append("Server: " + instruction + " " + new Date() + " " + socket.getLocalAddress().getHostAddress() + "\n");
                                break;
                            case LST:
                                text.append("Server: list a directory  " + new Date() + " " + socket.getLocalAddress().getHostAddress() + "\n");
                                text.append(reader.readUTF());
                                break;
                            case DSC:
                                text.append("Server: " + instruction + "  (Disconnet...) " + new Date() + " " +  socket.getLocalAddress().getHostAddress());
                                socket.close();
                                //text.append(reader.readUTF());
                                break;
                            //case DAT:
                            //text.append("Server: Bytes der angegebenen Datei");
                            //text.append(reader.readUTF());
                            //break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    //Einstiegspunkt zum Laufen des Programms.
    public static void main(String[] args)
    {
        //Methode EventQueue für die Steuerung von mehreren Threads, damit sie ohne Sorge laufen.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try{
                    //Erzeugung von drei Client.
                    LaunchClient client = new LaunchClient();
                    LaunchClient client2 = new LaunchClient();
                    LaunchClient client3 = new LaunchClient();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}