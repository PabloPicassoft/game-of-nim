/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author paul
 */
    //private inner class clienthandler will create and assign threads to each incoming client request
    public class ClientHandler extends Thread {

        private Socket connection;
        private DataInputStream input;
        private DataOutputStream output;
        private int clientNumber;
        private String messageString;
        protected boolean suspended = true;

        //constructor takes server socket address and the identifying int of the client
        public ClientHandler(Socket socket, int number) {
            clientNumber = number;
            connection = socket;

            obtainIOStreams();
        }

        public void obtainIOStreams() {
            try {
                input = new DataInputStream(connection.getInputStream());
                output = new DataOutputStream(connection.getOutputStream());
            } catch (IOException iOException) {
                iOException.printStackTrace();
                //displayMessage("ERROR GETTING IO STREAMS");
                System.exit(1);
            }
            //displayMessage("Obtained IO Streams");
        }

        @Override
        public void run() {
            try {
                //displayMessage("Client " + clientNumber + " from " + connection + " has connected.");
                output.writeUTF("PLAYER " + clientNumber + " CONNECTED.");
            } catch (Exception e) {
            }
        }

    }