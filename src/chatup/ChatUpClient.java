/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

/**
 *
 * @author Paulo
 */

public class ChatUpClient {
    private Socket socket;
    private String ip;
    private int port;
    private String name;
    
    private TextArea taChatArea;
    private ListView lvClients;
    private ObservableList olClients;
    private ListProperty<String> oCli;
    
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    
    //konstruktor
    public ChatUpClient(String name, String ip, int port) {
        this.ip = ip;
        this.name = name;
        this.port = port;
    }
    
    //pobieranie textarea z kontrolera
    public void setTextArea(TextArea ta) {
        this.taChatArea = ta;
    }
    
    public void setListView(ListView lv) {
        this.lvClients = lv;
        oCli = new SimpleListProperty<>();
        lvClients.itemsProperty().bind(oCli);
    } 
    
    //laczenie sie z serwerem
    public boolean connect() {
        try {
            socket = new Socket(ip,port);
        } catch (IOException ex) {
            System.out.println("Nie polaczylo sie z serwerem " + ex);
            return false;
        }
        
        onScreen("Polaczono z serwerem " + socket.getInetAddress() + ":" + socket.getPort());
        
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("blad przy tworzeniu i/o " + ex);
            return false;
        }
        
        new Listen().start();
        
        try {
            sOutput.writeObject(name);
        } catch (IOException ex) {
            System.out.println("Blad przy wysylaniu nicku " + ex);
            return false;
        }
        
        return true;
    }
    
    //wysylanie wiadomosci do servera
    public void sendMsg(ChatUpMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException ex) {
            System.out.println("blad przy wysylaniu do serwera" + ex);
        }
    }
    
    //drukowanie na ekran
    public void onScreen(String msg) {
        System.out.println(msg);
    }
    
    public void disconnect() {
        try { 
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}
    }
    
    
    
    //odbieranie wiadomosci z servea
    class Listen extends Thread{
        private String inmsg;
        private LocalTime time;
        @Override
        public void run() {
            while(true) {
                try {
                    inmsg = (String)sInput.readObject();
                    if(inmsg.startsWith("@UPDATELVCLIENTS:")) {
                        new Commands("@UPDATELVCLIENTS", inmsg).start();
                    }
                    else {
                        if(inmsg.startsWith(name)) {
                            inmsg = inmsg.replaceFirst(name + ":", "-");
                        }
                        time = LocalTime.now();
                        taChatArea.appendText(time.getHour() + ":" + time.getMinute() + " " + inmsg + "\n");
                        System.out.println(inmsg);
                    }
                } catch (IOException ex) {
                    System.out.println("blad i/o" + ex);
                    break;
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ChatUpClient.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }
        }
    }
    
    //obsluga specjalnych wiadomosci
    class Commands extends Thread {
        private String command;
        private String inmsg;
        
        Commands(String command, String msg) {
            this.command = command;
            this.inmsg = msg;
        }
        
        @Override
        public void run() {
            // ustawianie listy obecnych
            if(command.equalsIgnoreCase("@UPDATELVCLIENTS")) {
                //wyciaganie nickow z listy
                String msg = inmsg;
                msg = msg.replaceFirst("@UPDATELVCLIENTS:", "");
                String nick;
                List<String> l = new ArrayList<>();
                while(msg.length() > 1) {
                    nick = msg.substring(msg.indexOf("<")+1, msg.indexOf(">"));
                    msg = msg.replaceFirst("<"+nick+">", "");
                    l.add(nick);
                }
                olClients = FXCollections.observableArrayList(l);
                oCli.set(olClients);
                //lvClients.setItems(oCli);
            }
        }
        
    }
}
