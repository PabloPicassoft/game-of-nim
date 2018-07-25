/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.awt.BorderLayout;
import static java.awt.Event.ENTER;
import java.awt.event.ActionEvent;
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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author paul
 */
public class NimMultithreadClient extends JFrame implements Runnable {
    
    private JTextArea displayArea;
    private JTextArea inputArea;
    
    private JButton sendMoveButton;
    
    private JPanel mainPanel; // 
    private JPanel bottomPanel; // panel to hold board

    private String hostName;
    private Socket connection;
    private DataInputStream inputFromServer;
    private DataOutputStream outputToServer;
    private int serverMessage;
    private BufferedReader userInputReader;
    private boolean thisClientsTurn = false;
    
    public NimMultithreadClient(String host) {
        super("Game of Nim Client");
        this.hostName = host;
        
        initializeGUIComponents();
        startClient();
    }
    
    public void initializeGUIComponents() {

//      idField = new JTextField(); // set up textfield
//      idField.setEditable( false );
//      add( idField, BorderLayout.NORTH );
        displayArea = new JTextArea(); // set up JTextArea
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
        
        bottomPanel = new JPanel();
        
        inputArea = new JTextArea(4, 30);
        inputArea.setEditable(false);
        
        sendMoveButton = new JButton("Make Move");
        sendMoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMoveButtonClick(evt);
            }
        });
        sendMoveButton.setEnabled(false);
        //sendMoveButton.setActionCommand(ENTER);
        //sendMoveButton.addActionListener(buttonListener);
        bottomPanel.add(inputArea); // add container panel
        bottomPanel.add(sendMoveButton, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        setSize(500, 500); // set size of window
        setVisible(true); // show window
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
            try {
                processMessage(inputFromServer.readUTF());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void processMessage(String message) throws InterruptedException {
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
            
        } else if (message.contains("You Start.")) {
//            inputArea.setEditable(true);
//            sendMoveButton.setEnabled(true);
            thisClientsTurn = true;
            makeMove();
            displayMessage(message);
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
                inputArea.setEditable(true);
                sendMoveButton.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendMoveButtonClick(ActionEvent evt) {
        try {
            serverMessage = Integer.parseInt(inputArea.getText());
            inputArea.setText(null);
            inputArea.setEditable(false);
            sendMoveButton.setEnabled(false);
            outputToServer.writeInt(serverMessage);
            outputToServer.flush();
        } catch (Exception e) {
        }
    }

    //
    private void displayMessage(final String messageToDisplay) {
        //System.out.println("Client>>> " + messageToDisplay);

        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                displayArea.append(messageToDisplay + "\n"); // updates output
            } // end method run
        } // end inner class
        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage

    public static void main(String[] args) {
        NimMultithreadClient testClient; //connection from localhost

        if (args.length == 0) {
            testClient = new NimMultithreadClient("127.0.0.1"); // localhost
            testClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            testClient = new NimMultithreadClient(args[0]); // use args
            testClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }
}
