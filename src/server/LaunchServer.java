package server;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 *<p> Die Klasse LaunchServer kuemmert sich um den gesamten Prozess des Servers bis zur Erfolgreich</p>
 */
public class LaunchServer extends ClientConnection{
    public final int Port = 8080; //Port des Servers
    //Variablen zum Laufen des Servers
    public static DataInputStream reader;
    public static DataOutputStream sending;
    public static Socket socket = null;
    public static String absolutePath;
    public static ServerSocket server = null;
    public static int counter = 0;
    public static int bytesRead = 0;
    public static byte[] buffer = new byte[1024 * 1024 * 32];

    //Gui Variablen
    JFrame fenster = new JFrame();
    JPanel contentServer1 = new JPanel();
    JPanel contentServer2 = new JPanel();
    JPanel contentServer3 = new JPanel();
    //Felder zum Eingeben und Erhalten einer Nachricht.
    public static TextArea textServer = new TextArea(30, 60);
    JLabel message_1 = new JLabel();
    JLabel message_2 = new JLabel();

    /**
     * <p> Der Konstruktor der Klasse bestimmt den Pfad, wo die Dateien von Clients gespeichert werden sollen und Das Aussehen der Oberflaeche des Servers-</p>
     */
    public LaunchServer() {
        super(reader,sending,socket); // Methode-Super der Klasse Clientconnection mit ihren Attributen, weil LaunchServer erbt davon.
        //Eigenschafte des Fensters.
        absolutePath = new File("").getAbsolutePath() + "\\src\\server\\"; //c'est dans ce dossier que vont etre enregistrés les Fichier recus
        fenster.setTitle("GuiServer");
        fenster.setLayout(new BorderLayout());
        fenster.setVisible(true);
        fenster.setSize(550, 550);
        fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contentServer1.add(message_1);
        contentServer1.add(message_2);
        contentServer1.add(textServer);
        fenster.add(contentServer1, BorderLayout.CENTER);
        fenster.add(contentServer2, BorderLayout.SOUTH);
        runServer();
    }

    /**
     * <p> Die Methode runserver wartet auf neuen bzw. einen neuen Client (maximal drei), der den gleichen Port wie der Server hat</p>
     * <p> Bei einem gleichen Port wie der Server wird der entsprechende Client angenommen und mit dem Server in Verbindung gesetzt.</p>
     * <p> Bei jedem neuen angenommenen Client wird jeweils ein Objetksthread angelegt, denn Sie teilen zusammen den Arbeitsspeicher des Computers</p>
     */
    //Methode zur Durchführung der Anweisungen des Serves.
    public void runServer() {
        Thread threadServer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(Port); // Erstellung eines Objekts Server mit seinem Port.
                    textServer.append("Server start and waiting for client..." + "\n");
                    while (true) {
                        if (counter <= 2) {
                            socket = server.accept(); // Wartet der Server hier auf einen Client.
                            textServer.append("Accepted connection: Client: (" + (++counter) + ")" + " " + new Date() + " " + socket + "\n");
                            sending = new DataOutputStream(socket.getOutputStream());
                            reader = new DataInputStream(socket.getInputStream());  //Read Message from Client
                            Thread newThreadForClient = new ClientConnection(reader, sending, socket); // Estellung vom jeden Thread eines Clients, damit sie zusammen laufen und den Arbeitspeicher teielen.
                            newThreadForClient.start();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadServer.start(); //Starten der Run-Methode des Serves.
    }



    //Einstiegspunkt zum Laufen des Programms.
    public static void main(String [] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try{
                    //Erzeugung des Servers.
                    LaunchServer server = new LaunchServer();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}

