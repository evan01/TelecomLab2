package lab;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * TelecomLab2
 * Created by eknox on 2016-10-18.
 */
public class DnsPacketResponse {
    String ipAddr;
    int ttl;
    int queryType;
    int auth;
    String authString;
    int numAnswers;

    DnsPacketResponse() {
        numAnswers = 1;
    }

    /**
     * This will take in a stream of bytes and then parse out the answer
     * @param response
     */
    public void parseDnsPacketResponse(byte[] response, DNSOptions opts){
        System.out.println("PARSING THE PACKET");

        //Depending on the kind of packet, parse it differently
        if (opts.queryType.equals("MX")){

        }

        if (opts.queryType.equals("NS")){
            //Name server query
        }

        if (opts.queryType.equals("A")){
            //standard ip query
        }

    }

    public void parseDnsPacketResponse2(byte[] response) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response));

        //Only interested in some of the components of the response
        try {
            //Skip the bytes we don't care about in the reader
            in = skipRedundantBytes(in);

            //read the authority
            this.auth = (int) in.readShort();
            convertAuthToString();
            //Get the query type
            this.queryType = (int) in.readShort();
            // Read the ttl
            this.ttl = in.readInt();

            //Read the ip address
            this.ipAddr = readIpAddress(in);

        } catch (Exception e) {
            System.out.println("There was an IO exception!");
            e.printStackTrace();
        }
    }

    private void convertAuthToString() {
        if (this.auth == 1) {
            this.authString = "auth";
        }else {
            this.authString = "nonauth";
        }
    }


    private String readIpAddress(DataInputStream in) throws Exception {
        short addrLen = in.readShort();
        String address = "";
        for (int i = 0; i < addrLen; i++) {
            address = address + ("" + String.format("%d", (in.readByte() & 0xFF)) + ".");
        }
        //Delete the last .
        address = address.substring(0,address.length()-1);
        return address;
    }


    private DataInputStream skipRedundantBytes(DataInputStream in) throws Exception {
        //Skip the Header
        for (int i = 0; i < 6; i++) {
            in.readShort();
        }

        //Skip the query
        int j = 0;
        while ((j = in.readByte()) > 0) {
            for (int i = 0; i < j; i++) {
                in.readByte();
            }
        }

        //Skip redundant info
        for (int k = 0; k < 3; k++) {
            in.readShort();
        }
        return in;
    }
}
