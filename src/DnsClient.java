public class DnsClient {
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
}
