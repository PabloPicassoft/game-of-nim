///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ClientHandler;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//
///**
// *
// * @author paul
// */
//    //private inner class clienthandler will create and assign threads to each incoming client request
//    public class ClientHandler extends Thread {
//
//        private Socket connection;
//        private DataInputStream inputFromClient;
//        private DataOutputStream outputToClient;
//        private int playerID;
//        private String messageString;
//        protected boolean suspended = true;
//
//        //constructor takes server socket address and the identifying int of 
//        //the client
//        public ClientHandler(Socket socket, int clientNumber) {
//            playerID = clientNumber;
//            connection = socket;
//
//            obtainIOStreams();
//        }
//
//        public void obtainIOStreams() {
//            try {
//                inputFromClient = new DataInputStream(connection.getInputStream());
//                outputToClient = new DataOutputStream(connection.getOutputStream());
//            } catch (IOException iOException) {
//                iOException.printStackTrace();
//                displayMessage("ERROR GETTING IO STREAMS");
//                System.exit(1);
//            }
//            displayMessage("Obtained IO Streams");
//        }
//
//        public void opponentTurnTaken(int marblesTaken) {
//            try {
//                outputToClient.writeUTF("\nOpponent took " + marblesTaken 
//                        + " marbles from the pile.\n"
//                        + "New Pile is " + pile);
//            } catch (Exception e) {
//            }
//        }
//
//        @Override
//        public void run() {
//            try {
//                displayMessage("Client " + playerID + " from " + connection 
//                        + " has connected.");
//                outputToClient.writeUTF("PLAYER " + (playerID + 1) + " CONNECTED.");
//                outputToClient.flush();
//
//                if (twoPlayerMode && (playerID == PLAYER_1)) {
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
//                        IException.printStackTrace();
//                    }
//                    outputToClient.writeUTF("Second player connected.");
//                    outputToClient.flush();
//                    outputToClient.writeUTF("STARTING PILE: " + pile);
//                    outputToClient.flush();
//                } else if (playerID == PLAYER_2) {
//                    outputToClient.writeUTF("STARTING PILE: " + pile);
//                    outputToClient.flush();
//                } else {
//                    outputToClient.writeUTF("Playing against server.");
//                    outputToClient.flush();
//                }
//
//                setRandomStartPlayer();
//                
//                while (!gameOver()) {
//
//                    int userInput = inputFromClient.readInt();
//
//                    if (checkMoveAndRemoveFromPile(userInput, playerID)) {
//
//                        displayMessage("Player " + playerID + " Subtracted " 
//                                + userInput + " from " + (pile + userInput) + "."
//                                + " New pile is: " + pile + "\n");
//                        outputToClient.writeUTF("\nValid input. You took " 
//                                + userInput + " marbles.  New pile is: " 
//                                + pile + "\n");
//                        outputToClient.flush();
//                        //inputFromClient.reset();
//                    } else {
//                        outputToClient.writeUTF("Invalid move, try again");
//                        outputToClient.flush();
//                    }
//                }
//                connection.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void setRandomStartPlayer() {
//            try {
//                if (randomCounter == 1) {
//                    if (Math.random() < 0.5) {
//                        currentPlayer = PLAYER_1;
//                        clients[0].outputToClient.writeUTF("You Start.");
//                        outputToClient.flush();
//                        
//                        randomCounter--;
//                    } else {
//                        currentPlayer = PLAYER_2;
//                        clients[1].outputToClient.writeUTF("You Start.");
//                        outputToClient.flush();
//                        
//                        randomCounter--;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
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
//
//    }
//
///////// INTIAL randomstartplayer attempt
//
//private void setRandomStartPlayer() {
//            try {
//                
//                if (randomCounter == 1) {//ensure the starter is chosen only once
//                    if (Math.random() < 0.5) {
//                        currentPlayer = PLAYER_1;
//                        clients[0].outputToClient.writeUTF("You Start.");
//                        outputToClient.flush();
//
//                        randomCounter--;//decrement the 
//                    } else {
//                        currentPlayer = PLAYER_2;
//                        clients[1].outputToClient.writeUTF("You Start.");
//                        outputToClient.flush();
//
//                        randomCounter--;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
