/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo
 */



public class ChatUpServer {
    private boolean stop;
    private int port;
    private String motd;
    private List<ClientThread> clients;
    private int ids;
    
    //konstruktor
    public ChatUpServer(int port) {
        this.port = port;
        clients = new ArrayList<>();
        ids = 0;
    }
    //konstruktor
    public ChatUpServer(int port, String motd) {
        this.motd = motd;
        this.port = port;
    }
    
    //glowna petla serwera
    public void run() {
        
        try {
            ServerSocket s = new ServerSocket(this.port);
            Socket g;
            this.stop = false;
            //przyjmowanie klientow
            while(!this.stop) {
                g = s.accept();
                ClientThread st = new ClientThread(g, ids++);
                clients.add(st);
                st.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatUpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //wyswietlanie na ekran
    public void onScreen(String msg){
        System.out.println(msg);
    }
    
    //wysylanie 
    public void sendList() {
        new Commands("@UPDATELVCLIENTS").start();
    }
    
    //priv
    public void privMessage(String privmsg) {
        new Commands("@SENDPRIV"+privmsg).start();
    }
    
    public void sendPriv(String from, String to, String sms) {
        for(ClientThread ct : this.clients) {
            if(ct.name.equals(to)) {
                ct.sendMsg("(PRIVATE MESSAGE FROM: " + from + ") " + sms);
                System.out.println(sms);
            }
            if(ct.name.equals(from)) {
                ct.sendMsg("(PRIVATE MESSAGE SENT TO: " + to + ") " + sms);
            }
        }
    }
    
    public void broadcast(String msg) {
        onScreen(msg);
        for(int i = this.clients.size()-1; i >= 0; i--) {
            ClientThread ct = this.clients.get(i);
            if(!ct.sendMsg(msg)) {
                this.clients.remove(i);
                onScreen(ct.name + "disconnected.");
            }
        }
    }
    
    public synchronized void logout(int id) {
        for(ClientThread ct : this.clients) {
            if(ct.id == id) {
                this.clients.remove(ct);
                sendList();
                return;
            }
        }
    }
    
    public static void main(String[] args) {
        ChatUpServer chup = new ChatUpServer(8569);
        chup.run();
    }
    
    class Commands extends Thread {
        private String command;
        
        Commands(String command) {
            this.command = command;
        }
        
        @Override
        public void run() {
            if(command.equalsIgnoreCase("@UPDATELVCLIENTS")) {
                String list = "@UPDATELVCLIENTS:";
                for(ClientThread ct : clients) {
                    list += "<" + ct.name + ">";
                }
                broadcast(list);
            }
            else if(command.startsWith("@SENDPRIV")) {
                command = command.replaceFirst("@SENDPRIV", "");
                String from = command.substring(command.indexOf("<")+1, command.indexOf(">"));
                command = command.replaceFirst("<" + from + ">", "");
                String to = command.substring(command.indexOf("[")+1, command.indexOf("]"));
                command = command.replaceFirst("[" + to + "]", "");
                command = command.substring(command.indexOf("]")+1);
                sendPriv(from,to,command);
            }
        }
    }
    
    //watek dla kazdego polaczonego uzytkownika
    class ClientThread extends Thread {
        private boolean stop;
        private Socket socket;
        private int id;
        private String name;
        ChatUpMessage o_msg;

        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        
        //konstruktor
        ClientThread(Socket socket, int id) {
            this.stop = false;
            this.socket = socket;
            this.id = id;

            try{
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());

                name = (String)sInput.readObject();
                onScreen("(server) New user connected: " + name);
                broadcast("(server) New user connected: " + name);
                sendList();
            }
            catch(IOException e){
                System.out.println("blad przy tworzeniu i/o " + e);
            }
            catch (ClassNotFoundException e) {
            }


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
        
        public boolean sendMsg(String msg) {
            if(!socket.isConnected()) {
                System.out.println("sendMsgfail !socket.isconnected");
                disconnect();
                return false;
            }
            
            try {
                sOutput.writeObject(msg);
            } catch (IOException ex) {
                System.out.println("blad przy wysylaniu wiadomosci " + ex);
            }
            
            return true;
        }

        @Override
        public void run() {
            while(!stop) {
                try {
                   o_msg = (ChatUpMessage)sInput.readObject();
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
                
                String msg = o_msg.getMsg();
                
                switch(o_msg.getType()) {
                    case ChatUpMessage.MSG: {
                        broadcast(this.name + ": " + msg);
                    }break;
                    
                    case ChatUpMessage.ONLINE: {
                        sendList();
                    }break;
                    
                    case ChatUpMessage.PRIVATE: {
                        privMessage("<" + this.name + ">" + msg);
                        System.out.println("Message from: " + this.name + " to: " + msg);
                    }break;
                    
                    case ChatUpMessage.EXIT: {
                        broadcast("(server) " + this.name + " logged out.");
                        logout(this.id);
                        stopthread(); //zatrzymaj watek
                    }break;
                }
            }
            logout(id);
            disconnect();
        }

        public void stopthread() {
            this.stop = true;
        }
    }
}
