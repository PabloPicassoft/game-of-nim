/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 *
 * @author paul
 */
public class TestMulithreadServer {

    int numberOfPlayers;
    private ClientHandler[] clients;
    private ServerSocket server;
    private boolean twoPlayerMode;
    private final int PLAYER_1 = 0, PLAYER_2 = 1;
    private int firstPlayer;
    private int pile = 100;

    //constructor to start server by creating a socket for 2 clients for multiplayerMode
    public TestMulithreadServer() {

        try {

            firstPlayer = PLAYER_1;

            //initialise array to hold & handle the clients, 
            //array length specified by user when starting server
            clients = new ClientHandler[identifyClients()];

            //start a server socket with port number, and the nummber of clients specified by the user.
            server = new ServerSocket(12345, clients.length);

            //Diagnostic check if socket is open
            if (!server.isClosed()) {
                displayMessage("Server socket opened for " + clients.length + " connections");
            }
        } catch (IOException iOException) {
            displayMessage("Server failed to create socket. Exiting.");
            iOException.printStackTrace();
            System.exit(1);
        }

    }

    private void displayMessage(final String messageToDisplay) {
        /*
            SwingUtilities.invokeLater(
                new Runnable() {
            public void run() // updates outputArea
            {
                outputArea.append(messageToDisplay); // add message
            } // end method run
        } // end inner class

        ); // end call to SwingUtilities.invokeLater
         */
        System.out.println("SERVER>>> " + messageToDisplay);
    }

    //This method will get the number of players that need sockets in the opened for.
    public int identifyClients() {

        try {
            this.numberOfPlayers = Integer.parseInt(JOptionPane.showInputDialog("Enter how many players you want to play - 1 or 2"));
        } catch (NumberFormatException NFormatException) {
            JOptionPane.showMessageDialog(null, "Do not enter letters or symbols. Enter only the INTEGER 1 or 2.\n");
            identifyClients();
        }

        if (numberOfPlayers == 2) {
            twoPlayerMode = true;
            displayMessage("Server socket attempting to open for " + numberOfPlayers + " connections.");
        } else if (numberOfPlayers == 1) {
            twoPlayerMode = false;
            displayMessage("Server socket attempting to open for " + numberOfPlayers + " connection.");
        } else {
            JOptionPane.showMessageDialog(null, numberOfPlayers + " is the wrong input. Trying again");
            identifyClients();
        }

        return numberOfPlayers;
    }

    //start a thread for each client, based on the number of clients specified by user
    //assign an identifying integer to the client
    public void acceptClientThreads() {
        for (int i = 0; i < clients.length; i++) {
            try {
                displayMessage("waiting for client connection to socket");
                clients[i] = new ClientHandler(server.accept(), i);
                clients[i].start();
                displayMessage("Thread: " + clients[i] + " created for client " + i);
            } catch (Exception iOException) {
                displayMessage("Client.TestMulithreadServer.executeClientThread() failed to run");
                iOException.printStackTrace();
                System.exit(1);
            }
        }

        synchronized (clients[PLAYER_1]) {
            clients[PLAYER_1].setSuspended(false);
            clients[PLAYER_1].notify();
        }
    }

    public static void main(String[] args) {
        TestMulithreadServer server = new TestMulithreadServer();
        server.acceptClientThreads();
    }

    /**
     * *************************************************************************
     *
     * GAME LOGIC METHODS
     *
     *************************************************************************
     */
    public int initializePile() {

        return 0;
    }

    public synchronized boolean testRemoveFromPile(int userInputNumber) {
        if (userInputNumber <= pile) {
            displayMessage("Old Pile: " + pile);
            this.pile -= userInputNumber;
            displayMessage("New Pile: " + pile);
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean verifyAndRemoveFromPile() {

        return false;
    }

    public boolean gameOver() {
        if (this.pile <= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * *************************************************************************
     *
     * CLIENT HANDLING INNER CLASS
     *
     *************************************************************************
     */
    //private inner class clienthandler will create and assign threads to each incoming client request
    private class ClientHandler extends Thread {

        private Socket connection;
        private DataInputStream inputFromClient;
        private DataOutputStream outputToClient;
        private int playerID;
        private String messageString;
        protected boolean suspended = true;

        //constructor takes server socket address and the identifying int of the client
        public ClientHandler(Socket socket, int clientNumber) {
            playerID = clientNumber;
            connection = socket;

            obtainIOStreams();
        }

        public void obtainIOStreams() {
            try {
                inputFromClient = new DataInputStream(connection.getInputStream());
                outputToClient = new DataOutputStream(connection.getOutputStream());
            } catch (IOException iOException) {
                iOException.printStackTrace();
                displayMessage("ERROR GETTING IO STREAMS");
                System.exit(1);
            }
            displayMessage("Obtained IO Streams");
        }

        @Override
        public void run() {
            try {
                displayMessage("Client " + playerID + " from " + connection + " has connected.");
                outputToClient.writeUTF("PLAYER " + playerID + " CONNECTED.");
                outputToClient.flush();


                if (twoPlayerMode && (playerID == PLAYER_1)) {
                    outputToClient.writeUTF("Waiting for another player to connect.");
                    outputToClient.flush();
                   
                    try {
                        synchronized (this) {
                            while (suspended) {
                                wait();
                            }
                        }
                    } catch (InterruptedException IException) {
                        IException.printStackTrace();
                    }
                    outputToClient.writeUTF("Second player connected. Remove from pile.");
                    outputToClient.flush();
                } else {
                    outputToClient.writeUTF("Playing against server. Make your move.");
                    outputToClient.flush();
                }
                while (!gameOver()) {
                    int userInput = inputFromClient.readInt();
                    //inputFromClient.readInt();
                    //testRemoveFromPile(userInput);
                    if (testRemoveFromPile(userInput)) {
                        displayMessage("Player " + playerID + " Subtracted " + userInput
                                + " from " + (pile + userInput) + "."
                                + " New pile is: " + pile);
                        outputToClient.writeUTF("Valid input. New pile is: " + (pile));  
                        outputToClient.flush();
                    } else {
                        outputToClient.writeUTF("Invalid move, try again\n");
                        outputToClient.flush();
                    }
                }
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //this method will toggle suspended state
        private void setSuspended(boolean b) {
            suspended = b;
        }

    }
}
