import java.net.*;
import java.io.*;


public class lookupHandlerThread extends Thread {
	private Socket socket = null;
	private Socket sock = null; 
	static BrokerLocation location = null;

	public lookupHandlerThread (Socket socket) {
		super("lookupHandlerThread");
		this.socket = socket;
		this.sock = sock;
		System.out.println("Created new Look up Thread to handle client");
	}


	public void run() {

		boolean gotByePacket = false;
		String file = null;
		ObjectInputStream serverIn = null;
		
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			BrokerPacket packetFromClient = null;
			String request = null;
			Object incoming;
			
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			
			while ((incoming = fromClient.readObject()) != null) {

				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				BrokerPacket packetFromBroker = new BrokerPacket();
				
				
				/* process message */
				/* Reply in this example */

				if(incoming instanceof BrokerLocation){

					location = (BrokerLocation) incoming;
					String port = Integer.toString(location.broker_port);
					System.out.println("Got port from Server: " + port);
					continue;
				}


				else{
					packetFromClient = (BrokerPacket) incoming;

				}				
					

				if(packetFromClient.type == BrokerPacket.LOOKUP_REQUEST) {

						if(location == null){
						
							System.out.println("The server is not connected");
							packetToClient.type = BrokerPacket.BROKER_ERROR;
							packetToClient.error_code = BrokerPacket.ERROR_INVALID_EXCHANGE;
							toClient.writeObject(packetToClient);
						}
						
						else if((packetFromClient.exchange != null) && !(packetFromClient.exchange.equals("nasdaq") ||packetFromClient.exchange.equals("tse"))){
						
							packetToClient.exchange = packetFromClient.exchange;
							
							System.out.println("Invalid File name " + packetToClient.exchange);
							packetToClient.type = BrokerPacket.BROKER_ERROR;
							packetToClient.error_code = BrokerPacket.ERROR_INVALID_EXCHANGE;
							toClient.writeObject(packetToClient);
						}
						
						else{	
		
							packetToClient.locations = new BrokerLocation[1];
							packetToClient.locations[0] = location;
							packetToClient.exchange = packetFromClient.exchange;
							packetToClient.type = BrokerPacket.LOOKUP_REPLY;
							toClient.writeObject(packetToClient);
						}
			
						continue;
	
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
					
					if(packetFromClient.exchange == null)
						packetToClient.exchange = packetFromClient.symbol;
					else
						packetToClient.exchange = packetFromClient.exchange;
						
					System.out.println("Invalid File name " + packetToClient.exchange);
					packetToClient.type = BrokerPacket.BROKER_ERROR;
					packetToClient.error_code = BrokerPacket.ERROR_INVALID_EXCHANGE;
					toClient.writeObject(packetToClient);
				}

			}

        
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
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
