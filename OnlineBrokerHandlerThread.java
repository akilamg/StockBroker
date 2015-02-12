import java.net.*;
import java.io.*;
import java.util.*;

public class OnlineBrokerHandlerThread extends Thread {
	private Socket socket = null;
	String file = null;
	static Hashtable cache = new Hashtable();
	static Hashtable deleted = new Hashtable();

	public OnlineBrokerHandlerThread (Socket socket) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}

	public class TheEnd extends Thread {

      public void run() {
         updateFile();
      }
   }

	public void updateFile() {

	    try {

			File inFile = new File(file);

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			//Construct the new file that will later be renamed to the original filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter pw = new BufferedWriter(new FileWriter(tempFile));
			String[] words = null;
			String line = null;
			boolean first = true;
			Enumeration enumeration = cache.elements();

			while (enumeration.hasMoreElements()) {

				 line = (String) enumeration.nextElement();
				 pw.write(line, 0, line.length());
				 pw.newLine();
				 pw.flush();
			}

			line = null;
			//Read from the original file and write to the new
			//unless content matches data to be removed.
			while ((line = br.readLine()) != null) {
				words = line.split(" ");
				if (!cache.containsKey(words[0]) && !deleted.containsKey(words[0])) {
					
				  if(!first){

				  	pw.newLine();
				  }	
				  first = false;
				  pw.write(line, 0, line.length());
				  pw.flush();
				}
			}
			
			cache.clear();
			deleted.clear();
			pw.close();
			br.close();
			
			//Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			//Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");

	    }
	    catch (FileNotFoundException ex) {
	      ex.printStackTrace();
	    }
	    catch (IOException ex) {
	      ex.printStackTrace();
	    }
  }


	public void run() {

		boolean gotByePacket = false;
		FileReader inputFile = null;
	    BufferedReader bufferReader = null;
	    FileWriter fw = null;
	    String[] words = null;
	    String[] temp = null;
	    String[] temp2 = null;

		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			BrokerPacket packetFromClient;
			String line = null;
			String prevline = null;
			String request = null;
			String lower = null;
      		boolean found;
			
			Runtime.getRuntime().addShutdownHook(new TheEnd());
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			
			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
			
				if(packetFromClient.type == BrokerPacket.LOOKUP_REGISTER){
										
					file = packetFromClient.exchange;
			
					continue;
					
				}
				
        		found = false;
				inputFile = new FileReader(file);
				bufferReader = new BufferedReader(inputFile);
				
				request = packetFromClient.symbol;
				if(request != null)
					lower = request.toLowerCase();
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				packetToClient.type = BrokerPacket.BROKER_QUOTE;
				
				/* process message */
				/* Reply in this example */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
					
					System.out.println("From Client: " + packetFromClient.symbol);

					if(cache.containsKey(lower)){
						found=true;
						line = (String) cache.get(lower);
						words = line.split(" ");
					
					}
					
					if(!deleted.containsKey(lower)){
					
						while ((!found)&&((line = bufferReader.readLine()) != null)) {
							words = line.split(" ");
						    if(words[0].equals(lower)){
			          			found = true;
						    	break;
						    }
						    
						}
            		}
					/* send reply back to client */
		       		if(!found){
		       			String nfile = null;
		       			if(file.equals("nasdaq"))
		       				nfile = "tse";
		       			else{
		       				nfile = "nasdaq";
		       			}
		       			
		       			inputFile = new FileReader(nfile);
						bufferReader = new BufferedReader(inputFile);
		       			
		       			while ((line = bufferReader.readLine()) != null) {
							words = line.split(" ");
						    if(words[0].equals(lower)){
			          			found = true;
						    	break;
						    }
						}
						
						if(!found){
						
							packetToClient.num_locations = 0;
							System.out.println(packetFromClient.symbol +" invalid.");
							System.out.println("Sending to Client: " + packetToClient.num_locations);
							toClient.writeObject(packetToClient);
						}
						
						else{
						
							packetToClient.num_locations = Integer.parseInt(words[1]);
							System.out.println("Sending to Client: " + packetToClient.num_locations);
							toClient.writeObject(packetToClient);
						}
		        
		        	}
		        
		        	else{
						packetToClient.num_locations = Integer.parseInt(words[1]);
						System.out.println("Sending to Client: " + packetToClient.num_locations);
						toClient.writeObject(packetToClient);
		        	}
					/* wait for next packet */
					continue;
				}

				else if(packetFromClient.type == BrokerPacket.EXCHANGE_ADD){
					System.out.println("From Client To Be Added: " + packetFromClient.symbol);


					while ((line = bufferReader.readLine()) != null) {

						prevline=line.trim();
						words = line.split(" ");
				        if(words[0].equals(lower)){
                  			found = true;
				        	break;
				    	}
				    }

				    if((found == true) || (cache.containsKey(lower))){
				    	System.out.println(packetFromClient.symbol + " exists.");
				    	packetToClient.type = BrokerPacket.BROKER_ERROR;
				    	packetToClient.error_code = BrokerPacket.ERROR_SYMBOL_EXISTS;
				    	toClient.writeObject(packetToClient);
				    }

				    else{

				    	System.out.println("Adding: " + packetFromClient.symbol);
				    	cache.put(lower, lower + " " + "0");
				    	packetToClient.type = BrokerPacket.EXCHANGE_ADD;
				    	toClient.writeObject(packetToClient);
				    }

				    continue;

				}

				else if(packetFromClient.type == BrokerPacket.EXCHANGE_UPDATE){

					temp = lower.split(" ");
					temp2 = request.split(" ");
					System.out.println("From Client To Be Updated: " + temp2[0]);

					while ((line = bufferReader.readLine()) != null) {
						
						prevline=line.trim();
						words = line.split(" ");
				        if(words[0].equals(temp[0])){
                  			found = true;
				        	break;
				    	}
				    }

				    if((found == true) || (cache.containsKey(temp[0]))){

				    	if((Integer.parseInt(temp[1]) >= 1) && (Integer.parseInt(temp[1]) <= 300)){

				    		System.out.println("Updating: " + temp[0]);
		
				    		cache.put(temp[0], temp[0] + " " + temp[1]);
				    		packetToClient.type = BrokerPacket.EXCHANGE_UPDATE;
				    		toClient.writeObject(packetToClient);
				    	}

				    	else{

				    		System.out.println(packetFromClient.symbol + " out of range.");
				    		packetToClient.type = BrokerPacket.BROKER_ERROR;
				    		packetToClient.error_code = BrokerPacket.ERROR_OUT_OF_RANGE;
				    		toClient.writeObject(packetToClient);
				    	}

				    }

				    else{

				    	System.out.println(packetFromClient.symbol + " is invalid.");
				    	packetToClient.type = BrokerPacket.BROKER_ERROR;
				    	packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
				    	toClient.writeObject(packetToClient);


				    }

				    continue;
				}

				else if(packetFromClient.type == BrokerPacket.EXCHANGE_REMOVE){
		
					System.out.println("From Client To Be Removed: " + packetFromClient.symbol);

					while ((line = bufferReader.readLine()) != null) {
						
						words = line.split(" ");
				        if(words[0].equals(lower)){
                  			found = true;
				        	break;
				    	}
				    }

				    if(!deleted.containsKey(lower) && ((found == true) || (cache.containsKey(lower)))){

				    	System.out.println("Deleting: " + packetFromClient.symbol);

				    	if(found == false){
				    		cache.remove(lower);
				    		packetToClient.type = BrokerPacket.EXCHANGE_REMOVE;
				    		toClient.writeObject(packetToClient);
				    	}

				    	else{

				    		deleted.put(lower, lower);
				    		packetToClient.type = BrokerPacket.EXCHANGE_REMOVE;
				    		toClient.writeObject(packetToClient);
				    	}

				    }

				    else{

				    	System.out.println(packetFromClient.symbol + " is invalid.");
				    	packetToClient.type = BrokerPacket.BROKER_ERROR;
				    	packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
				    	toClient.writeObject(packetToClient);

				    }

				    continue;
				}

				
				/* Sending an ECHO_NULL || ECHO_BYE means quit */
				if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					gotByePacket = true;
					packetToClient = new BrokerPacket();
					packetToClient.type = BrokerPacket.BROKER_BYE;
					//packetToClient.message = "Bye!";
					toClient.writeObject(packetToClient);
					break;
				}
				
				/* if code comes here, there is an error in the packet */
				System.err.println("ERROR: Unknown packet!!");
				System.exit(-1);
			}
			
			/* cleanup when client exits */
			bufferReader.close();
			fromClient.close();
			toClient.close();
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
