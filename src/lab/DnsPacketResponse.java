package lab;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * TelecomLab2
 * Created by eknox on 2016-10-18.
 */
public class DnsPacketResponse {


    ArrayList<String> records;
    ArrayList<String> additionalRecords;

    int questCnt;
    int answerCnt;
    int nsAuthCnt;
    int addRecCnt;
    int headerLen = 12;
    int questLen;
    String authority;

    /**
     * This will take in a stream of bytes and then parse out the answer
     *
     * @param response
     */
    public void parseDnsPacketResponse(byte[] response, DNSOptions opts, int questionLength) throws Exception {
        this.questLen = questionLength;

        //Regardless on the kind of packet, we will parse the header the same every time
        DataInputStream responseData = parseDnsHeader(response);

        //We will also want to send JUST the bytes containing the answer to the parser methods, skip question
        responseData.skipBytes(questionLength);

        //todo Now we just have to parse JUST the responses returned by the server
        //http://www.networksorcery.com/enp/protocol/dns.htm#Answer RRs is a good website

        //Do the normal records first
        int totalResponses = answerCnt;
        records = new ArrayList<String>();
        for (int i = 0; i < totalResponses; i++) {
            records.add(parseDnsRecord(responseData, response));
        }

        //todo make sure that we can indeed parse additional records as well
        //Then do the additional records
        additionalRecords = new ArrayList<String>();
        int totalAdditionalResponses = addRecCnt;
        for (int i = 0; i < totalAdditionalResponses; i++) {
            additionalRecords.add(parseDnsRecord(responseData, response));
        }

        //todo possibly delete this
        if (this.nsAuthCnt > 0) {
            System.out.print("Server returned an authoritative type of message");
        }
    }

    private String parseDnsRecord(DataInputStream responseData, byte[] response) throws Exception {

        String record = "";

        String name = readRecordName(responseData, response);

        //The next part is the TYPE
        String type = getRecordType(responseData);

        //Next is the CLASS
        String respClass = getRecordClass(responseData);

        //Then the TTL
        int ttl = responseData.readInt();

        //Then the data length
        int dataL = responseData.readShort();

        //Then depending on the kind of record, read the data!
        if (type == "IP") {
            String data = readIpData(responseData, dataL);
            record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
        }

        if (type == "CNAME") {
            String data = "";//todo not implemented
            record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
        }

        if (type == "MX") {
            String data = "";//todo not implemented
            record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
        }

        if (type == "NS") {
            String data = "";//todo not implemented
            record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
        }


        return record;
    }

    private String readIpData(DataInputStream responseData, int dataL) throws Exception {
        boolean first = true;
        String ipAdd = "";
        //Should just be reading ip addr for ip query
        for (int i = 0; i < dataL; i++) {
            int addr = Byte.toUnsignedInt(responseData.readByte());
            if (first) {
                ipAdd = ipAdd + addr;
                first = false;
            } else {
                ipAdd = ipAdd + "." + addr;
            }

        }
        return ipAdd;
    }


    private String getRecordClass(DataInputStream responseData) throws Exception {
        String recClass = "";
        //We know that the type is 2 bytes
        int cls = responseData.readShort();

        switch (cls) {
            case 1:
                recClass = "IN";
                break;
            default:
                recClass = "NoCls:" + cls;
                break;
        }

        return recClass;

    }

    private String getRecordType(DataInputStream responseData) throws Exception {
        String recType = "";

        //We know that the type is 2 bytes
        int type = responseData.readByte();

        switch (type) {
            case 1:
                recType = "IP";
                break;
            case 5:
                recType = "CNAME";
                break;
            case 15:
                recType = "MX";
                break;
            case 2:
                recType = "NS";
                break;
            default:
                recType = "CODE:" + type;
                break;
        }

        return recType;
    }

    private String readRecordName(DataInputStream responseData, byte[] response) throws IOException {
        //As we read this name, we can either have a compression label or a normal label
        String recordName = "";
        boolean endOfName = false;
        byte currentByte;
        while (!endOfName) {
            currentByte = responseData.readByte();
            if (currentByte == (byte) (-64)) {
                responseData.reset();
                responseData.skipBytes(questLen + headerLen);
                recordName = recordName + getCompressionLabel(responseData, response);
            }
            if (currentByte == (byte) 0) {
                endOfName = true;
            }
        }

        return recordName;
    }

    private String getCompressionLabel(DataInputStream responseData, byte[] response) throws IOException {
        //Every time you see this byte sequence you know to sub it with a specific string
        short compLabelPointer = responseData.readShort();
        short compLabelID = (short) 49152;//Represents 110000000 00000000
        int offset = compLabelPointer - compLabelID;

        //Now with the offset, read the first bit of the label, which will tell us the length of the
        //First part of the label mcgill there could be a .ca after however
        String strLable = "";
        boolean first = true;
        //Then read the label, not that we know how long it is
        while (response[offset] != (byte) 0) {
            byte labelLengthByte = response[offset];
            byte[] label = new byte[labelLengthByte];
            for (int i = 0; i < label.length; i++) {
                label[i] = response[offset + i + 1];
            }
            offset += (label.length + 1);
            if (first) {
                strLable = new String(label);
                first = false;
            } else {
                strLable = strLable + "." + new String(label);
            }
        }
        return strLable;
    }

    private DataInputStream parseDnsHeader(byte[] response) throws IOException {
        //The important thing here is to parse the header so that you can get the number of records
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response));

        //Use Another method to actually interpret the bytes
        in.readByte();
        in.readByte();
        String flagByte1 = Integer.toBinaryString(in.readByte()).substring(Integer.SIZE - Byte.SIZE);
        if (flagByte1.charAt(5) == '1') {
            this.authority = "auth";
        } else {
            this.authority = "nonauth";
        }

        in.reset();
        in.skipBytes(4);
        this.questCnt = in.readShort();//2 bytes

        //Do read the answer record count
        this.answerCnt = in.readShort(); //2 bytes

        //Do read the NS authority record count
        this.nsAuthCnt = in.readShort();//2 bytes

        //Do read the additional record count
        this.addRecCnt = in.readShort();//2 bytes

        return in;
    }

    public ArrayList<String> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<String> records) {
        this.records = records;
    }

}
