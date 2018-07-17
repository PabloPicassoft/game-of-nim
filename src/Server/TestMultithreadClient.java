/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.SwingUtilities;

/**
 *
 * @author paul
 */
public class TestMultithreadClient {

    private String hostName;
    private Socket connection;
    private DataInputStream inputFromServer;
    private DataOutputStream outputFromClient;

    public TestMultithreadClient(String host) {
        this.hostName = host;
        startClient();
    }

    public void startClient() {
        try {
            //create a new socket connection to the server (port 12345), with an identifying host name
            connection = new Socket(InetAddress.getByName(hostName), 12345);

            //retrieve IO streams for communication with the server
            inputFromServer = new DataInputStream(connection.getInputStream());
            outputFromClient = new DataOutputStream(connection.getOutputStream());
        } catch (IOException iOException) {
            iOException.printStackTrace();
            displayMessage("failed to start client");
        }
    }

    //
    private void displayMessage(final String messageToDisplay) {
        System.out.println("Client>>> " + messageToDisplay + "\n");

//        SwingUtilities.invokeLater(
//                new Runnable() {
//            public void run() {
//                displayArea.append(messageToDisplay); // updates output
//            } // end method run
//        } // end inner class
//        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage

    public static void main(String[] args) {
        TestMultithreadClient testClient; //connection from localhost
        
        if (args.length == 0) {
            testClient = new TestMultithreadClient("127.0.0.1"); // localhost
        } else {
            testClient = new TestMultithreadClient(args[0]); // use args
        }
    }
}
