import java.net.*;
import java.io.*;


public class lookupHandlerThread extends Thread {
	private Socket socket = null;
	private Socket sock = null; 

	public lookupHandlerThread (Socket socket) {
		super("lookupHandlerThread");
		this.socket = socket;
		this.sock = sock;
		System.out.println("Created new Look up Thread to handle client");
	}


	public void run() {

		boolean gotByePacket = false;
		String file = null;
		ObjectOutputStream serverOut = null;
		ObjectInputStream serverIn = null;
		
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			BrokerPacket packetFromClient;
			String request = null;
			
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			
			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
        		
					
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				BrokerPacket packetToBroker = new BrokerPacket();
				BrokerPacket packetFromBroker = new BrokerPacket();
				
				
				/* process message */
				/* Reply in this example */
				if(packetFromClient.type == BrokerPacket.LOOKUP_REGISTER) {
						
						//System.out.println("host: " + BrokerPacket.broker_host + "port:" + broker_port);
						String port = "5005";
						int p = Integer.parseInt(port);
						System.out.println("Connecting to: " + packetFromClient.exchange);
						try {
						
							sock = new Socket("localhost", p);
							serverOut = new ObjectOutputStream(sock.getOutputStream());
							serverIn = new ObjectInputStream(sock.getInputStream());
						}
						catch (Exception e) {
						
							System.err.println("server could not connect to: " + port);
							serverOut.close();
							serverIn.close();
							sock.close();
							System.exit(1);
						  }
						
						if(packetFromClient.exchange.equals("nasdaq") || packetFromClient.exchange.equals("tse")){
							packetToBroker.type = BrokerPacket.LOOKUP_REGISTER;
							request = packetFromClient.exchange;
							request = request.toLowerCase();
							file = request;
							System.out.println(file);
							
							System.out.println("Now using file: " + file);
							packetToBroker.exchange = file;
							serverOut.writeObject(packetToBroker);
							
							packetToClient.exchange = file;
							packetToClient.type = BrokerPacket.LOOKUP_REGISTER;
							toClient.writeObject(packetToClient);
					
							continue;
						}
						
						else {
						
							System.err.println("Unknown file name: " + packetFromClient.exchange);
							packetToClient.exchange = packetFromClient.exchange;
							packetToClient.type = BrokerPacket.LOOKUP_REPLY;
							toClient.writeObject(packetToClient);
							continue;
						}
			
				}
				    
			/* Sending an ECHO_NULL || ECHO_BYE means quit */
				else if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					gotByePacket = true;
					packetToClient = new BrokerPacket();
					packetToClient.type = BrokerPacket.BROKER_BYE;
					toClient.writeObject(packetToClient);
					System.out.println("Good bye");

					break;
				}
				
				else{
						serverOut.writeObject(packetFromClient);
						packetFromBroker = (BrokerPacket)serverIn.readObject();
						toClient.writeObject(packetFromBroker);
						continue;
				}
					
				     
			}

        
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
			serverOut.close();
			serverIn.close();
			sock.close();
			socket.close();


		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
}
