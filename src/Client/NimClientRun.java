
package Client;

import javax.swing.JFrame;

/**
 *
 * @author Paul Iudean
 */
public class NimClientRun {
    
    public static void main(String[] args) {
        
        NimClient client;

        if (args.length == 0) {
            client = new NimClient("127.0.0.1");
            client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            //set the address to 0
            client = new NimClient(args[0]);
            client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }
}
