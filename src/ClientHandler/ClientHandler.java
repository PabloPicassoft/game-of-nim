//package ClientHandler;
//
//import Server.NimMulithreadServer;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//
//public class ClientHandler extends Thread {
//
//        private NimMulithreadServer server;
//        private final Socket connection;
//        private final int playerID;
//        private DataInputStream inputFromClient;
//        private DataOutputStream outputToClient;
//        protected boolean suspended = true;
//
//        //constructor takes server socket address and the identifying int of 
//        //the client - used to distinguish clients in game logic operations.
//        public ClientHandler(Socket socket, int clientNumber) {
//            server = new NimMulithreadServer();
//            
//            playerID = clientNumber;
//            connection = socket;
//
//            obtainIOStreams();
//        }
//
//        private void obtainIOStreams() {
//            try {
//                inputFromClient = new DataInputStream(connection.getInputStream());
//                outputToClient = new DataOutputStream(connection.getOutputStream());
//            } catch (IOException iOException) {
//                server.displayMessage("ERROR GETTING IO STREAMS");
//                System.exit(1);
//            }
//            server.displayMessage("Obtained IO Streams");
//        }
//
//        public void opponentTurnTaken(int marblesTaken) {
//            try {
//                outputToClient.writeUTF("Opponent took " + marblesTaken
//                        + " marbles from the pile.\n"
//                        + "New Pile is " + server.pile);
//                outputToClient.flush();
//            } catch (IOException e) {
//            }
//        }
//        
//        public void youLose() {
//            try {
//                outputToClient.writeUTF("You lose.");
//                outputToClient.flush();
//            } catch (IOException e) {
//            }
//        }
//
//        @Override
//        public void run() {
//            try {
//                server.displayMessage("Client " + playerID + " from " + connection
//                        + " has connected.");
//                outputToClient.writeUTF("PLAYER " + (playerID + 1) + " CONNECTED.");
//                outputToClient.flush();
//
//                if (server.twoPlayerMode && (playerID == server.PLAYER_1)) {
//
//                    outputToClient.writeUTF("Waiting for another player to connect.");
//                    outputToClient.flush();
//
//                    try {
//                        synchronized (this) {
//                            while (suspended) {
//                                wait();
//                            }
//                        }
//                    } catch (InterruptedException IException) {
//                    }
//                    outputToClient.writeUTF("Second player connected.");
//                    outputToClient.flush();
//                    outputToClient.writeUTF("STARTING PILE: " + server.pile + "\n");
//                    outputToClient.flush();
//                } else if (playerID == server.PLAYER_2) {
//                    outputToClient.writeUTF("STARTING PILE: " + server.pile + "\n");
//                    outputToClient.flush();
//                } else {
//                    outputToClient.writeUTF("Playing against server.");
//                    outputToClient.flush();
//                }
//
//                //inform the the randomly chosen player it is their turn to start.
//                if (playerID == server.currentPlayer) {
//                    outputToClient.writeUTF("You Start.");
//                    outputToClient.flush();
//                }
//
//                while (!server.gameOver()) {
//                    if (server.twoPlayerMode) {
//                        twoPlayerGameRun();
//                    } else {
//                        singlePlayerGameRun();
//                    }
//                }
//                connection.close();
//            } catch (IOException e) {
//            }
//        }
//
//        private void twoPlayerGameRun() {
//            try {
//                int userInput = inputFromClient.readInt();
//
//                if (server.checkMoveAndRemoveFromPile(userInput, playerID)) {
//
//                    server.displayMessage("Player " + (playerID + 1) + " Subtracted "
//                            + userInput + " from " + (server.pile + userInput) + "."
//                            + " New pile is: " + server.pile + "\n");
//                    outputToClient.writeUTF("\nValid input. You took "
//                            + userInput + " marbles.  New pile is: "
//                            + server.pile + "\n");
//                    outputToClient.flush();
//                } else {
//                    outputToClient.writeUTF("Invalid move, try again");
//                    outputToClient.flush();
//                }
//            } catch (Exception e) {
//            }
//        }
//
//        private void singlePlayerGameRun() {
//            try {
//                int userInput = inputFromClient.readInt();
//
//                if (server.checkMoveSinglePlayer(userInput)) {
//
//                    server.displayMessage("Player " + (playerID + 1) + " Subtracted "
//                            + userInput + " from " + (server.pile + userInput) + "."
//                            + " New pile is: " + server.pile + "\n");
//                    outputToClient.writeUTF("\nYou took " + userInput 
//                            + " marbles.  New pile is: " + server.pile + "\n");
//                    outputToClient.flush();
//                    
//                    ////change this to be the notification of server move 
//                    ////completion (when server moves are implemeted)
//                    outputToClient.writeUTF("Your turn.");
//                    outputToClient.flush();
//                } else {
//                    outputToClient.writeUTF("Invalid move, try again");
//                    outputToClient.flush();
//                }
//            } catch (Exception e) {
//            }
//        }
//        
//        private int serverMove(int currentPileSize) {
//            int smartmove = currentPileSize;
//            int randomLegalMove;
//            
//            
//            
//            int serverMoveAmount = 0;
//            return serverMoveAmount;
//        }
//
//        private void displayPile(int pile) {
//            //outputToClient.write(pile);
//        }
//
//        //this method will toggle suspended state of a thread
//        private void setSuspended(boolean b) {
//            suspended = b;
//        }
//    }