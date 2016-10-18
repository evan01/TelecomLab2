package lab;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TelecomLab2
 * Created by eknox on 2016-10-18.
 */
public class DnsPacket2Test {

    @Test
    public void testDNS(){
        DNSOptions opts = new DNSOptions();
        opts.query = "www.mcgill.ca";
        opts.queryType = "A";

        DnsPacket2 p = new DnsPacket2(opts);
        byte[] packet = p.dnsData;

    }

}