package lab;

import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.concurrent.*;

public class DnsClient {

    /**
     * The start of our program!
     *
     * @param args
     */
    public static void main(String args[]) {
        //first get the args from the user
        DNSOptions opts = Parser.parse(args);
        sendDnsMessage(opts);
        System.exit(0);
    }

    public static boolean sendDnsMessage(DNSOptions opts) {

        //Then try sending the packet
        DnsPacket send_pkt = new DnsPacket(opts);
        try {
            //Try sending a new packet
            DnsPacketResponse receieve_pkt = sendRequestUsingUDP(send_pkt);
            //If you don't get anything back then server did not respond
            if (receieve_pkt == null) {
                System.out.println("\nERROR\tMaximum number of retries [" + send_pkt.options.maxRetries + "] exceeded");
                return false;
            } else {
                printResponseSection(receieve_pkt);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void printResponseSection(DnsPacketResponse receieve_pkt) {
        if (receieve_pkt.records.size() + receieve_pkt.additionalRecords.size() < 1) {
            System.out.print("NOTFOUND");
            return;
        }

        System.out.println("\n***Answer Section (" + receieve_pkt.answerCnt + ") records ***");
        for (int i = 0; i < receieve_pkt.records.size(); i++) {
            System.out.println(receieve_pkt.records.get(i));
        }
        System.out.println("\n***Additional Section (" + receieve_pkt.addRecCnt + ") records ***");
        for (int i = 0; i < receieve_pkt.additionalRecords.size(); i++) {
            System.out.println(receieve_pkt.additionalRecords.get(i));
        }
    }

    /**
     * Sends a UDP, DNS request to a server
     *
     * @param pkt
     * @return
     * @throws Exception
     */
    public static DnsPacketResponse sendRequestUsingUDP(DnsPacket pkt) throws Exception {

        //Create UDP packet, can possibly throw an exception
        printQuery(pkt.options);
        DatagramPacket send_packet = createUdpPacket(pkt);

        //SEND the packet
        DatagramPacket receivePacket = sendDNSPacket(send_packet, pkt.options);

        if (receivePacket == null) {
            return null;
        } else {
            //Parse the packet and return the disected packet
            return parseResponsePacket(receivePacket, pkt.options, pkt.QUESTION.length);
        }
    }

    private static void printQuery(DNSOptions options) {
        System.out.println("\nDnsClient sending request for " + options.query);
        System.out.println("Server: " + options.stringServer);
        System.out.println("Request type: " + options.queryType);
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
        int timeOut = opts.timeout;
        int maxRetries = opts.maxRetries;

        //Try to send the packet in a new thread
        ExecutorService exec = Executors.newSingleThreadExecutor();
        System.out.println();

        while (attempts < maxRetries) {
            //Submit a new task to be completed
            Future<String> future = exec.submit(new ClientSender(opts, send_packet, attempts));

            try {
                //Start a timer
                System.out.print(".");
                long now = System.currentTimeMillis();
                String res = future.get(timeOut, TimeUnit.SECONDS);
                double time = (double) (System.currentTimeMillis() - now);
                time = time / (double) 1000;

                if (res.equals("success")) {
                    System.out.println("\nResponse received after " + time + " seconds and (" + attempts + " retries)");
                    return ClientSender.getReceive_packet();
                }
                //
            } catch (TimeoutException e) {
                future.cancel(true);
                attempts++;
                continue;
            }

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
                    pkt.packet,
                    pkt.packet.length,
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
    private static DnsPacketResponse parseResponsePacket(DatagramPacket pkt, DNSOptions opts, int questionLen) throws Exception {
        byte[] data = pkt.getData();
        DnsPacketResponse rsp = new DnsPacketResponse();
        rsp.parseDnsPacketResponse(data, opts, questionLen);
        return rsp;
    }
}
