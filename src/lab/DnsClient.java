package lab;

import javax.xml.crypto.Data;
import java.io.EOFException;
import java.net.*;
import javax.swing.Timer;
import java.util.TimerTask;

public class DnsClient {

    /**
     * The start of our program!
     *
     * @param args
     */
    public static void main(String args[]) {
        DNSOptions opts = new DNSOptions();
        opts = Parser.parse(args);
        DnsPacket send_pkt = new DnsPacket(opts);
        try {
            //Try sending a new packet
            DnsPacket receieve_pkt = sendRequestUsingUDP(send_pkt);
            //If you don't get anything back then server did not respond
            if (receieve_pkt == null) {
                System.out.println("No response after " + send_pkt.options.maxRetries + " retries");
            } else {
                System.out.print("YAY HERE ARE ALL THE DETAILS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Sends a UDP, DNS request to a server
     *
     * @param pkt
     * @return
     * @throws Exception
     */
    public static DnsPacket sendRequestUsingUDP(DnsPacket pkt) throws Exception {

        //Create UDP packet, can possibly throw an exception
        System.out.println("Creating the UDP packet");
        DatagramPacket send_packet = createUdpPacket(pkt);

        //SEND the packet
        DatagramPacket receivePacket = sendDNSPacket(send_packet, pkt.options);

        if (receivePacket == null) {
            return null;
        } else {
            //Parse the packet and return the disected packet
            return parseResponsePacket(receivePacket);
        }
    }

    /**
     * Takes in a packet to send to the udp server and returns the response
     *
     * @param send_packet
     * @param opts
     * @return
     * @throws Exception
     */
    private static DatagramPacket sendDNSPacket(DatagramPacket send_packet, DNSOptions opts) throws Exception {

        int attempts = 0;
        int timeOut = opts.timeout * 1000;

        //Send packet and hope that it doesn't time out
        while (attempts < opts.maxRetries) {
            //Try to send the packet
            Thread t = new Thread(new ClientSender(opts, send_packet, attempts));
            t.start();
            Thread.sleep(timeOut);

            //If after the timeout, we don't get anything, try again if we received packet
            if (ClientSender.isPacketReceived())
                //Then we received the packet!
                return ClientSender.getReceive_packet();
            //If the timeout occurs then close the thread and try again
            if (t.isAlive())
                t.interrupt();

            attempts++;
        }
        return null;
    }


    /**
     * Takes in the dns packet contents and creates a UDP packet ready to send to DNS server
     *
     * @param pkt DNS packet contents
     * @return UDP packet
     * @throws Exception if for some reason the byte rep. of the ip address does not work.
     */
    private static DatagramPacket createUdpPacket(DnsPacket pkt) throws Exception {
        //Get the ip address that we are sending to
        byte[] byteIpAddr = pkt.options.server;

        try {
            //Get the ip address representation of the passed in DNS ip
            InetAddress addr = Inet4Address.getByAddress(byteIpAddr);

            //Create a UDP packet to be sent
            DatagramPacket send_packet = new DatagramPacket(
                    pkt.packetByte,
                    pkt.packetByte.length,
                    addr,
                    pkt.options.port);

            return send_packet;
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * This function will take as input a DNS response packet and return the disected packet
     *
     * @param pkt
     */
    private static DnsPacket parseResponsePacket(DatagramPacket pkt) {
        byte[] data = pkt.getData();

        //PARSE IT HERE
        return null;
    }
}
