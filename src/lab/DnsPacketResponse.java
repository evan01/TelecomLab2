package lab;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * TelecomLab2
 * Created by eknox on 2016-10-18.
 */
public class DnsPacketResponse {

    String[] records;
    int questLen;
    int questCnt;
    int answerCnt;
    int nsAuthCnt;
    int addRecCnt;

    /**
     * This will take in a stream of bytes and then parse out the answer
     *
     * @param response
     */
    public void parseDnsPacketResponse(byte[] response, DNSOptions opts,int questionLength) throws IOException {
        this.questLen = questionLength;

        //Regardless on the kind of packet, we will parse the header the same every time
        DataInputStream responseData = parseDnsHeader(response);

        //We will also want to send JUST the bytes containing the answer to the parser methods, skip question
        for (int i =0;i<questLen;i++){
            responseData.readByte();
        }

        //todo Now we just have to parse JUST the responses returned by the server
        //http://www.networksorcery.com/enp/protocol/dns.htm#Answer RRs is a good website



    }

    private DataInputStream parseDnsHeader(byte[] response) throws IOException {
        //The important thing here is to parse the header so that you can get the number of records
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response));

        //Skip the Identifier(16bits), flags(16bits) and the question count (4 bytes)
        in.readInt();//4 bytes
        this.questCnt = in.readShort();//2 bytes

        //Do read the answer record count
        this.answerCnt = in.readShort(); //2bytes

        //Do read the NS authority record count
        this.nsAuthCnt = in.readShort();//2 bytes

        //Do read the additional record count
        this.addRecCnt = in.readShort();//2 bytes

        return in;
    }

//    public void parseDnsPacketResponse2(byte[] response) {
//        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response));
//
//        //Only interested in some of the components of the response
//        try {
//            //Skip the bytes we don't care about in the reader
//            in = skipRedundantBytes(in);
//
//            //read the authority
//            this.auth = (int) in.readShort();
//            convertAuthToString();
//            //Get the query type
//            this.queryType = (int) in.readShort();
//            // Read the ttl
//            this.ttl = in.readInt();
//
//            //Read the ip address
//            this.ipAddr = readIpAddress(in);
//
//        } catch (Exception e) {
//            System.out.println("There was an IO exception!");
//            e.printStackTrace();
//        }
//    }

//    private void convertAuthToString() {
//        if (this.auth == 1) {
//            this.authString = "auth";
//        }else {
//            this.authString = "nonauth";
//        }
//    }


    private String readIpAddress(DataInputStream in) throws Exception {
        short addrLen = in.readShort();
        String address = "";
        for (int i = 0; i < addrLen; i++) {
            address = address + ("" + String.format("%d", (in.readByte() & 0xFF)) + ".");
        }
        //Delete the last .
        address = address.substring(0, address.length() - 1);
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
