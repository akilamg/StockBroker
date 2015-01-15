import java.io.*;
import java.net.*;

public class BrokerClient {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;
			
			if(args.length == 2 ) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
			socket = new Socket(hostname, port);

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		String[] line;
		
		System.err.println("Enter command, symbol or x for exit:");
		System.out.print(">");
		while ((userInput = stdIn.readLine()) != null
				&& userInput.toLowerCase().indexOf("x") == -1) {
				
			//BrokerLocation currLocation = new BrokerLocation(socket.getInetAddress(), socket.getLocalPort());
			BrokerPacket packetToServer = new BrokerPacket();	
			line = userInput.split(" ");
				
			/* make a new request packet */
			if(line[0].equals("local")){
				
				packetToServer.type = BrokerPacket.LOOKUP_REGISTER;
				packetToServer.exchange = line[1];
			}
			
			else{

				packetToServer.type = BrokerPacket.BROKER_REQUEST;
				packetToServer.symbol = userInput;

			}
			out.writeObject(packetToServer);

			/* print server reply */
			BrokerPacket packetFromServer;
			packetFromServer = (BrokerPacket) in.readObject();

			if (packetFromServer.type == BrokerPacket.BROKER_QUOTE)
				System.out.println("Quote from broker: " + packetFromServer.num_locations);
				
			else if(packetFromServer.type == BrokerPacket.LOOKUP_REGISTER)
				System.out.println(packetFromServer.exchange + " as local.");

				
			else if(packetFromServer.type == BrokerPacket.LOOKUP_REPLY){
				System.out.println("Unknown file: " + packetFromServer.exchange);
			}
			
			else
				System.out.println("Unknown command");


			/* re-print console prompt */
			System.out.print(">");
		}

		/* tell server that i'm quitting */
		BrokerPacket packetToServer = new BrokerPacket();
		packetToServer.type = BrokerPacket.BROKER_BYE;
		//packetToServer.message = "Bye!";
		out.writeObject(packetToServer);

		out.close();
		in.close();
		stdIn.close();
		socket.close();
	}
}
