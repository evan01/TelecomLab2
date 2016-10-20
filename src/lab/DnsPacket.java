package lab;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

public class DnsPacket {

    public byte[] HEADER;
    public byte[] QUESTION;
    public byte[] ANSWER;
    public int UDP_DATA_BLOCK_SIZE = 512; // RFC791
    
    //This is the byte[] representation of the packet
    byte[] packet;
    
    //These are the arguments that were supplied to the packet
    DNSOptions options;
    
    // Header attributes
    short ID;
    byte QR, OPCODE, AA, TC, RD, RA, Z, RCODE;
    short QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT;
    
    // Question attributes
    public String QNAME, QTYPE, QCLASS;

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

        HEADER = createHeader(id,
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

        QUESTION = createQuestion(QNAME, QTYPE, (short) 0x0001);

//        int size = UDP_DATA_BLOCK_SIZE - HEADER.length - QUESTION.length;
//        ANSWER = packetAnswer(size);

        ByteBuffer packetByteBuffer = ByteBuffer.allocate(HEADER.length + QUESTION.length);
        packetByteBuffer.put(HEADER);
        packetByteBuffer.put(QUESTION);
//        packetByteBuffer.put(ANSWER);
        
        packet = packetByteBuffer.array();
        //todo delete these or use a debugger
//        System.out.println("\nSent: " + packet.length + " bytes");
//        for (int i =0; i< packet.length; i++) {
//            System.out.print("0x" + String.format("%02x", packet[i]) + " ");
//        }
//        System.out.println("\n");
    }

    public static void main(String args[]) {
        DNSOptions opts = new DNSOptions();
        opts.query = "www.mcgill.ca";
        opts.queryType = "A";
        
        DnsPacket p = new DnsPacket(opts);
//        byte[] packet = p.packetByte;

//        String stringByteRep = new String(packet);
//        System.out.print(stringByteRep);
        
        byte[] testpacket = hexStringToByteArray("12348180000100010000000003777777066d6367696c6c0263610000010001c00c0001000100000024000484d8b1a0");
        
//        for (int i =0; i< testpacket.length; i++) {
//            System.out.print("0x" + String.format("%02x", testpacket[i]) + " " );            
//        }
//        System.out.println("");
        
        p.interpretPacket(testpacket);        
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public byte[] createHeader(
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
    public byte[] createQuestion(String qname, String qtype, short qclass) {

        short qtypeShort = parseQTYPE(qtype); //2 bytes

        byte[] qnameByteArray = parseQNAME(qname); //2bytes
        ByteBuffer question = ByteBuffer.allocate(qnameByteArray.length + 2 * Short.BYTES);

        question.put(qnameByteArray);
        question.putShort(qtypeShort);
        question.putShort(qclass);

        return question.array();
    }

    // Construct packet answer
    public byte[] createAnswer(int size) {
        byte[] answer = new byte[size];
        return answer;
    }
    
    public void interpretPacket(byte[] packet) {
    	
    	System.out.println("Received: " + packet.length);    	
    	for (int i =0; i< packet.length; i++) {
            System.out.print("0x" + String.format("%02x", packet[i]) + " ");
        }
    	System.out.println("\n");
    	
    	int header_size = this.HEADER.length;
    	int question_size = this.QUESTION.length;
    	int answer_size = packet.length - header_size - question_size;
		byte[] newHeader = Arrays.copyOfRange(packet, 0, header_size);
		byte[] newQuestion = Arrays.copyOfRange(packet, header_size, header_size + question_size);
		byte[] newAnswer = Arrays.copyOfRange(packet, header_size + question_size, header_size + question_size + answer_size);
		
		interpretHeader(newHeader);
		interpretQuestion(newQuestion);
		interpretAnswer(newAnswer);
    }
    
    public void interpretHeader(byte[] header) {
    	this.ID = (short) ((header[0] << 8) | header[1]);
		this.QR = (byte) ((header[2] & 0xff) >>> 7);
		this.OPCODE = (byte) (((header[2] & 0xff) >>> 3) & 0x0f);
		this.AA = (byte) (((header[2] & 0xff) >>> 2) & 0x01);
		this.TC = (byte) (((header[2] & 0xff) >>> 1) & 0x01);
		this.RD = (byte) ((header[2] & 0xff) & 0x01);
		this.RA = (byte) (((header[3] & 0xff) >>> 7) & 0x01);
		this.Z = (byte) (((header[3] & 0xff) >>> 4) & 0x07);
		this.RCODE = (byte) ((header[3] & 0xff) & 0x0f);
		this.QDCOUNT = (short) ((header[4] << 8) | header[5]);
		this.ANCOUNT = (short) ((header[6] << 8) | header[7]);
		this.NSCOUNT = (short) ((header[8] << 8) | header[9]);
		this.ARCOUNT = (short) ((header[10] << 8) | header[11]);
		
		System.out.println("ID:	 0x" + String.format("%02x", this.ID));
		System.out.println("Flags:	 0x" + String.format("%02x", header[2]) + String.format("%02x", header[3]));
		System.out.println("QDCOUNT: 0x" + String.format("%04x", this.QDCOUNT));
		System.out.println("ANCOUNT: 0x" + String.format("%04x", this.ANCOUNT));
		System.out.println("NSCOUNT: 0x" + String.format("%04x", this.NSCOUNT));
		System.out.println("ARCOUNT: 0x" + String.format("%04x", this.ARCOUNT));		
    }

    public void interpretQuestion(byte[] question) {
    	byte[] qnameArray = Arrays.copyOfRange(question, 0, question.length - 4);
		byte[] qtypeArray = Arrays.copyOfRange(question, question.length - 4, question.length - 2);
		byte[] qclassArray = Arrays.copyOfRange(question, question.length - 2, question.length);
		
		short qtypeShort = (short)((qtypeArray[1] << 8) | qtypeArray[0]);
		short qclassShort = (short)((qclassArray[1] << 8) | qclassArray[0]);
				
		this.QTYPE = "" + qtypeArray[0] + qtypeArray[1];
		this.QCLASS = "" + qclassArray[0] + qclassArray[1];
		
		System.out.println("QName:	 " + this.QNAME);
		System.out.println("QType:	 " + this.QTYPE);
		System.out.println("QClass:	 " + this.QCLASS);
    }

	public void interpretAnswer(byte[] answer) {

        int index = 0;
        for (byte b : answer) {
            // e.g. [c0 0c] [22 33] [44 55] [66 77 88 99] [aa bb] [cc dd ee ff]
            if (String.format("%02x", b).equals("c0")) {

                Hashtable<String, String> packet = new Hashtable<String, String>();

                short responseTYPE = (short) ((answer[index + 2] << 8) | answer[index + 3]);
                packet.put("TYPE", Short.toString(responseTYPE));
                System.out.println("AType:	 0x" + String.format("%04x", responseTYPE));

                short responseCLASS = (short) ((answer[index + 4] << 8) | answer[index + 5]);
                packet.put("CLASS", Short.toString(responseCLASS));
                System.out.println("AClass:	 0x" + String.format("%04x", responseCLASS));

                long responseTTL = (long) (((answer[index + 6] << 24) | (answer[index + 7]) << 16 | answer[index + 8]) << 8 | answer[index + 9]);
                packet.put("TTL", Long.toString(responseTTL));
                System.out.println("TTL:	 0x" + String.format("%04x", responseTTL));

                short responseRDLENGTH = (short) ((answer[index + 10] << 8) | answer[index + 11]);
                packet.put("RDLEGNTH", Short.toString(responseRDLENGTH));
                System.out.println("Length:	 0x" + String.format("%04x", responseRDLENGTH));

                byte[] responseRDATA = new byte[responseRDLENGTH];

                // IP Address
                if (responseTYPE == 0x0001) {
                    for (int i = 0; i < responseRDATA.length; i++) {
                        responseRDATA[i] = answer[index + 12 + i]; // offset = 12
                    }

                    try {
                        InetAddress ipAddr = InetAddress.getByAddress(responseRDATA);
                        packet.put("RDATA", ipAddr.getHostAddress());
                        System.out.println("Address: " + packet.get("RDATA"));
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
// todo delete this       System.out.println(qname + " has " + labels.length + " labels");

        for (String label : labels) {
            // Each label is preceded by a single byte giving the length of label
            qnameByteArray.put((byte) label.length());
            try {
                // Characters are replaced by their 8-bit ASCII representation
                qnameByteArray.put(label.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                System.out.println("label cannot be encoded into byte array: " + label);
            }
// todo delete this           System.out.println("Writing: " + label);
        }

        // To signal the end of a domain name, one last byte is written with value 0
        qnameByteArray.put((byte) 0);

        return qnameByteArray.array();
    }

    // Given a String "IP", "NS", "MX" etc. and output its short representation
    public short parseQTYPE(String qtype) {
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
    
    public String parseQTYPE(short qtype) {
    	String qtypeString;
    	if (qtype == 0x0001) {
            qtypeString = "A";    // IP address
        } else if (qtype == 0x0002) {
            qtypeString = "NS";    // Name server
        } else if (qtype == 0x000f) {
            qtypeString = "MX";    // Mail server
        } else {
            qtypeString = "ERROR";
            System.out.println("Invalid QTYPE: " + qtype);
        }
        return qtypeString;
    }

    // Returns a random Short ID
    public short generateRandomID() {
        Random r = new Random();
        short rShort = (short) r.nextInt(32767); // Short = 2 bytes = -32768 to 32767
        return rShort;
    }
}
