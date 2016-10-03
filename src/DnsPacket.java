import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Hashtable;


public class DnsPacket {
	// QTYPE: 16-bit code specifying the type of query.
	private String TYPE_A = "A";
	private String TYPE_NS = "NS";
	private String TYPE_MX = "MX";
	
	public String NAME;
	public int TYPE;
	public byte[] HEADER;
	public byte[] QUESTION;
	public byte[] ANSWER;
	
	// Constructors
	public DnsPacket() {
		
	}
	
	public DnsPacket(String name, int type) {
		this.NAME = name;
		this.TYPE = type;
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
		
		byte[] header = new byte[12];
		
		header[0] = (byte)(id >>> 8);
		header[1] = (byte)(id);
		header[2] = (byte)((qr << 7) | (opcode << 3) |(aa << 2) | (tc << 1) | rd);
		header[3] = (byte)((ra << 7) | (z << 4) | rcode);
		header[4] = (byte)(qdcount >>> 8);
		header[5] = (byte)qdcount;
		header[6] = (byte)(arcount >>> 8);
		header[7] = (byte)arcount;
		header[8] = (byte)(ancount >>> 8);
		header[9] = (byte)ancount;
		header[10] = (byte)(nscount >>> 8);
		header[11] = (byte)nscount;
			
		return header;
	}
	
	public byte[] packetQuestion (
			String qname,
			String qtype,
			short qclass) {
		
		short qtypeShort;
		if (qtype.toUpperCase() == TYPE_A) {
			qtypeShort = 0x0001;	// IP address
		} else if (qtype.toUpperCase() == TYPE_NS) {
			qtypeShort = 0x0002;	// Name server
		} else if (qtype.toUpperCase() == TYPE_MX){
			qtypeShort = 0x000f;	// Mail server
		} else {
			qtypeShort = 0x0000;
			System.out.println("Invalid QTYPE: " + qtype);
		}
		
		byte[] qnameByteArray = parseQNAME(qname);		
		ByteBuffer question = ByteBuffer.allocate(qnameByteArray.length + 2*Short.BYTES);
		
		question.put(qnameByteArray);
		question.putShort(qtypeShort);
		question.putShort(qclass);
		
		return question.array();		
	}
	
	public byte[] packetAnswer() {
		ByteBuffer answer = ByteBuffer.allocate(0);
		
		return answer.array();
	}
	
	public void parseAnswer(
			byte[] header,
			byte[] question,
			byte[] answer) {
		
		int index = 0;
		for (byte b : answer) {
			// e.g. [c0 0c] [22 33] [44 55] [66 77 88 99] [aa bb] [cc dd ee ff] 
			if (String.format("%02X", b).equals("C0")) {
				
				Hashtable<String, String> packet = new Hashtable<String, String>();
				
				short responseTYPE = (short)((answer[index + 2] << 8) | answer[index + 3]);
				packet.put("TYPE", Short.toString(responseTYPE));
				
				short responseCLASS = (short)((answer[index + 4] << 8) | answer[index + 5]);
				packet.put("CLASS", Short.toString(responseCLASS));
				
				long responseTTL = (long)(((answer[index + 6] << 24) | (answer[index + 7]) << 16 | answer[index + 8]) << 8 | answer[index + 9]);
				packet.put("TTL", Long.toString(responseTTL));
				
				short responseRDLENGTH = (short)((answer[index + 10] << 8) | answer[index + 11]);
				packet.put("RDLEGNTH", Short.toString(responseRDLENGTH));
				
				byte[] responseRDATA = new byte[responseRDLENGTH];
				
				// IP Address
				if(responseTYPE == 0x0001) {					
					for (int i = 0; i < responseRDATA.length; i++) {
						responseRDATA[i] = answer[index + 12 + i];
					}
					
					try {
						InetAddress ipAddr = InetAddress.getByAddress(responseRDATA);
						packet.put("RDATA", ipAddr.getHostAddress());
					} catch (UnknownHostException e){
						System.out.println("Invalid IP address");
					}
				
				// Name server
				} else if (responseTYPE == 0x0002) {
					for (int i = 0; i < responseRDATA.length; i++) {
						responseRDATA[i] = answer[index + 12 + i];
					}
				
				
				// CNAME (name of the alias)
				} else if (responseTYPE == 0x0005) {
					
				// Mail server					
				} else if (responseTYPE == 0x000f) {
				
				// Error
				} else {
					
				}
			}						
		}
		
	}
	
	// Parse domain name (QNAME) into sequence of bytes 
	private byte[] parseQNAME(String qname) {		
		ByteBuffer qnameByteArray = ByteBuffer.allocate(qname.length());
		
		String[] labels = qname.split(".");
		
		for(String label : labels) {
			// Each label is preceded by a single byte giving the length of label
			qnameByteArray.put((byte)label.length());		
			try {
				// Characters are replaced by their 8-bit ASCII representation
				qnameByteArray.put(label.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				System.out.println("label cannot be encoded into byte array: " + label);
			}			
		}
		
		// To signal the end of a domain name, one last byte is written with value 0
		qnameByteArray.put((byte)0);
		
		return qnameByteArray.array();
	}
	
}
