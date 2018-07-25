/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

/**
 *
 * @author paul
 */
public class NimMultithreadClient implements Runnable {

    private String hostName;
    private Socket connection;
    private DataInputStream inputFromServer;
    private DataOutputStream outputToServer;
    private int serverMessage;
    private BufferedReader userInputReader;
    private boolean thisClientsTurn = false;

    public NimMultithreadClient(String host) {
        this.hostName = host;
        startClient();
    }

    public void startClient() {
        try {
            //create a new socket connection to the server (port 12345), with an identifying host name
            connection = new Socket(InetAddress.getByName(hostName), 12345);

            //retrieve IO streams for communication with the server
            inputFromServer = new DataInputStream(connection.getInputStream());
            outputToServer = new DataOutputStream(connection.getOutputStream());
            //String userInput = userInputScanner.nextLine();
//            run();

            //outputToServer.writeUTF(userInput);
        } catch (IOException iOException) {
            iOException.printStackTrace();
            displayMessage("failed to start client");
        }

        Thread readOutputFromServer = new Thread(this);
        readOutputFromServer.start();
    }

    public void run() {

        while (true) {
            //if (inputFromServer.hasNextLine()) {
            try {
                processMessage(inputFromServer.readUTF());

            } catch (Exception e) {
                e.printStackTrace();
            }
            //processMessage(inputFromServer.nextLine());
            //}
        }
    }

    private void processMessage(String message) {
        // valid move occurred
        if (message.contains("Valid input.")) {
            displayMessage(message);
            //makeMove();
            thisClientsTurn = false;
        } // end if
        else if (message.equals("Invalid move, try again")) {
            displayMessage(message); // display invalid move
            thisClientsTurn = true;
            makeMove();

        } else if (message.contains("Playing against server.")) {
            displayMessage(message);

        } else if (message.contains("Second player connected.")) {
            displayMessage(message);

        } else if (message.contains("Remove from pile.")) {
            displayMessage(message);
            thisClientsTurn = true;
            makeMove();
        } else if (message.contains("Opponent took")) {
            displayMessage(message);
            thisClientsTurn = true;
            makeMove();
        } else {
            displayMessage(message); // display the message
        }
    } // end method processMessage

    private void makeMove() {
        try {
            if (thisClientsTurn) {
                userInputReader = new BufferedReader(new InputStreamReader(System.in));
                serverMessage = Integer.parseInt(userInputReader.readLine());
                outputToServer.writeInt(serverMessage);
                outputToServer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    private void displayMessage(final String messageToDisplay) {
        System.out.println("Client>>> " + messageToDisplay);

//        SwingUtilities.invokeLater(
//                new Runnable() {
//            public void run() {
//                displayArea.append(messageToDisplay); // updates output
//            } // end method run
//        } // end inner class
//        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage

    public static void main(String[] args) {
        NimMultithreadClient testClient; //connection from localhost

        if (args.length == 0) {
            testClient = new NimMultithreadClient("127.0.0.1"); // localhost
        } else {
            testClient = new NimMultithreadClient(args[0]); // use args
        }
    }
}
