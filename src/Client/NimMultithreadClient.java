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
 *
 * @author Paul Iudean
 */
public class NimMultithreadClient extends JFrame implements Runnable {

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
    private boolean thisClientsTurn = false;

    public NimMultithreadClient(String host) {
        super("Game of Nim Client");
        this.hostName = host;

        initializeGUIComponents();
        startClient();
    }

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
         * ****************MIDDLE PANEL COMPONENTS********************
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
         * ****************BOTTOM PANEL COMPONENTS********************
         * ***********************************************************
         */
        bottomPanel = new JPanel();

        inputArea = new JTextArea(4, 30);
        inputArea.setEditable(false);

        /**
         * SEND MOVE BUTTON
         */
        sendMoveButton = new JButton("Make Move");
        //use lambda expression to replace anonymous inner class creation
        sendMoveButton.addActionListener((ActionEvent evt) -> {
            sendMoveButtonClick(evt);
        });
        //set 
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

    private void startClient() {
        try {
            //create a new socket connection to the server (port 12345), with 
            //an identifying host name
            connection = new Socket(InetAddress.getByName(hostName), 12345);

            //retrieve IO streams for communication with the server
            inputFromServer = new DataInputStream(connection.getInputStream());
            outputToServer = new DataOutputStream(connection.getOutputStream());

        } catch (IOException iOException) {
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

        String pileData[];

        try {
            if (message.contains("Valid input.")) {
                displayMessage(message);
            } else if (message.equals("Invalid move, try again")) {
                makeMove(message);
            } else if (message.contains("Playing against server.")) {
                displayMessage(message);
            } else if (message.contains("Second player connected.")) {
                displayMessage(message);
            } else if (message.contains("You Start.")) {
                makeMove(message);
            } else if (message.contains("Opponent took")) {
                makeMove(message);
            } else if (message.contains("Your turn.")) {
                makeMove(message);
            } else if (message.contains("pile:")) {
                pileData = message.split(":");
                pileIndicatorLabel.setText(pileData[1]);
            } else if (message.contains("You Lose.")) {
                inputArea.setEditable(false);
                sendMoveButton.setEnabled(false);
                displayMessage("\t" + message + "\n\n Closing window automatically"
                        + " in 10 seconds.");
                Thread.sleep(10000);
                connection.close();
                System.exit(1);
            } else if (message.contains("You Win!")) {
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

    private void makeMove(String msgFromServer) {
        try {
            displayMessage(msgFromServer);
            inputArea.setEditable(true);
            sendMoveButton.setEnabled(true);
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
