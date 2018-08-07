package Server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Paul Iudean
 */
public class MultiClientNimServer extends JFrame {

    private JTextArea outputArea; // for outputting moves

    int numberOfPlayers;
    private ClientHandler[] clients;
    private ServerSocket server;
    private boolean twoPlayerMode;
    private final int PLAYER_1 = 0;
    private final int PLAYER_2 = 1;
    private int currentPlayer;
    private Random r;
    private int randomCounter = 1;
    private int pile;

    /**
     * Constructor to start server by creating a socket for 2 clients for multiplayerMode,
     * initialize the pile to start the game with, and initialize the GUI.
     */
    public MultiClientNimServer() {
        super("Game of Nim Server");
        try {

            //initialize array to hold & handle the clients, 
            //array length specified by user when starting server
            clients = new ClientHandler[identifyClients()];

            /*
            * Creates a server socket and binds it to the specified local port number
            * the second parameter is the connection queue length, specified by the
            * length of the clients array.
            */
            
            server = new ServerSocket(12345, clients.length);

            //instantiate random generator
            r = new Random();

            //initialize pile based on user input mode
            initializePile();

            //initialize GUI components
            initializeGUIComponents();

        } catch (IOException iOException) {
            displayMessage("Server failed to create socket. Exiting.");
            System.exit(1);
        }
    }

    /**
     * Initialize Components of the GUI
     */
    private void initializeGUIComponents() {
        outputArea = new JTextArea(); // create JTextArea for output
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        outputArea.setText("Server socket opened for " + clients.length
                + " connections\n");
        setSize(500, 500); // set size of window
        setBackground(Color.yellow);
        setVisible(true); // show window
    }

    /**
     * Update the GUI display with a string passed as a parameter when called
     * 
     * @param messageToDisplay String to be displayed
     */
    private void displayMessage(final String messageToDisplay) {
        //use a lambda expression to add the message passed as a parameter to
        //the output area GUI component.
        SwingUtilities.invokeLater(() -> {
            outputArea.append(messageToDisplay + "\n");
        });

        //also display the message in the console output.
        System.out.println("SERVER>>> " + messageToDisplay);
    }

    /**
     * This recursive method will get the number of players that would be 
     * connecting to the server socket.
     * 
     * @return numberOfPlayers - this will be used when defining the length
     * of the clients array, and also when defining the server socket connection queue
     */
    private int identifyClients() {
        try {
            this.numberOfPlayers = Integer.parseInt(JOptionPane.showInputDialog("Enter how many players you want to play - 1 or 2"));

        } catch (NumberFormatException NFormatException) {
            //catch the number format exception and handle it - informing user
            //of mistake and calling the method again to retry.
            JOptionPane.showMessageDialog(null, "Do not enter letters or symbols."
                    + " Enter only the INTEGER 1 or 2.\n");
            identifyClients();
        }
        //check whether the user input is 1 or 2, otherwise call method again.
        switch (numberOfPlayers) {

            case 2:
                //if 2 then the two player mode is true, this will be used
                //in further logic implementation.
                twoPlayerMode = true;

                //update server GUI display with the result of the method call
                //(numberofplayers).
                displayMessage("Server socket attempting to open for "
                        + numberOfPlayers + " connections.");

                //as the two player mode was selected, call the setRandomStartPlayer
                //method to select a player to have the first move for balanced gameplay
                //with random starting advantage bias.
                setRandomStartPlayer();
                break;
            case 1:
                //in this case the user selected there is 
                twoPlayerMode = false;
                displayMessage("Server socket attempting to open for "
                        + numberOfPlayers + " connection.");
                break;
            default:
                JOptionPane.showMessageDialog(null, numberOfPlayers + " is the wrong"
                        + " input. Trying again");
                identifyClients();
                break;
        }
        return numberOfPlayers;
    }

    /**
     * Start a thread for each client, based on the number of clients specified
     * by user assign an identifying integer to the client (0 for player 1, 1
     * for player 2)
     */
    public void acceptClientThreads() {
        //open connection threads for the amount of clients in the clients array
        for (int i = 0; i < clients.length; i++) {
            try {
                //display information in server GUI
                displayMessage("waiting for client connection to socket");

                //create a new client handling object thread, calling the 
                //server.accept method to wait for a client to open.
                //store the instance in the clients array.
                clients[i] = new ClientHandler(server.accept(), i);

                //call the start mathod of the ClientHandler to start the thread
                //when a client has opened
                clients[i].start();

                //display thread creation information in server GUI
                displayMessage("Thread: " + clients[i] + " created for client "
                        + (i + 1));

            } catch (IOException iOException) {
                JOptionPane.showMessageDialog(null, "Error opening client threads, Exiting.");
                System.exit(1);
            }
        }

        synchronized (clients[PLAYER_1]) {
            clients[PLAYER_1].setSuspended(false);
            clients[PLAYER_1].notify();
        }
    }

    
     /***************************************************************************
     *
     * GAME LOGIC METHODS
     *
     * These are logic methods that do not use the in and output streams and
     * which can modify the pile variable stored in the server.
     *
     *****************************************************************************
     */
    
    /**
     * Retrieve user preference for the size of the starting pile
     */
    private void initializePile() {
        String mode = JOptionPane.showInputDialog(null, "Type 'easy' "
                + "for easy mode, or 'hard' for a challenge.");
        switch (mode.toUpperCase()) {
            //set the size of the marble pile to adhere to the game guidelines of 
            //difficulty - randomly generated piles based on boundaries
            case "EASY":
                this.pile = r.nextInt(20 - 2) + 2;
                break;
            case "HARD":
                this.pile = r.nextInt(100 - 2) + 2;
                break;
            default:
                //handle error informing user of the issue and retrying the method.
                JOptionPane.showMessageDialog(null, "Do not type anything other than"
                        + "'easy' or 'hard'");
                initializePile();
        }
    }

    /**
     * This method will at random determine which player should get the first
     * move.
     * @return Nothing.
     */
    private void setRandomStartPlayer() {

        //ensure the starter is chosen only once, avoiding a logic error 
        //that previously opccured
        if (randomCounter == 1) {
            //as math.random generates a random float between 0 and 1, 
            //this can be used to make two random outcomes (if the random
            //number is below or above 0.5.
            if (Math.random() < 0.5) {
                //if less than 0.5 set currentplayer (the players turn) to Player 1
                currentPlayer = PLAYER_1;
                //decrement randomCounter to avoid running twice
                randomCounter--;
            } else {
                //if more than 0.5 set currentplayer (the players turn) to Player 2
                currentPlayer = PLAYER_2;
                //decrement randomCounter to avoid running twice
                randomCounter--;
            }
        }
    }

    /**
     * As this method will have the possibility of changing the global pile
     * variable when called, it must be synchronised. This is because the pile
     * variable is visible to more than one thread and any thread can read or
     * write to this variable when this is called.
     *
     * @param userInputNumber The amount of marbles the user has input to be taken from the pile.
     * @param player The player number that made the move.
     * @return
     */
    public synchronized boolean checkMoveAndRemoveFromPile(int userInputNumber,
            int player) {
        /*
        * To avoid thread interferance or consistency errors between clients,
        * tell the thread (client) whose turn it is not to wait until the current
        * players turn is up.
         */
        while (player != currentPlayer) {
            try {
                /* 
                * Wait until the player whose turn it is currently ot perform a valid move,
                * therefore calling the notify() method, allowing the player whose thread
                * was set to 'wait' to proceed.
                 */
                wait();
            } catch (InterruptedException IException) {
                System.out.println(IException.getCause());
            }
        }

        //Compare the input number to the game logic for a valid move. 
        if ((userInputNumber >= 1) && (userInputNumber <= pile / 2)) {

            //If valid, deduct the user amount from the pile variable, 
            //display information on serverGUI
            displayMessage("Old Pile: " + pile);
            this.pile -= userInputNumber; //deduct from pile
            displayMessage("New Pile: " + pile);

            //Alternate the current player (turn to be taken) to the other plient instance
            currentPlayer = (currentPlayer + 1) % 2;

            //display information on serverGUI
            clients[currentPlayer].opponentTurnTaken(userInputNumber);

            //notify the thread in the wait state that it can run now.
            notify();

            //return true to pass the if condition.
            return true;
        } else {
            //return false to fail the if condition and tell the user they made
            //an invalid move.
            return false;
        }
    }


    /**
     * As this method will only be called in the context of a single client thread
     * running, there is no risk of thread interference, and therefore does not
     * need to be synchronized.
     * @param userInputNumber
     * @return
     */
    public boolean checkMoveSinglePlayer(int userInputNumber) {

        //perform the same logic as method above without the multiplayer functionality
        if ((userInputNumber >= 1) && (userInputNumber <= pile / 2)) {
            displayMessage("Old Pile: " + pile);
            this.pile -= userInputNumber;
            displayMessage("New Pile: " + pile);
            return true;
        } else {
            return false;
        }
    }

    /**
     * End the game when the pile remaining is 1. The player whose turn it is
     * (currentPlayer) will have lost the game.
     * @return return true only when pile is 1.
     */
    public boolean gameOver() {
        return this.pile == 1;
    }

    /**
     * CLIENT HANDLING INNER CLASS -
     *
     * private inner class ClientHandler will create and assign threads to each
     * incoming client request, extending Thread so that its objects have all
     * the properties of Thread.
     *
     */
    private class ClientHandler extends Thread {

        private final Socket connection;
        private final int playerID;
        private DataInputStream inputFromClient;
        private DataOutputStream outputToClient;
        protected boolean suspended = true;
        private boolean serverWin = false;

        /**
         * Constructor takes socket address and the identifying integer of 
         * the client - used to distinguish clients in game logic operations.
         * @param socket
         * @param clientNumber 
         */
        public ClientHandler(Socket socket, int clientNumber) {
            //set the passed parameter as the player ID
            playerID = clientNumber;
            
            //initialize the socket 'connection' to the passed socket to bind the
            //connection to the client.
            connection = socket;
            
            //call the method that gets the input and output streams
            obtainIOStreams();
        }

        /**
         * This method will connect the client and server communication channels
         */
        private void obtainIOStreams() {
            try {
                //get input and output streams of the server, calling the getters
                //on the socket passed through the constructor, set these to the variables.
                inputFromClient = new DataInputStream(connection.getInputStream());
                outputToClient = new DataOutputStream(connection.getOutputStream());
            } catch (IOException iOException) {
                displayMessage("ERROR GETTING IO STREAMS");
                System.exit(1);
            }
            //display the confirmation of connection between the client and server
            //on the server GUI
            displayMessage("Obtained IO Streams");
        }

        /**
         * This method will let the client know that the opponent moved, and to perform
         * the relevant actions specified in the processMessage() method in the client class.
         * 
         * @param marblesTaken The amount the pile has decreased by after opponent turn
         *                      has been taken
         */
        private void opponentTurnTaken(int marblesTaken) {
            try {
                //write a string to the output stream and flush to clear the stream.
                outputToClient.writeUTF("Opponent took " + marblesTaken
                        + " marbles from the pile.\n"
                        + "New Pile is " + pile);
                outputToClient.flush();
            } catch (IOException e) {
            }
        }

        /**
        * When called this method will let the client know that they have lost,
        * and the actions specified in the processMessage() method of the client class
        * will handle ending the game (visually, and terminating the connection)
         */
        private void youLoseMessage() {
            try {
                //write a string to the output stream and flush to clear the stream.
                outputToClient.writeUTF("You Lose.");
                outputToClient.flush();
            } catch (IOException e) {
            }
        }

        /**
        * When called this method will let the client know that they have won,
        * and the actions specified in the processMessage() method of the client class
        * will handle ending the game (visually, and terminating the connection)
         */
        private void youWinMessage() {
            try {
                //write a string to the output stream and flush to clear the stream.
                outputToClient.writeUTF("You Win!");
                outputToClient.flush();
            } catch (IOException e) {
            }
        }

        /**
         * As this is a subclass of Thread, the run() method is overridden
         */
        @Override
        public void run() {
            try {
                //This is the first message that will appear after the server is started
                //and has accepted a connection.
                displayMessage("Client " + playerID + " from " + connection
                        + " has connected.");

                //This is the first message that will appear on the client side GUI
                outputToClient.writeUTF("PLAYER " + (playerID + 1) + " CONNECTED.");
                outputToClient.flush();

                /*
                * Check whether or not two player mode is true and the ID of the client
                * that just connected is '0', this condition is meant only for the first
                * client that connects and will display messages intended for the first.
                 */
                if (twoPlayerMode && (playerID == PLAYER_1)) {

                    //Tell the first connected client that another player must connect
                    //in order to start playing.
                    outputToClient.writeUTF("Waiting for another player to connect.");
                    outputToClient.flush();

                    /*
                    * set the thread of the first client to wait for the notify()
                    * method to be called once the for loop in acceptClientThreads()
                    * is complete (meaning two clients have connected.)
                     */
                    try {
                        synchronized (this) {
                            while (suspended) {
                                wait();
                            }
                        }
                    } catch (InterruptedException IException) {
                    }

                    //Once two have connected in the acceptClientThreads method,
                    //inform the user.
                    outputToClient.writeUTF("Second player connected."
                            + "\nSTARTING PILE: " + pile + "\n");
                    outputToClient.flush();

                    //If the client is the second client (playerID is 2)
                    //inform them of the starting pile size
                } else if (playerID == PLAYER_2) {
                    outputToClient.writeUTF("STARTING PILE: " + pile + "\n");
                    outputToClient.flush();
                    //Update the GUI dsplay of the pile size in both clients at once.
                    displayPileIn2Clients();
                } else {
                    //As the game is in single player mode, there is only the one
                    //client. Inform them of the starting pile size
                    outputToClient.writeUTF("STARTING PILE: " + pile
                            + "\nPlaying against server.");
                    outputToClient.flush();
                    //because the is only one player playing, display the pile in
                    //the only client open.
                    displayPileInClient();
                }

                //Inform the the randomly chosen player it is their turn to start.
                if (playerID == currentPlayer) {
                    outputToClient.writeUTF("You Start.");
                    outputToClient.flush();
                }

                //Run the game until the gameOver method returns false using a while
                //loop
                while (!gameOver()) {
                    //If the game is in two player mode, run the logic for two clients
                    //to play.
                    if (twoPlayerMode) {
                        twoPlayerGameRun();
                        //keep updating the pile size after every turn for a 
                        //smooth visual feel
                        displayPileIn2Clients();
                    } else {
                        //run the logic for two clients to play.
                        singlePlayerGameRun();
                    }
                }

                /*
                * when the game is over and gameOver() has returned false,
                * inform both clients (if in two player mode) of the results
                * after 10 seconds, close the connection to the client and also
                * the window.
                 */
                if (twoPlayerMode) {
                    try {
                        clients[currentPlayer].youLoseMessage();
                        clients[(currentPlayer + 1) % 2].youWinMessage();
                        displayMessage("Someone won the game.\n\n"
                                + "Closing server in 10 seconds.");
                        connection.close();
                        Thread.sleep(10000);
                        System.exit(1);
                    } catch (IOException | InterruptedException e) {
                    }
                }
            } catch (IOException | InterruptedException e) {
            }
        }

        /**
         * Gameplay logic for two clients playing against eachother.
         */
        private void twoPlayerGameRun() {
            try {
                //take the input from the client dataInputStream and store it in a
                //local variable.
                int userInput = inputFromClient.readInt();

                /*
                * Verify the amount to be taken from the pile and make the move
                * by calling the checkMoveAndRemoveFromPile method. If true is
                * returned, the user will be notified of the valid move, 
                * otherwise notify of an invalid move.
                 */
                if (checkMoveAndRemoveFromPile(userInput, playerID)) {
                    displayMessage("Player " + (playerID + 1) + " Subtracted "
                            + userInput + " from " + (pile + userInput) + "."
                            + " New pile is: " + pile + "\n");
                    outputToClient.writeUTF("\nValid input. You took "
                            + userInput + " marbles. \nNew pile is: "
                            + pile + "\n");
                    outputToClient.flush();
                } else {
                    outputToClient.writeUTF("Invalid move, try again");
                    outputToClient.flush();
                }
            } catch (IOException e) {
            }
        }

        /**
         * Gameplay logic for one client playing against a smart server.
         * 
         * @throws InterruptedException When the thread is sleeping.
         */
        private void singlePlayerGameRun() throws InterruptedException {
            try {

                //take the input from the client dataInputStream and store it in a
                //local variable.
                int userInput = inputFromClient.readInt();

                /*
                * Verify the amount to be taken from the pile and make the move
                * by calling the checkMoveSinglePlayer method. If true is
                * returned, the user will be notified of the valid move, 
                * otherwise notify of an invalid move.
                 */
                if (checkMoveSinglePlayer(userInput)) {

                    displayMessage("Player " + (playerID + 1) + " Subtracted "
                            + userInput + " from " + (pile + userInput) + "."
                            + " New pile is: " + pile + "\n");
                    outputToClient.writeUTF("\nYou took " + userInput
                            + " marbles. \nNew pile is: " + pile + "\n");
                    outputToClient.flush();
                    
                    //Display the pile in the client GUI
                    displayPileInClient();

                    //As the client has made a successful move, tell the server
                    //to make its move
                    serverMove(pile);
                    
                    //Display the new pile in the client GUI after server move
                    displayPileInClient();

                    //This check will run after every server move. If the server
                    //has taken its turn and the pile becomes 1, the server has won.
                    if (pile == 1) {
                        serverWin = true;
                        clients[currentPlayer].youLoseMessage();
                        displayMessage("Server has won the game.\n\n"
                                + "Closing server in 10 seconds.");
                        connection.close();
                        Thread.sleep(10000);
                        System.exit(1);
                    }

                    //If the server hasnt won, tell client to take its turn 
                    if (!serverWin) {
                        outputToClient.writeUTF("Your turn.");
                        outputToClient.flush();
                    }

                } else {
                    outputToClient.writeUTF("Invalid move, try again");
                    outputToClient.flush();
                }
            } catch (IOException exception ) {
            }
        }

        /**
         * Gameplay logic for the server's response to a clients move.
         * 
         * @param currentPileSize Used for calculation of the servers next move,
         *                        and displaying the pile visually.
         * @throws InterruptedException Thrown when the thread is sleeping.
         */
        private void serverMove(int currentPileSize) throws InterruptedException {
            try {
                //This check will run after every Client move. If the client
                //has taken a turn and the pile becomes 1, the client has won.
                //display the appropriate message.
                if (currentPileSize == 1) {
                    clients[currentPlayer].youWinMessage();
                    displayMessage("Opponent has won the game.\n\n"
                            + "Closing server in 10 seconds.");
                    connection.close();
                    Thread.sleep(10000);
                    System.exit(1);
                } else {
                    
                    //calculate a move that is half of the pile size
                    int serverMoveAmount = Math.round(currentPileSize / 2);

                    //tell the user the server is 'thinking' and pause the thread
                    //for differing periods between 1 and 4 seconds to look realistic
                    Thread.sleep(500);
                    outputToClient.writeUTF("Server is thinking...\n");
                    outputToClient.flush();
                    Thread.sleep(r.nextInt(4000 - 1000) + 1000);

                    displayMessage("Old Pile: " + pile);
                    pile -= serverMoveAmount;
                    displayMessage("New Pile: " + pile);
                    displayMessage("Server took " + serverMoveAmount
                            + " marbles off the pile. New pile is: " + pile + "\n");

                    outputToClient.writeUTF("Server took " + serverMoveAmount
                            + " marbles off the pile.\nNew pile is: " + pile);
                    outputToClient.flush();
                }
            } catch (IOException e) {
            }
        }

        /**
         * Add the pile size to the dataOutputStream of the method is called on.
         */
        private void displayPile() {
            try {
                outputToClient.writeUTF("pile:" + pile);
                outputToClient.flush();
            } catch (IOException e) {
            }
        }

        /**
         * This method will be called in the context of two player game (non server)
         * calling the displayPile() method on the two instances of ClientHandler
         * stored in the clients[] array - these are the two clients.
         * 
         * @see displayPile()
         */
        private void displayPileIn2Clients() {
            clients[currentPlayer].displayPile();
            //called again but on the alternate client
            clients[(currentPlayer + 1) % 2].displayPile();
        }
        
        /**
         * This method will be called in the context of a single player game (with server)
         * calling the displayPile() method above on the instance of ClientHandler
         * that was initialized.
         * 
         * @see displayPile()
         */
        private void displayPileInClient() {
            clients[currentPlayer].displayPile();
        }

        /**
         * This method will toggle suspended state of a thread
         * 
         * @param b 
         */
        private void setSuspended(boolean b) {
            suspended = b;
        }
    }
}
