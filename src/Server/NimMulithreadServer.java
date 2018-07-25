/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

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
import javax.swing.JScrollPane;
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
    private int randomCounter = 1;
    private int pile = 100;
//    private boolean turnComplete = false;
//    private boolean firstMove = true;

    //constructor to start server by creating a socket for 2 clients for multiplayerMode
    public NimMulithreadServer() {
        super("Game of Nim Server");
        try {

            currentPlayer = PLAYER_1;

            //initialise array to hold & handle the clients, 
            //array length specified by user when starting server
            clients = new ClientHandler[identifyClients()];

            //start a server socket with port number, and the nummber of clients
            //specified by the user.
            server = new ServerSocket(12345, clients.length);

            //instantiate randomm generator and initialize pile based on user input mode
            r = new Random();
            
            initializePile();
            
            initializeGUIComponents();
            
        } catch (IOException iOException) {
            displayMessage("Server failed to create socket. Exiting.");
            iOException.printStackTrace();
            System.exit(1);
        }

    }

    private void initializeGUIComponents() {
        
            outputArea = new JTextArea(); // create JTextArea for output
            add(new JScrollPane(outputArea), BorderLayout.CENTER);
            outputArea.setText("Server socket opened for " + clients.length 
                    + " connections\n");

            setSize(500, 500); // set size of window
            setVisible(true); // show window
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
            this.numberOfPlayers = Integer.parseInt(JOptionPane.showInputDialog
        ("Enter how many players you want to play - 1 or 2"));
        } catch (NumberFormatException NFormatException) {
            JOptionPane.showMessageDialog(null, "Do not enter letters or symbols."
                    + " Enter only the INTEGER 1 or 2.\n");
            identifyClients();
        }

        if (numberOfPlayers == 2) {
            twoPlayerMode = true;
            displayMessage("Server socket attempting to open for " 
                    + numberOfPlayers + " connections.");
        } else if (numberOfPlayers == 1) {
            twoPlayerMode = false;
            displayMessage("Server socket attempting to open for " 
                    + numberOfPlayers + " connection.");
        } else {
            JOptionPane.showMessageDialog(null, numberOfPlayers + " is the wrong"
                    + " input. Trying again");
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
                displayMessage("Thread: " + clients[i] + " created for client " 
                        + (i+1));
            } catch (Exception iOException) {
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
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    public synchronized boolean checkMoveAndRemoveFromPile(int userInputNumber,
            int player) {

        while (player != currentPlayer) {
            try {
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
    //private inner class clienthandler will create and assign threads 
    //to each incoming client request
    private class ClientHandler extends Thread {

        private Socket connection;
        private DataInputStream inputFromClient;
        private DataOutputStream outputToClient;
        private int playerID;
        private String messageString;
        protected boolean suspended = true;

        //constructor takes server socket address and the identifying int of 
        //the client
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
                outputToClient.writeUTF("\nOpponent took " + marblesTaken 
                        + " marbles from the pile.\n"
                        + "New Pile is " + pile);
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            try {
                displayMessage("Client " + playerID + " from " + connection 
                        + " has connected.");
                outputToClient.writeUTF("PLAYER " + (playerID + 1) + " CONNECTED.");
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
                    outputToClient.writeUTF("STARTING PILE: " + pile);
                    outputToClient.flush();
                } else if (playerID == PLAYER_2) {
                    outputToClient.writeUTF("STARTING PILE: " + pile);
                    outputToClient.flush();
                } else {
                    outputToClient.writeUTF("Playing against server.");
                    outputToClient.flush();
                }

                setRandomStartPlayer();
                
                while (!gameOver()) {

                    int userInput = inputFromClient.readInt();

                    if (checkMoveAndRemoveFromPile(userInput, playerID)) {

                        displayMessage("Player " + playerID + " Subtracted " 
                                + userInput + " from " + (pile + userInput) + "."
                                + " New pile is: " + pile + "\n");
                        outputToClient.writeUTF("\nValid input. You took " 
                                + userInput + " marbles.  New pile is: " 
                                + pile + "\n");
                        outputToClient.flush();
                        //inputFromClient.reset();
                    } else {
                        outputToClient.writeUTF("Invalid move, try again");
                        outputToClient.flush();
                    }
                }
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setRandomStartPlayer() {
            try {
                if (randomCounter == 1) {
                    if (Math.random() < 0.5) {
                        currentPlayer = PLAYER_1;
                        clients[0].outputToClient.writeUTF("You Start.");
                        outputToClient.flush();
                        
                        randomCounter--;
                    } else {
                        currentPlayer = PLAYER_2;
                        clients[1].outputToClient.writeUTF("You Start.");
                        outputToClient.flush();
                        
                        randomCounter--;
                    }
                }
            } catch (Exception e) {
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