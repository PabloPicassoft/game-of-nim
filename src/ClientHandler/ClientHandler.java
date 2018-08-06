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
//    private NimMulithreadServer server;
//
//    private final Socket connection;
//    private final int playerID;
//    private DataInputStream inputFromClient;
//    private DataOutputStream outputToClient;
//    protected boolean suspended = true;
//    private boolean serverWin = false;
//
//    //constructor takes server socket address and the identifying int of 
//    //the client - used to distinguish clients in game logic operations.
//    public ClientHandler(Socket socket, int clientNumber) {
//        playerID = clientNumber;
//        connection = socket;
//
//        obtainIOStreams();
//    }
//
//    private void obtainIOStreams() {
//        try {
//            inputFromClient = new DataInputStream(connection.getInputStream());
//            outputToClient = new DataOutputStream(connection.getOutputStream());
//        } catch (IOException iOException) {
//            server.displayMessage("ERROR GETTING IO STREAMS");
//            System.exit(1);
//        }
//        server.displayMessage("Obtained IO Streams");
//    }
//
//    public void opponentTurnTaken(int marblesTaken) {
//        try {
//            outputToClient.writeUTF("Opponent took " + marblesTaken
//                    + " marbles from the pile.\n"
//                    + "New Pile is " + server.pile);
//            outputToClient.flush();
//        } catch (IOException e) {
//        }
//    }
//
//    public void youLoseMessage() {
//        try {
//            outputToClient.writeUTF("You Lose.");
//            outputToClient.flush();
//        } catch (IOException e) {
//        }
//    }
//
//    public void youWinMessage() {
//        try {
//            outputToClient.writeUTF("You Win!");
//            outputToClient.flush();
//        } catch (IOException e) {
//        }
//    }
//
//    @Override
//    public void run() {
//        try {
//            server.displayMessage("Client " + playerID + " from " + connection
//                    + " has connected.");
//            outputToClient.writeUTF("PLAYER " + (playerID + 1) + " CONNECTED.");
//            outputToClient.flush();
//
//            if (server.twoPlayerMode && (playerID == server.PLAYER_1)) {
//
//                outputToClient.writeUTF("Waiting for another player to connect.");
//                outputToClient.flush();
//
//                try {
//                    synchronized (this) {
//                        while (suspended) {
//                            wait();
//                        }
//                    }
//                } catch (InterruptedException IException) {
//                }
//                outputToClient.writeUTF("Second player connected."
//                        + "\nSTARTING PILE: " + server.pile + "\n");
//                outputToClient.flush();
//            } else if (playerID == server.PLAYER_2) {
//                outputToClient.writeUTF("STARTING PILE: " + server.pile + "\n");
//                outputToClient.flush();
//                displayPileIn2Clients();
//            } else {
//                outputToClient.writeUTF("STARTING PILE: " + server.pile
//                        + "\nPlaying against server.");
//                outputToClient.flush();
//                displayPileInClient();
//            }
//
//            //inform the the randomly chosen player it is their turn to start.
//            if (playerID == server.currentPlayer) {
//                outputToClient.writeUTF("You Start.");
//                outputToClient.flush();
//            }
//
//            while (!server.gameOver()) {
//                if (server.twoPlayerMode) {
//                    twoPlayerGameRun();
//                    displayPileIn2Clients();
//                } else {
//                    singlePlayerGameRun();
//                }
//            }
//
//            if (server.twoPlayerMode) {
//                try {
//                    server.clients[server.currentPlayer].youLoseMessage();
//                    server.clients[(server.currentPlayer + 1) % 2].youWinMessage();
//                    server.displayMessage("Someone won the game.\n\n"
//                            + "Closing server in 10 seconds.");
//                    connection.close();
//                    Thread.sleep(10000);
//                    System.exit(1);
//                } catch (Exception e) {
//                }
//            }
//        } catch (IOException e) {
//        }
//    }
//
//    private void twoPlayerGameRun() {
//        try {
//            int userInput = inputFromClient.readInt();
//
//            if (server.checkMoveAndRemoveFromPile(userInput, playerID)) {
//
//                server.displayMessage("Player " + (playerID + 1) + " Subtracted "
//                        + userInput + " from " + (server.pile + userInput) + "."
//                        + " New pile is: " + server.pile + "\n");
//                outputToClient.writeUTF("\nValid input. You took "
//                        + userInput + " marbles. \nNew pile is: "
//                        + server.pile + "\n");
//                outputToClient.flush();
//            } else {
//                outputToClient.writeUTF("Invalid move, try again");
//                outputToClient.flush();
//            }
//        } catch (Exception e) {
//        }
//    }
//
//    private void singlePlayerGameRun() {
//        try {
//
//            int userInput = inputFromClient.readInt();
//
//            if (server.checkMoveSinglePlayer(userInput)) {
//
//                server.displayMessage("Player " + (playerID + 1) + " Subtracted "
//                        + userInput + " from " + (server.pile + userInput) + "."
//                        + " New pile is: " + server.pile + "\n");
//                outputToClient.writeUTF("\nYou took " + userInput
//                        + " marbles. \nNew pile is: " + server.pile + "\n");
//                outputToClient.flush();
//                displayPileInClient();
//
//                serverMove(server.pile);
//                displayPileInClient();
//
//                if (server.pile == 1) {
//                    serverWin = true;
//                    server.clients[server.currentPlayer].youLoseMessage();
//                    server.displayMessage("Server has won the game.\n\n"
//                            + "Closing server in 10 seconds.");
//                    connection.close();
//                    Thread.sleep(10000);
//                    System.exit(1);
//                }
//
//                if (!serverWin) {
//                    outputToClient.writeUTF("Your turn.");
//                    outputToClient.flush();
//                }
//
//            } else {
//                outputToClient.writeUTF("Invalid move, try again");
//                outputToClient.flush();
//            }
//        } catch (IOException | InterruptedException e) {
//        }
//    }
//
//    private void serverMove(int currentPileSize) throws InterruptedException {
//        try {
//            //int smartmove = currentPileSize;
//            //int randomLegalMove;
//            if (currentPileSize == 1) {
//                server.clients[server.currentPlayer].youWinMessage();
//                server.displayMessage("Opponent has won the game.\n\n"
//                        + "Closing server in 10 seconds.");
//                connection.close();
//                Thread.sleep(10000);
//                System.exit(1);
//            } else {
//                int serverMoveAmount = Math.round(currentPileSize / 2);
//
//                Thread.sleep(500);
//                outputToClient.writeUTF("Server is thinking...\n");
//                outputToClient.flush();
//                Thread.sleep(server.r.nextInt(4000 - 1000) + 1000);
//
//                server.displayMessage("Old Pile: " + server.pile);
//                server.pile -= serverMoveAmount;
//                server.displayMessage("New Pile: " + server.pile);
//                server.displayMessage("Server took " + serverMoveAmount
//                        + " marbles off the pile. New pile is: " + server.pile + "\n");
//
//                outputToClient.writeUTF("Server took " + serverMoveAmount
//                        + " marbles off the pile.\nNew pile is: " + server.pile);
//                outputToClient.flush();
//            }
//        } catch (IOException e) {
//        }
//    }
//
//    private void displayPile() {
//        try {
//            outputToClient.writeUTF("pile:" + server.pile);
//            outputToClient.flush();
//        } catch (Exception e) {
//        }
//    }
//
//    private void displayPileIn2Clients() {
//        server.clients[server.currentPlayer].displayPile();
//        server.clients[(server.currentPlayer + 1) % 2].displayPile();
//    }
//
//    private void displayPileInClient() {
//        server.clients[server.currentPlayer].displayPile();
//    }
//
//    //this method will toggle suspended state of a thread
//    public void setSuspended(boolean b) {
//        suspended = b;
//    }
//}
