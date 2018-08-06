/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Client.NimClient;
import javax.swing.JFrame;

/**
 *
 * @author paul
 */
public class NimServerRun {
    
    public static void main(String[] args) {
        
        MultiClientNimServer server = new MultiClientNimServer();
        
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        server.acceptClientThreads();
    }
}
