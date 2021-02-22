package server;

import common.Anweisung;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Date;


/**
 * <p> In der Klasse ClientConnecting werden alle Funktionalitäten des Datei-Transfers vom Client nach Server implementiert</p>
 * <p> Jede Nachricht, die vom bestimmten CLient nach Server kommt, trifft genaue Funktionalitaet mit Anweisungen, die ausgefuehrt werden und an Client geschickt werden.</p>
 */
public class ClientConnection extends Thread{

    // Variablendeklaration
    DataInputStream dis;
    DataOutputStream dos;
    FileOutputStream fos;
    BufferedOutputStream bos;
    Socket s;
    //Konstruktor
    public ClientConnection(DataInputStream dis, DataOutputStream dos, Socket s) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
    }

    /**
     * <p> Die Run-Methode, die vom Thread erbt, implementiert die Anweisungen, die jeder Client zum Laufen des Programms braucht.</p>
     */
    @Override
    public void run() {

        try {
            while (true)
            {
                if(s.isClosed())
                    LaunchServer.counter--;
                Anweisung instruction = Anweisung.valueOf(dis.readUTF());
                String receive="";
                File file;
                switch(instruction){
                    case CON:
                        if(LaunchServer.counter<=3) {
                            LaunchServer.textServer.append("Client: (" + (LaunchServer.counter--) + ")" + " " + instruction + " " + new Date() + "\n ");
                            LaunchServer.textServer.append("Protokoll der gesendeten Nachricht: " + LaunchServer.socket.getLocalAddress().getHostAddress() + " " + "Protokoll der empfangenen Nachricht: " + LaunchServer.server.getLocalSocketAddress() + "\n");
                            dos.writeUTF("ACK");
                        }
                        else
                        {
                            dos.writeUTF("DND");
                        }
                        break;
                    case DSC:
                        LaunchServer.textServer.append("Client: (" + (LaunchServer.counter) +")" + " " + " Disconnect.. " + " " + new Date() + " " + LaunchServer.socket + "\n");
                        dos.writeUTF("DSC");
                        --LaunchServer.counter;
                        //LaunchServer.socket.close();
                        break;
                    case PUT:
                        receive = dis.readUTF();//nom du ficher recu
                        if(!receive.isEmpty()) {

                            dos.writeUTF("ACK");
                            LaunchServer.textServer.append("Client: ("+ (LaunchServer.counter) +")"+ "\n");
                            fos = new FileOutputStream(LaunchServer.absolutePath + receive);
                            bos = new BufferedOutputStream(fos);
                            long totalSize = dis.readLong();
                            long alreadyReadSize = 0;
                            while (alreadyReadSize != totalSize) {
                                if (totalSize - alreadyReadSize < LaunchServer.buffer.length)
                                    LaunchServer.bytesRead = dis.read(LaunchServer.buffer, 0, (int) (totalSize - alreadyReadSize));
                                else
                                    LaunchServer.bytesRead = dis.read(LaunchServer.buffer, 0, LaunchServer.buffer.length);
                                bos.write(LaunchServer.buffer, 0, LaunchServer.bytesRead);
                                alreadyReadSize += LaunchServer.bytesRead;
                            }

                            LaunchServer.textServer.append("server:  "+ instruction+ " " + receive + " was added " + new Date() + "\n");
                            dos.writeUTF("ACK");
                            bos.flush();
                            bos.close();
                            fos.close();
                        }
                        else
                        {
                            dos.writeUTF("DND");
                        }
                        break;
                    case GET:
                        receive = dis.readUTF();//nom du ficher demandé
                        //LaunchClient.text.append("Server: ACK" + "\n");
                        file = new File(LaunchServer.absolutePath + receive);

                        dos.writeUTF("ACK");
                        LaunchServer.textServer.append("Client: ("+ (LaunchServer.counter) + ")"+ " ACK" + " " + new Date() + "\n");

                        if (file.exists()) {
                            InputStream is = new FileInputStream(file);

                            dos.writeUTF("PUT");
                            dos.writeUTF(receive);// nom du fichier a transferer
                            dos.writeLong(file.length());

                            while (is.available() > 0) {
                                dos.write(LaunchServer.buffer, 0, is.read(LaunchServer.buffer));
                            }
                            LaunchServer.textServer.append("server:  " + instruction + " " + receive + " was send " + new Date() + "\n");
                            LaunchServer.textServer.append("Client: ("+ (LaunchServer.counter) +")"+ " ACK "+ new Date() + "\n");
                            is.close();
                        } else if (!file.exists()) {
                            dos.writeUTF("DND");
                        }
                        break;
                    case LST:

                        dos.writeUTF("ACK");
                        System.out.println("\n");
                        LaunchServer.textServer.append("Client: ("+ (++LaunchServer.counter) +")"+ " ACK "+ "\n");
                        LaunchServer.textServer.append("Client: (" + (LaunchServer.counter++) + ") LST Request " + " " + new Date()+ "\n");
                        dos.writeUTF("LST");
                        file = new File(LaunchServer.absolutePath);
                        String[] listFiles = file.list();
                        if(listFiles.length>0) {
                            String msg = "\t vorhandene Dateien\n";
                            for (int i = 0; i < listFiles.length; i++)
                                msg += "\t\t ->" + listFiles[i] + "\n";
                            dos.writeUTF(msg);

                            LaunchServer.textServer.append("Client: (" + (--LaunchServer.counter) + ")" + " ACK " + "\n");
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(LaunchServer.textServer,"Keine derzeit vorhandenen Datei im Server !!!");
                        }
                        break;
                    case DEL:
                        receive = dis.readUTF();// fichier a suprimer
                        file = new File(LaunchServer.absolutePath + receive);
                        if(file.exists()){
                            file.delete();
                            LaunchServer.textServer.append("server: "+ receive +" was deleted "+ new Date()+ "\n");
                            dos.writeUTF("ACK");
                        }
                        else
                        {
                            dos.writeUTF("DND");
                        }
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}