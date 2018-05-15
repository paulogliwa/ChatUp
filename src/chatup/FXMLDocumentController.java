/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatup;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Paulo
 */
public class FXMLDocumentController implements Initializable {
//<editor-fold defaultstate="collapsed" desc="FXML labels n stuff">
    @FXML private Label lStatus1;
    @FXML private Label lStatus2;
    @FXML private Button bConnect;
    @FXML private Button bDisconnect;
    @FXML private Button bPrivate;
    @FXML private Button bSend;
    @FXML private Button bWhoIs;
    @FXML private TextArea taChat;
    @FXML private TextField tfNick;
    @FXML private TextField tfIP;
    @FXML private TextField tfPort;
    @FXML private TextField tfMessage;
    @FXML private ListView lvOnline;
//</editor-fold>
    
    ChatUpClient client;
    String selected;
    
    
    @FXML //wcisniecie przycisku Connect
    public void bConnect() {
        client = new ChatUpClient(tfNick.getText(), tfIP.getText(), Integer.parseInt(tfPort.getText()));
        if(!client.connect()) {
            lStatus1.setText("could not connect to the server");
            return;
        }
        lStatus1.setText("Connected.");
        
        client.setTextArea(taChat);
        client.setListView(lvOnline);
        
        bConnect.setDisable(true);
        bDisconnect.setDisable(false);
        bSend.setDisable(false);
        tfMessage.setDisable(false);
        tfNick.setDisable(true);
        tfIP.setDisable(true);
        tfPort.setDisable(true);
        bWhoIs.setDisable(false);
    }
    
    @FXML
    public void bDisconnect() {
        bConnect.setDisable(false);
        bDisconnect.setDisable(true);
        bSend.setDisable(true);
        tfMessage.setDisable(true);
        client.sendMsg(new ChatUpMessage(ChatUpMessage.EXIT, ""));
        client.disconnect();
        tfNick.setDisable(false);
        tfIP.setDisable(false);
        tfPort.setDisable(false);
        bWhoIs.setDisable(true);
        bPrivate.setDisable(true);
        lStatus1.setText("Disconnected");
    }
    
    @FXML
    public void bWhoIs() {
        client.sendMsg(new ChatUpMessage(ChatUpMessage.ONLINE, ""));
    }
    
    @FXML
    public void bPrivate() {
        if(selected == null) {
            lStatus2.setText("You have to pick a person first.");
        }
        else if(selected.equals(tfNick.getText())) {
            lStatus2.setText("Can't speak with yourself.");
        }
        else {
            client.sendMsg(new ChatUpMessage(ChatUpMessage.PRIVATE, "[" + selected + "]" + tfMessage.getText()));
            tfMessage.clear();
        }
    }
    
    @FXML //wcisniecie entera w polu wiadomosci
    public void enterPressed(KeyEvent ev) {
        if(ev.getCode() == KeyCode.ENTER) {
            bSend();
        }
    }
    
    @FXML //wcisniecie przycisku Send
    public void bSend() {
        String msg = tfMessage.getText();
        if(msg.equalsIgnoreCase("")) return; //pusta wiadomosc
        client.sendMsg(new ChatUpMessage(ChatUpMessage.MSG, msg));
        tfMessage.clear();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lvOnline.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                bPrivate.setDisable(false);
                selected = newValue;
            }
            
        });
    }    
    
}
