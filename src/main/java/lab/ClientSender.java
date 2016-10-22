package lab;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * TelecomLab2
 * Created by eknox on 2016-10-13.
 */
public class ClientSender implements Callable<String> {


    private static boolean PacketReceived;
    private static DatagramPacket receive_packet;
    DNSOptions options;
    DatagramPacket send_pkt;
    int attemptNum;

    public ClientSender(DNSOptions opts, DatagramPacket packet, int attemptNumber) {
        options = opts;
        send_pkt = packet;
        attemptNum = attemptNumber;
        setPacketReceived(false);
    }

    @Override
    /**
     * This function will try and send a packet
     */
    public String call() throws Exception {
        try {
            // Create packet to store data that the server is sending us
            byte[] receiveData = new byte[512];
            setReceive_packet(new DatagramPacket(receiveData, receiveData.length));

            //Open a socket to send the packet
            DatagramSocket clientSoc = new DatagramSocket();

            //SEND THE PACKET
            clientSoc.send(send_pkt);

            //RECEIVE THE PACKET
            getUdpPacket(clientSoc);

            //Set the flag that we indeed received the packet
            setPacketReceived(true);

            //Close the client socket
            clientSoc.close();

        } catch (SocketException skt) {
            System.out.println("Socket problem");
            skt.printStackTrace();
        } catch (IOException io) {
            System.out.println("IO Exception");
            io.printStackTrace();
        } catch (Exception e) {
            System.out.println("");
            throw new TimeoutException("Timed out, try sending again");
        }
        return "success";
    }

    public synchronized void getUdpPacket(DatagramSocket sock) throws Exception {
        //Hopefully we receive this! Can timeout here...THIS is what timesout
        sock.receive(getReceive_packet());
        if (getReceive_packet() == null) {
            throw new Exception("SOMETHING WRONG WITH PACKET");
        }

    }


    public synchronized static DatagramPacket getReceive_packet() {
        return receive_packet;
    }

    public synchronized static void setReceive_packet(DatagramPacket receive_packet) {
        ClientSender.receive_packet = receive_packet;
    }

    public synchronized static boolean isPacketReceived() {
        return PacketReceived;
    }

    public synchronized static void setPacketReceived(boolean packetReceived) {
        PacketReceived = packetReceived;
    }


}
