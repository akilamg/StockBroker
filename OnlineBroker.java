import java.net.*;
import java.io.*;

public class OnlineBroker{
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket sock = null; 
        String port = "5005";
        boolean listening = true;

        try {
        	if(args.length == 3) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[1]));
                BrokerLocation sendToLookup = new BrokerLocation(args[0], Integer.parseInt(args[1]));
                BrokerPacket fromLookup = null;
                ObjectOutputStream serverOut = null;
                ObjectInputStream serverIn = null;
                        try {
                            port = args[2];
                            sock = new Socket(args[0], Integer.parseInt(port));
                            serverOut = new ObjectOutputStream(sock.getOutputStream());
                            serverIn = new ObjectInputStream(sock.getInputStream());
                        }
                        catch (Exception e) {
                        
                            System.err.println("server could not connect to lookup on: " + port);
                            serverOut.close();
                            sock.close();
                            System.exit(1);
                          }
                        serverOut.writeObject(sendToLookup);
                        
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {

        	new OnlineBrokerHandlerThread(serverSocket.accept()).start();
        }

        serverSocket.close();
    }
}
