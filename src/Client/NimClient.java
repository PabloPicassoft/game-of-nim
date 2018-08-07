package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * @author Paul Iudean
 */
public class NimClient extends JFrame implements Runnable {

    private JPanel mainPanel;

    private JPanel topPanel;
    private JPanel middlePanel;
    private JPanel bottomPanel;

    private JTextArea displayArea;
    private JTextArea inputArea;

    private JLabel clientTitleLabel;
    private JLabel pileTitleLabel;
    private JLabel pileIndicatorLabel;

    private JButton sendMoveButton;

    private final String hostName;
    private Socket connection;
    private DataInputStream inputFromServer;
    private DataOutputStream outputToServer;
    private int serverMessage;

    /**
     * 
     * 
     * @param host 
     */
    public NimClient(String host) {
        super("Game of Nim Client");
        this.hostName = host;

        initializeGUIComponents();
        startClient();
    }

    /**
     * This method handles the initialization of all the GUI components
     */
    public final void initializeGUIComponents() {

        mainPanel = new JPanel();
        this.setContentPane(mainPanel);

        topPanel = new JPanel();
        clientTitleLabel = new JLabel("THE GAME OF NIM");
        clientTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        topPanel.add(clientTitleLabel, BorderLayout.CENTER);
        topPanel.setBackground(Color.LIGHT_GRAY);

        /**
         * ***********************************************************
         * MIDDLE PANEL COMPONENTS *
         * ***********************************************************
         */
        middlePanel = new JPanel();

        displayArea = new JTextArea(20, 25);
        displayArea.setEditable(false);
        displayArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pilePanel = new JPanel();

        pileTitleLabel = new JLabel("  Pile Size:");
        pileTitleLabel.setVerticalAlignment((int) TOP_ALIGNMENT);
        pileTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        pileIndicatorLabel = new JLabel("00");
        pileIndicatorLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        pileIndicatorLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        pilePanel.add(pileTitleLabel, BorderLayout.NORTH);
        pilePanel.add(pileIndicatorLabel, BorderLayout.CENTER);
        pilePanel.setBackground(Color.LIGHT_GRAY);

        middlePanel.add(new JScrollPane(displayArea), BorderLayout.WEST);
        middlePanel.add(pilePanel, BorderLayout.EAST);
        middlePanel.setBackground(Color.LIGHT_GRAY);

        /**
         * ***********************************************************
         * BOTTOM PANEL COMPONENTS *
         * ***********************************************************
         */
        bottomPanel = new JPanel();

        inputArea = new JTextArea(4, 30);
        inputArea.setEditable(false);

        sendMoveButton = new JButton("Make Move");
        //use lambda expression to replace anonymous inner class creation
        sendMoveButton.addActionListener((ActionEvent evt) -> {
            sendMoveButtonClick(evt);
        });
        
        /**
         * Set the button to disabled inititally - to stop thread interferance 
         * and inconsistent runtime activities. If enabled by default, both clients
         * would be able to send messages to the server, however this will disrupt
         * the flow of the game and runtime errors could occur.
         */
        sendMoveButton.setEnabled(false);

        bottomPanel.add(inputArea);
        bottomPanel.add(sendMoveButton, BorderLayout.EAST);
        bottomPanel.setBackground(Color.LIGHT_GRAY);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.setBackground(Color.LIGHT_GRAY);

        // set size and make the window visible & non-resizeable
        setSize(500, 550);
        setVisible(true);
        setResizable(false);
    }

    /**
     * This method will create a connection between the server and the client
     * by binding this client's socket with the server's.
     * Open a communication channel for the socket through DIS and DOS.
     */
    private void startClient() {
        try {
            //create a new socket connection to the server (port 12345), with 
            //an identifying host name
            connection = new Socket(InetAddress.getByName(hostName), 12345);

            //retrieve IO streams for communication with the server
            inputFromServer = new DataInputStream(connection.getInputStream());
            outputToServer = new DataOutputStream(connection.getOutputStream());

        } catch (IOException iOException) {
            
            /**
             * The most common exception cause is when the server is not running
             * and a user runs the client program. As there is nothing to connect to 
             * an IOException is thrown
             */
            JOptionPane.showMessageDialog(null, "\n>>>  FAILED TO START CLIENT, "
                    + "SERVER MAY NOT BE RUNNING.\n FOLLOW THESE STEPS:\n\n"
                    + " 1. Close this window.\n\n"
                    + " 2. Start the server.\n\n"
                    + " 3. Follow steps once server is running\n\n"
                    + " 4. Reopen the client.\n\n"
                    + " 5. ENJOY!");

            displayMessage("\n >>>  FAILED TO START CLIENT"
                    + "\n >>>  SERVER MAY NOT BE RUNNING.");
            displayMessage("\n FOLLOW THESE STEPS:\n\n"
                    + " 1. Close this window.\n\n"
                    + " 2. Start the server.\n\n"
                    + " 3. Follow steps once server is running\n\n"
                    + " 4. Reopen the client.\n\n"
                    + " 5. ENJOY!");
        }
        
        //Start this client's listening thread
        Thread readOutputFromServer = new Thread(this);
        readOutputFromServer.start();
    }

    /**
     * Once the thread starts, this method will be always listening for a server
     * message to interpret using the processMessage method until the connection is closed
     */
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

    /**
     * This method will interpret the various messages or responses that will come
     * from the server, and act on each with the relevant functionality.
     * 
     * @param message
     * @throws InterruptedException 
     */
    private void processMessage(String message) throws InterruptedException {

        /**
         * When the pile size is transmitted from the server it is sent as a string
         * in the form of "pile:(num)" where (num) is the concatenated pile variable.
         * (done in MultiClientNimServer.displayPile())
         * This array will be initialized with two strings after splitting the message
         * from the server with a ":". Therefore the data in pileData[1] will
         * be the current size of the pile as transmitted by the server.
         */
        String pileData[];

        try {
            if (message.contains("Valid input.")) {
                displayMessage(message);
            } else if (message.equals("Invalid move, try again")) {
                //re-enable the input buttons and display the server message
                //on the "currentPlayer" client.
                makeMove(message);
            } else if (message.contains("Playing against server.")) {
                displayMessage(message);
            } else if (message.contains("Second player connected.")) {
                displayMessage(message);
            } else if (message.contains("You Start.")) {
                //This is sent to the player who was randomly chosen to get the
                //first move, only the client who sees this message will have their
                //inputs enabled.
                makeMove(message);
            } else if (message.contains("Opponent took")) {
                //let this client know that the other client made a valid move and
                //it is now this clients turn. Enable inputs.
                makeMove(message);
            } else if (message.contains("Your turn.")) {
                //whenever the server sends 'your turn' to either client, the inputs
                //will be enabled.
                makeMove(message);
            } else if (message.contains("pile:")) {
                //explained at start of method.
                pileData = message.split(":");
                pileIndicatorLabel.setText(pileData[1]);
            } else if (message.contains("You Lose.")) {
                
                //Inform client of result, disable all inputs and close window
                //and connection after 10 seconds
                inputArea.setEditable(false);
                sendMoveButton.setEnabled(false);
                displayMessage("\t" + message + "\n\n Closing window automatically"
                        + " in 10 seconds.");
                Thread.sleep(10000);
                connection.close();
                System.exit(1);
            } else if (message.contains("You Win!")) {
                
                //Inform client of result, disable all inputs and close window
                //and connection after 10 seconds
                inputArea.setEditable(false);
                sendMoveButton.setEnabled(false);
                displayMessage("\t" + message + "\n\n Closing window automatically"
                        + " in 10 seconds.");
                Thread.sleep(10000);
                connection.close();
                System.exit(1);
            } else {
                displayMessage(message);
            }
        } catch (IOException e) {
        }
    }

    /**
     * when it is this client's turn to make a move, this method is called and will
     * enable the input box and the make move button. These are then disabled in the 
     * sendMoveButtonClick method.
     * 
     * @see sendMoveButtonClick 
     * 
     * @param msgFromServer 
     */
    private void makeMove(String msgFromServer) {
        try {
            displayMessage(msgFromServer);
            inputArea.setEditable(true);
            sendMoveButton.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actionlistener method for when the button is pressed, once clicked, the
     * input field and the button itself are disabled. The integer that was input to the box
     * is sent to the server for verification through the dataOutputStream.
     * 
     * @param evt 
     */
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

    /**
     * Use lambda expression to update display area with the string parameter provided.
     * 
     * @param messageToDisplay 
     */
    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(() -> {
            displayArea.append(messageToDisplay + "\n");
        });
    }
}
