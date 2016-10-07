package lab;

import java.net.*;

public class DnsClient {
//	public static void main (String args[]) {
//		DatagramSocket socket = new DatagramSocket();
//	}


	/**
	 * The start of our program!
	 * @param args
	 */
	public static void main (String args[]) {
		DNSOptions opts = new DNSOptions();
		opts = Parser.parse(args);
		DnsPacket send_pkt = new DnsPacket(opts);
		try {
			DnsPacket receieve_pkt = sendRequestUsingUDP(send_pkt);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public static DnsPacket sendRequestUsingUDP(DnsPacket pkt) throws Exception{
		//Open the socket
		DatagramSocket clientSoc = new DatagramSocket();

		//Get the ip address that we are sending to
		byte[] byteIpAddr = pkt.options.server;
		InetAddress addr = Inet4Address.getByAddress(byteIpAddr);

		byte[] UDP_PACKET = pkt.bytes;

		//Create a UDP packet to be sent
		DatagramPacket send_packet = new DatagramPacket(
				UDP_PACKET,
				pkt.size,
				addr,
				pkt.options.port);

		//SEND the packet
		System.out.println("Sending the packet");
		clientSoc.send(send_packet);

		//Create a packet to store the response
		byte[] receiveDataBuffer = new byte [1024];//1024 is an arbitrary number will depend on response
		DatagramPacket receive_packet = new DatagramPacket(receiveDataBuffer,receiveDataBuffer.length);

		//Go receive the response
		clientSoc.receive(receive_packet);
		System.out.println("Received the packet");

		System.out.println("DONE");


		//Parse the packet and return the disected packet
		return parseResponsePacket(receiveDataBuffer);

	}

	/**
	 * This function will take as input a DNS response packet and return the disected packet
	 * @param receiveDataBuffer
	 */
	private static DnsPacket parseResponsePacket(byte[] receiveDataBuffer) {

		return null;
	}
}
