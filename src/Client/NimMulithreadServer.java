/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author paul
 */
public class NimMulithreadServer extends JFrame {

    private JTextArea outputArea; // for outputting moves
    int numberOfPlayers;
    private ClientHandler[] clients;
    private ServerSocket server;
    private boolean twoPlayerMode;
    private final int PLAYER_1 = 0, PLAYER_2 = 1;
    private int currentPlayer;
    private Random r;
    private int pile = 100;
    private boolean turnComplete = false;
    private boolean firstMove = true;

    //constructor to start server by creating a socket for 2 clients for multiplayerMode
    public NimMulithreadServer() {
        super("Game of Nim Server");
        try {

            currentPlayer = PLAYER_1;

            //initialise array to hold & handle the clients, 
            //array length specified by user when starting server
            clients = new ClientHandler[identifyClients()];

            //start a server socket with port number, and the nummber of clients specified by the user.
            server = new ServerSocket(12345, clients.length);

            //Diagnostic check if socket is open
            //if (!server.isClosed()) {
                //displayMessage("");
            //}

            //instantiate randomm generator and initialize pile based on user input mode
            r = new Random();
            /////////////////////////////////////////////////////////////////////initializePile();

            ////GUI STUFF
            outputArea = new JTextArea(); // create JTextArea for output
            add(outputArea, BorderLayout.CENTER);
            outputArea.setText("Server socket opened for " + clients.length + " connections\n");

            setSize(500, 500); // set size of window
            setVisible(true); // show window

        } catch (IOException iOException) {
            displayMessage("Server failed to create socket. Exiting.");
            iOException.printStackTrace();
            System.exit(1);
        }

    }

    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() // updates outputArea
            {
                outputArea.append(messageToDisplay + "\n"); // add message
            } // end  method run
        }); // end call to SwingUtilities.invokeLater

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
        NimMulithreadServer server = new NimMulithreadServer();
        server.acceptClientThreads();
    }

    /**
     * *************************************************************************
     *
     * GAME LOGIC METHODS
     *
     *************************************************************************
     */
    public void initializePile() {
        String mode = JOptionPane.showInputDialog(null, "Type 'easy' "
                + "for easy mode, or 'hard' for a challenge.");

        switch (mode.toUpperCase()) {
            case "EASY":
                this.pile = r.nextInt(20 - 2) + 2;
                break;
            case "HARD":
                this.pile = r.nextInt(100 - 2) + 2;
                break;
        }
    }

    public synchronized boolean testRemoveFromPile(int userInputNumber, int player) {

        while (player != currentPlayer) {
            try {
                //turnComplete = false;
                wait();
            } catch (InterruptedException IException) {
                System.out.println(IException.getCause());
            }
        }

        if (userInputNumber <= pile) {
            displayMessage("Old Pile: " + pile);
            this.pile -= userInputNumber;
            displayMessage("New Pile: " + pile);

            currentPlayer = (currentPlayer + 1) % 2;

            clients[currentPlayer].opponentTurnTaken(userInputNumber);
            //turnComplete = true;
            //displayMessage("It is now player " + currentPlayer + "'s turn");
            notify();

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

        public void opponentTurnTaken(int marblesTaken) {
            try {
                outputToClient.writeUTF("Opponent took " + marblesTaken + " marbles from the pile.\n"
                        + "New Pile is " + pile);
            } catch (Exception e) {
            }
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
                    outputToClient.writeUTF("Second player connected.");
                    outputToClient.flush();
                    outputToClient.writeUTF("STARTING PILE: " + pile + "\nRemove from pile.");
                    outputToClient.flush();
                } else if (playerID == PLAYER_2) {
                    outputToClient.writeUTF("STARTING PILE: " + pile);
                    outputToClient.flush();
                } else {
                    outputToClient.writeUTF("Playing against server.");
                    outputToClient.flush();
                }

                while (!gameOver()) {

                    //if (playerID == currentPlayer) {
                    int userInput = inputFromClient.readInt();

                    if (testRemoveFromPile(userInput, playerID)) {

                        displayMessage("Player " + playerID + " Subtracted " + userInput
                                + " from " + (pile + userInput) + "."
                                + " New pile is: " + pile);
                        outputToClient.writeUTF("Valid input. New pile is: " + (pile));
                        outputToClient.flush();
                        //inputFromClient.reset();
                    } else {
                        outputToClient.writeUTF("Invalid move, try again\n");
                        outputToClient.flush();
                    }
                    //}

//                    if ((turn % 2 == 0) && (playerID == PLAYER_1)) {
//                        //make move
//                        int userInput = inputFromClient.readInt();
//
//                        if (testRemoveFromPile(userInput)) {
//
//                            displayMessage("Player " + playerID + " Subtracted " + userInput
//                                    + " from " + (pile + userInput) + "."
//                                    + " New pile is: " + pile);
//                            outputToClient.writeUTF("Valid input. New pile is: " + (pile));
//                            outputToClient.flush();
//                        } else {
//                            outputToClient.writeUTF("Invalid move, try again\n");
//                            outputToClient.flush();
//                        }
//                        //increment turn
//                        turn++;
//                    } else {
//                        return;
//                    }
//
//                    if ((turn % 2 != 0) && (playerID == PLAYER_2)) {
//                        //make move
//                        int userInput = inputFromClient.readInt();
//
//                        if (testRemoveFromPile(userInput)) {
//
//                            displayMessage("Player " + playerID + " Subtracted " + userInput
//                                    + " from " + (pile + userInput) + "."
//                                    + " New pile is: " + pile);
//                            outputToClient.writeUTF("Valid input. New pile is: " + (pile));
//                            outputToClient.flush();
//                        } else {
//                            outputToClient.writeUTF("Invalid move, try again\n");
//                            outputToClient.flush();
//                        }
//                        //increment turn
//                        turn++;
//                    } else {
//                        return;
//                    }
                }
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void displayPile(int pile) {
            //outputToClient.write(pile);
        }

        //this method will toggle suspended state
        private void setSuspended(boolean b) {
            suspended = b;
        }

    }
}