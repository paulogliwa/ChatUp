/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatup;

import java.io.Serializable;

/**
 *
 * @author Paulo
 */
public class ChatUpMessage implements Serializable {
    static final int EXIT = 0, MSG = 1, ONLINE = 2, PRIVATE = 3;
    private int type;
    private String msg;
    
    ChatUpMessage(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }
    
    int getType() {
        return this.type;
    }
    
    String getMsg() {
        return this.msg;
    }
}
