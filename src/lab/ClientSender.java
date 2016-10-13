package lab;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * TelecomLab2
 * Created by eknox on 2016-10-13.
 */
public class ClientSender implements Runnable {


    private static boolean PacketReceived;
    private static DatagramPacket receive_packet;
    DNSOptions options;
    DatagramPacket send_pkt;
    int attemptNum;

    public ClientSender(DNSOptions opts, DatagramPacket packet,int attemptNumber){
        options = opts;
        send_pkt = packet;
        attemptNum = attemptNumber;
        setPacketReceived(false);
    }

    @Override
    /**
     * This function will try and send a packet
     */
    public void run() {
        try {
            // Create packet to store data that the server is sending us
            byte[] receiveData = new byte[1024];
            setReceive_packet(new DatagramPacket(receiveData, receiveData.length));

            //Open a socket to send the packet
            DatagramSocket clientSoc = new DatagramSocket();

            //SEND THE PACKET
            System.out.print("Sending UDP packet: "+attemptNum);
            clientSoc.send(send_pkt);

            //RECEIVE THE PACKET
            getUdpPacket(clientSoc);

            //Set the flag that we indeed received the packet
            setPacketReceived(true);

            //Close the client socket
            clientSoc.close();

        }catch (SocketException skt){
            System.out.println("Socket problem");
            skt.printStackTrace();
        }catch (IOException io){
            System.out.println("IO Exception");
            io.printStackTrace();
        }catch (Exception e){
            System.out.println("Exception");
            e.printStackTrace();
        }
    }

    public synchronized void getUdpPacket(DatagramSocket sock) throws Exception{
            //Hopefully we receive this! Can timeout here...
            sock.receive(getReceive_packet());
            if (getReceive_packet() == null){
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
