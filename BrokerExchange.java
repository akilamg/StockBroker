import java.io.*;
import java.net.*;

public class BrokerExchange {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		Socket socket = null;
		Socket lookupSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		String hostname = "localhost";

		try {
			/* variables for hostname/port */
			int port = 4444;
			
			if(args.length == 2 ) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}

			lookupSocket = new Socket(hostname, port);

			out = new ObjectOutputStream(lookupSocket.getOutputStream());
			in = new ObjectInputStream(lookupSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		String[] input = null;

		System.err.println("Enter command, symbol or x for exit:");
		System.out.print(">");
		while ((userInput = stdIn.readLine()) != null
				&& userInput.toLowerCase().indexOf("x") == -1) {
			/* make a new request packet */
			BrokerPacket packetToServer = new BrokerPacket();
			input = userInput.split(" ");
			
			if(input[0].equals("local")){

				packetToServer.type = BrokerPacket.LOOKUP_REQUEST;
				packetToServer.exchange = input[1];

			}
			
			if(input[0].equals("add")){
				packetToServer.type = BrokerPacket.EXCHANGE_ADD;
				packetToServer.symbol = input[1];
			}
			
			else if(input[0].equals("update")){
				packetToServer.type = BrokerPacket.EXCHANGE_UPDATE;
				String temp = input[1] + " " + input[2];
				packetToServer.symbol = temp;
			}
			
			else if(input[0].equals("remove")){
				packetToServer.type = BrokerPacket.EXCHANGE_REMOVE;
				packetToServer.symbol = input[1];
			}

			//System.out.println(input[0]+input[1]);
			out.writeObject(packetToServer);

			/* print server reply */
			BrokerPacket packetFromServer;
			packetFromServer = (BrokerPacket) in.readObject();


			if (packetFromServer.type == BrokerPacket.BROKER_ERROR){

				if(packetFromServer.error_code == BrokerPacket.ERROR_INVALID_SYMBOL)
					System.out.println(input[1] + " invalid.");
					
				else if(packetFromServer.error_code == BrokerPacket.ERROR_OUT_OF_RANGE)
					System.out.println(input[1] + " out of range.");
					
				else if(packetFromServer.error_code == BrokerPacket.ERROR_SYMBOL_EXISTS)
					System.out.println(input[1] + " exists.");
					
				else if(packetFromServer.error_code == BrokerPacket.ERROR_INVALID_EXCHANGE){
				
					if(packetFromServer.exchange != null)
						System.out.println("Unknown file: " + packetFromServer.exchange);
					else
						System.out.println("Server is Disconnected");
				}
			}

			else{
					if(packetFromServer.type == BrokerPacket.EXCHANGE_ADD){
						System.out.println(input[1] + " added.");
					}
					else if(packetFromServer.type == BrokerPacket.EXCHANGE_UPDATE){
						System.out.println(input[1] + "  updated to " + input[2] + ".");
					}
					else if(packetFromServer.type == BrokerPacket.EXCHANGE_REMOVE){
						System.out.println(input[1] + " removed.");
					}
					
					else if(packetFromServer.type == BrokerPacket.LOOKUP_REPLY){

						
						try {

							socket = new Socket (hostname, packetFromServer.locations[0].broker_port);
							out = new ObjectOutputStream(socket.getOutputStream());
							in = new ObjectInputStream(socket.getInputStream());
						}catch (Exception e){
						
							System.err.println("server could not connect to: " + packetFromServer.locations[0].broker_port);
							out.close();
							in.close();
							socket.close();
							System.exit(1);
						}
						
							packetToServer = new BrokerPacket();
							packetToServer.type = BrokerPacket.LOOKUP_REGISTER;
							packetToServer.exchange = packetFromServer.exchange;
							out.writeObject(packetToServer);
							System.out.println(packetFromServer.exchange + " as local.");
					}

					
					else
						System.out.println("Unknown command");
					
			}

			

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
		lookupSocket.close();
	}
}

