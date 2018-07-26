/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Paul Iudean
 */
public class NimMultithreadClient extends JFrame implements Runnable {

    private JTextArea displayArea;
    private JTextArea inputArea;

    private JButton sendMoveButton;

    private JPanel bottomPanel;

    private String hostName;
    private Socket connection;
    private DataInputStream inputFromServer;
    private DataOutputStream outputToServer;
    private int serverMessage;
    private boolean thisClientsTurn = false;

    public NimMultithreadClient(String host) {
        super("Game of Nim Client");
        this.hostName = host;

        initializeGUIComponents();
        startClient();
    }

    public final void initializeGUIComponents() {

        displayArea = new JTextArea(); // set up JTextArea
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        bottomPanel = new JPanel();

        inputArea = new JTextArea(4, 30);
        inputArea.setEditable(false);

        sendMoveButton = new JButton("Make Move");

        //use lambda expression to replace anonymous inner class creation
        sendMoveButton.addActionListener((ActionEvent evt) -> {
            sendMoveButtonClick(evt);
        });

        //set 
        sendMoveButton.setEnabled(false);

        bottomPanel.add(inputArea); // add container panel
        bottomPanel.add(sendMoveButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        setSize(500, 500); // set size of window
        setVisible(true); // show window
    }

    private void startClient() {
        try {
            //create a new socket connection to the server (port 12345), with 
            //an identifying host name
            connection = new Socket(InetAddress.getByName(hostName), 12345);

            //retrieve IO streams for communication with the server
            inputFromServer = new DataInputStream(connection.getInputStream());
            outputToServer = new DataOutputStream(connection.getOutputStream());

        } catch (IOException iOException) {
            displayMessage("failed to start client");
        }

        Thread readOutputFromServer = new Thread(this);
        readOutputFromServer.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                processMessage(inputFromServer.readUTF());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message) throws InterruptedException {
        // valid move occurred
        if (message.contains("Valid input.")) {
            displayMessage(message);
            thisClientsTurn = false;
        } else if (message.equals("Invalid move, try again")) {
            displayMessage(message);
            thisClientsTurn = true;
            makeMove();
        } else if (message.contains("Playing against server.")) {
            displayMessage(message);
        } else if (message.contains("Second player connected.")) {
            displayMessage(message);
        } else if (message.contains("You Start.")) {
            displayMessage(message);
            thisClientsTurn = true;
            makeMove();
        } else if (message.contains("Opponent took")) {
            displayMessage(message);
            thisClientsTurn = true;
            makeMove();
        } else {
            displayMessage(message);
        }
    }

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
        } catch (IOException | NumberFormatException e) {
            displayMessage("Please type an Integer value");
            inputArea.setText(null);
        }
    }

    //Use lambda expression to update diplay area with the string parameter provided.
    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(() -> {
            displayArea.append(messageToDisplay + "\n");
        });
    }

    public static void main(String[] args) {
        NimMultithreadClient testClient;

        if (args.length == 0) {
            testClient = new NimMultithreadClient("127.0.0.1");
            testClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            testClient = new NimMultithreadClient(args[0]);
            testClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }
}
