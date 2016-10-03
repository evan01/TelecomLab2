
import java.net.*;

public class DnsClient {
<<<<<<< HEAD
	public static void main (String args[]) {				
		
		
		DatagramSocket socket = new DatagramSocket();
	}	
=======
	/**
	 * The start of our program!
	 * @param args
	 */
	public static void main (String args[]) {
		Parser prsr = new Parser();
		DNSOptions options = prsr.parse(args);
	}

	public static void createSocket(){

	}
>>>>>>> a91ffca3469b897a22dfe16b7df309b9dae7a76c
}
