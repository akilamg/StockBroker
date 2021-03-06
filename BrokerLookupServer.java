import java.net.*;
import java.io.*;

public class BrokerLookupServer{
    public static void main(String[] args) throws IOException {
        ServerSocket lookupSocket = null;
        boolean listening = true;

        try {

        	if(args.length == 1) {

        		lookupSocket = new ServerSocket(Integer.parseInt(args[0]));
        	} 
        	else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {

            new lookupHandlerThread(lookupSocket.accept()).start();

        }

        lookupSocket.close();
    }
}
