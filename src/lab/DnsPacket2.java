package lab;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.io.ByteArrayOutputStream;
/**
 * TelecomLab2
 * Created by eknox on 2016-10-18.
 */
public class DnsPacket2 {

    byte[] dnsData; //full header + data for dns
    byte[] header;
    byte[] query;
    public DNSOptions options;

    public DnsPacket2(DNSOptions options){
        this.options = options;
        //Construct the header first
        this.header= createHeader();

        //Then handle the actual query
        this.query = createQuestion(options.query,options.queryType);

        //byte extraSpace with 6 bytes
        byte extraSpace[] = new byte[6];

        //Then combine the two creating additonal space for response data
        //Append all the byte arrays together
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try{
            outputStream.write(this.header);
            outputStream.write(this.query);
            outputStream.write(extraSpace);

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Unable to append the two elements together");
        }
        //Finally set the dnsData output to hopefully be the correct one
        this.dnsData = outputStream.toByteArray();
    }


    private byte[] createHeader() {
        byte[] identifier;
        byte[] flags;
        byte[] questionCnt;
        byte[] answerRecCnt;
        byte[] nsAuthCnt;
        byte[] addRecCnt;

        //Start with the identifier
        identifier = generateRandomID();

        //Then do the flags
        flags = getFlags();

        //Then do the question count, answerRecCnt, nsAuthCnt, addRecCnt
        questionCnt = generateEmpty16Bits();
        answerRecCnt = generateEmpty16Bits();
        nsAuthCnt = generateEmpty16Bits();
        addRecCnt = generateEmpty16Bits();

        //Append all the byte arrays together
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(identifier);
            outputStream.write(flags);
            outputStream.write(questionCnt);
            outputStream.write(answerRecCnt);
            outputStream.write(nsAuthCnt);
            outputStream.write(addRecCnt);
        }catch (Exception e){
            System.out.println("Unable to append the bytes together");
            e.printStackTrace();
        }

        //Return the final value
        return outputStream.toByteArray();

    }

    private byte[] getFlags() {
        BitSet flags = new BitSet(16);
        flags.set(0000000000000000); //1 means recursive query
        flags.set(7);
        return flags.toByteArray();
    }

    private byte[] generateEmpty16Bits(){
        BitSet flags = new BitSet(16);
        flags.set(0000000000000000);
        return flags.toByteArray();
    }

    // Returns a random Short ID
    public byte[] generateRandomID() {
        Random r = new Random();
        short rShort = (short)r.nextInt(32767); // Short = 2 bytes = -32768 to 32767
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(rShort);
        return buffer.array();
    }


    // Construct packet question
    private byte[] createQuestion(String qname, String qtype) {
        BitSet bs = new BitSet(16);
        bs.set(0000000000000000);
        byte qclass[] = bs.toByteArray(); //Class

        byte queryType[] = parseType(qtype); //Type
        byte qName[] = parseQNAME(qname); //Name

        //Append all the byte arrays together
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(qName);
            outputStream.write(queryType);
            outputStream.write(qclass);
        }catch (Exception e){
            System.out.println("Unable to append the bytes together");
            e.printStackTrace();
        }
        //Return the final value
        return outputStream.toByteArray();

    }

    // Parse domain name (QNAME) into sequence of bytes
    private byte[] parseQNAME(String qname) {
        // Create the byte buffer that will contain all the domain name byte values
        ByteBuffer qnameByteArray = ByteBuffer.allocate(qname.length()+2);

        String[] labels = qname.split("\\.");

        for (String label : labels) {
            // Each label is preceded by a single byte giving the length of label
            qnameByteArray.put((byte) label.length());
            try {
                // Characters are replaced by their 8-bit ASCII representation
                qnameByteArray.put(label.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                System.out.println("label cannot be encoded into byte array: " + label);
            }
        }

        // To signal the end of a domain name, one last byte is written with value 0
        qnameByteArray.put((byte) 0);

        return qnameByteArray.array();
    }

    // Given a String "IP", "NS", "MX" etc. and output its short representation
    public byte[] parseType(String qtype) {
        // QTYPE: 16-bit code specifying the type of query.
        String TYPE_A = "A";
        String TYPE_NS = "NS";
        String TYPE_MX = "MX";

        BitSet bs = new BitSet();

        byte[] queryType;

        if (qtype.toUpperCase() == TYPE_A) {
            bs.set(0000000000000001);    // IP address
            queryType = bs.toByteArray();
        } else if (qtype.toUpperCase() == TYPE_NS) {
            bs.set(0000000000000002);
            queryType = bs.toByteArray();   // Name server
        } else if (qtype.toUpperCase() == TYPE_MX) {
            bs.set(0000000000001111);
            queryType = bs.toByteArray();   // Name server
        } else {
            bs.set(0000000000000000);
            queryType = bs.toByteArray();   // Name server
        }
        return queryType;
    }

}
