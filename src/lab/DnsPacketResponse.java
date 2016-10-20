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
    String compressionLabel;
    String mxPref;

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

        //Do the normal records first
        int totalResponses = answerCnt;
        records = new ArrayList<String>();
        for (int i = 0; i < totalResponses; i++) {
            responseData = parseDnsRecord(responseData, response, true);
        }

        //Then do the additional records
        additionalRecords = new ArrayList<String>();
        int totalAdditionalResponses = addRecCnt;
        for (int i = 0; i < totalAdditionalResponses; i++) {
            responseData = parseDnsRecord(responseData, response, false);
        }

    }

    private DataInputStream parseDnsRecord(DataInputStream responseData, byte[] response, boolean isNormalRecord) throws Exception {

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
            String data = readIpData(responseData, response, dataL);
            record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
        }

        if (type == "CNAME") {
            String data = readNonIpData(responseData, dataL, response);
            record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
        }

        if (type == "MX") {

            String data = readNonIpData(responseData, dataL, response);
            if (records.size() < 1) {
                this.mxPref = data;
                record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
                record = record.replaceAll("\r", "");
            } else {
                record = type + "\t" + data + "\t" + this.mxPref + "\t" + ttl + "\t" + this.authority;
                record = record.replaceAll("\r", "");
            }

        }

        if (type == "NS") {
            String data = readNonIpData(responseData, dataL, response);
            record = type + "\t" + data + "\t" + ttl + "\t" + this.authority;
        }

        if (isNormalRecord)
            records.add(record);
        else
            additionalRecords.add(record);

        return responseData; //Return the byte stream pointer
    }

    private String readNonIpData(DataInputStream responseData, int dataL, byte[] response) throws Exception {
        //Should just be a string to read
        String data = "";
        String compressedNm = "";
        byte[] dataBytes = new byte[dataL];

        //Read all of the data into a special array
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = responseData.readByte();
        }

        //Then turn these bytes into the data string that we need
        for (int j = 0; j < dataBytes.length; j++) {
            byte currentByte = dataBytes[j];
            if (currentByte == (byte) (-64)) {
                //Skip this and the next byte and sub in the compression label
                int labelOffset = (int) dataBytes[j + 1];
                String compLabel = getCompressionLabel2(labelOffset, response);
                j++;
                data = data + "." + compLabel;
            } else {
                if ((int) currentByte == 4) {
                    data = data + ".";
                } else {
                    data = data + (char) currentByte;
                }
            }
        }

        return data;
    }

    private String getCompressionLabel2(int offset, byte[] response) {
        String strLable = "";
        boolean first = true;
        //Then read the label, not that we know how long it is
        while (response[offset] != (byte) 0) {
            byte labelLengthByte = response[offset];
            if (labelLengthByte > 0) {
                byte[] label = new byte[labelLengthByte];
                //Copy all the label bytes into a new array
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
            } else {
                return strLable + this.compressionLabel; //error case
            }
        }
        this.compressionLabel = strLable;//handy
        return strLable;
    }


    private String readIpData(DataInputStream responseData, byte[] response, int dataL) throws Exception {
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
                //Then the next byte is a compression byte
                int labelOffset = (int) responseData.readByte();
                recordName = recordName + getCompressionLabel2(labelOffset, response);
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
        this.compressionLabel = strLable;
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
