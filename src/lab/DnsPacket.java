package lab;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Random;

public class DnsPacket {

    public String QNAME;
    public String QTYPE;
    public byte[] HEADER;
    public byte[] QUESTION;
    public byte[] ANSWER;
    public int UDP_DATA_BLOCK_SIZE = 512; // RFC791
    //This is the byte[] representation of the packet
    byte[] packetByte;
    //These are the arguments that were supplied to the packet
    DNSOptions options;
    // Header attributes
    short ID;
    byte QR, OPCODE, AA, TC, RD, RA, Z, RCODE;
    short QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT;
    // QTYPE: 16-bit code specifying the type of query.
    private String TYPE_A = "A";
    private String TYPE_NS = "NS";
    private String TYPE_MX = "MX";

    // Constructors

    /**
     * This constructor will create a DNS packet based off of the options supplied
     *
     * @param option
     */
    public DnsPacket(DNSOptions option) {
        this.QNAME = option.query;
        this.QTYPE = option.queryType;
        this.options = option;

        short id = generateRandomID();

        HEADER = packetHeader(id,
                (byte) 0, // QR: query (0) or response(1)
                (byte) 0, // OPCODE: 0000 for standard query
                (byte) 0, // AA: 0 not meaning for a query
                (byte) 0, // TC: 0 not truncated
                (byte) 1, // RD: 1 recursion requested
                (byte) 0, // RA: 0 not meaningful for a query
                (byte) 0, // Z: 0 reserved, set to zero
                (byte) 0, // RCODE: 0 not meaning for a query
                (short) 1, // QDCOUNT: 0001 one question
                (short) 0, // ANCOUNT: 0000 no records in the Answer section
                (short) 0, // NSCOUNT: 0000 no record in the Authoritative section
                (short) 0); // ARCOUNT: 0000 No records in the Additional section

        QUESTION = packetQuestion(QNAME, QTYPE, (short) 0x0001);

        int size = UDP_DATA_BLOCK_SIZE - HEADER.length - QUESTION.length;
        ANSWER = packetAnswer(size);

        ByteBuffer packetByteBuffer = ByteBuffer.allocate(HEADER.length + QUESTION.length);
        packetByteBuffer.put(HEADER);
        packetByteBuffer.put(QUESTION);
        //packetByteBuffer.put(ANSWER);

        packetByte = packetByteBuffer.array();
    }

    public static void main(String args[]) {
        DNSOptions opts = new DNSOptions();
        opts.query = "www.mcgill.ca";
        opts.queryType = "A";

        DnsPacket p = new DnsPacket(opts);
        byte[] packet = p.packetByte;

        String stringByteRep = new String(packet);
        System.out.print(stringByteRep);
    }

    public byte[] packetHeader(
            short id,
            byte qr,
            byte opcode,
            byte aa,
            byte tc,
            byte rd,
            byte ra,
            byte z,
            byte rcode,
            short qdcount,
            short ancount,
            short nscount,
            short arcount) {

        this.ID = id;
        this.QR = qr;
        this.OPCODE = opcode;
        this.AA = aa;
        this.TC = tc;
        this.RD = rd;
        this.RA = ra;
        this.Z = z;
        this.RCODE = rcode;
        this.QDCOUNT = qdcount;
        this.ANCOUNT = ancount;
        this.NSCOUNT = nscount;
        this.ARCOUNT = arcount;

        byte[] header = new byte[12];
        header[0] = (byte) ((id >> 8) & 0xff);
        header[1] = (byte) (id & 0xff);

        header[2] = (byte) ((qr << 7) | (opcode << 3) | (aa << 2) | (tc << 1) | rd);
        header[3] = (byte) ((ra << 7) | (z << 4) | rcode);

        header[4] = (byte) (qdcount >>> 8);
        header[5] = (byte) qdcount;

        header[6] = (byte) (arcount >>> 8);
        header[7] = (byte) arcount;

        header[8] = (byte) (ancount >>> 8);
        header[9] = (byte) ancount;

        header[10] = (byte) (nscount >>> 8);
        header[11] = (byte) nscount;

        return header;
    }

    // Construct packet question
    public byte[] packetQuestion(String qname, String qtype, short qclass) {

        short qtypeShort = parseType(qtype);

        byte[] qnameByteArray = parseQNAME(qname);
        ByteBuffer question = ByteBuffer.allocate(qnameByteArray.length + 2 * Short.BYTES);

        question.put(qnameByteArray);
        question.putShort(qtypeShort);
        question.putShort(qclass);

        return question.array();
    }

    // Construct packet answer
    public byte[] packetAnswer(int size) {
        byte[] answer = new byte[size];
        return answer;
    }

    public void interpretHeader(byte[] header) {

    }

    public void interpretQuestion(byte[] question) {

    }

    public void interpretAnswer(byte[] header, byte[] question, byte[] answer) {

        int index = 0;
        for (byte b : answer) {
            // e.g. [c0 0c] [22 33] [44 55] [66 77 88 99] [aa bb] [cc dd ee ff]
            if (String.format("%02X", b).equals("C0")) {

                Hashtable<String, String> packet = new Hashtable<String, String>();

                short responseTYPE = (short) ((answer[index + 2] << 8) | answer[index + 3]);
                packet.put("TYPE", Short.toString(responseTYPE));

                short responseCLASS = (short) ((answer[index + 4] << 8) | answer[index + 5]);
                packet.put("CLASS", Short.toString(responseCLASS));

                long responseTTL = (long) (((answer[index + 6] << 24) | (answer[index + 7]) << 16 | answer[index + 8]) << 8 | answer[index + 9]);
                packet.put("TTL", Long.toString(responseTTL));

                short responseRDLENGTH = (short) ((answer[index + 10] << 8) | answer[index + 11]);
                packet.put("RDLEGNTH", Short.toString(responseRDLENGTH));

                byte[] responseRDATA = new byte[responseRDLENGTH];

                // IP Address
                if (responseTYPE == 0x0001) {
                    for (int i = 0; i < responseRDATA.length; i++) {
                        responseRDATA[i] = answer[index + 12 + i]; // offset = 12
                    }

                    try {
                        InetAddress ipAddr = InetAddress.getByAddress(responseRDATA);
                        packet.put("RDATA", ipAddr.getHostAddress());
                    } catch (UnknownHostException e) {
                        System.out.println("Invalid IP address");
                    }
                }

                // Name server (name of the server)
                else if (responseTYPE == 0x0002) {

                }

                // CNAME (name of the alias)
                else if (responseTYPE == 0x0005) {

                }

                // Mail server
                else if (responseTYPE == 0x000f) {

                }

                // Error
                else {
                    System.out.println("Error parsing RDATA for " + responseTYPE);
                }
            }
            index++;
        }
    }

    // Parse domain name (QNAME) into sequence of bytes
    private byte[] parseQNAME(String qname) {
        // Create the byte buffer that will contain all the domain name byte values
        ByteBuffer qnameByteArray = ByteBuffer.allocate(qname.length() + 2);

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
    public short parseType(String qtype) {
        short qtypeShort;
        if (qtype.toUpperCase() == TYPE_A) {
            qtypeShort = 0x0001;    // IP address
        } else if (qtype.toUpperCase() == TYPE_NS) {
            qtypeShort = 0x0002;    // Name server
        } else if (qtype.toUpperCase() == TYPE_MX) {
            qtypeShort = 0x000f;    // Mail server
        } else {
            qtypeShort = 0x0000;
            System.out.println("Invalid QTYPE: " + qtype);
        }
        return qtypeShort;
    }

    // Returns a random Short ID
    public short generateRandomID() {
        Random r = new Random();
        short rShort = (short) r.nextInt(32767); // Short = 2 bytes = -32768 to 32767
        return rShort;
    }
}
