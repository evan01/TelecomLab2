package lab; /**
 * TelecomLab2
 * Created by eknox on 2016-10-06.
 */

import org.junit.Assert;
import org.junit.Test;

public class DnsClientTest {

    @Test
    public void testFacebookDNS() {
        String args[] = {"-t", "3", "-r", "45", "-p", "53", "@8.8.8.8", "www.facebook.com"};
        DnsClient client = new DnsClient();
        Assert.assertEquals(DnsClient.sendDnsMessage(Parser.parse(args)), true);
    }

    @Test
    public void testGoogleDNS_ns() {
        String args[] = {"-t", "3", "-r", "45", "-p", "53", "-ns", "@8.8.8.8", "www.asdfaaaaasdfasdfasdfasdfsdafsdf.com"};
        DnsClient client = new DnsClient();
        Assert.assertEquals(DnsClient.sendDnsMessage(Parser.parse(args)), true);
    }

    @Test
    public void testAmazonDNS_ns() {
        String args[] = {"-t", "3", "-r", "45", "-p", "53", "-ns", "@8.8.8.8", "www.amazon.com"};
        DnsClient client = new DnsClient();
        Assert.assertEquals(DnsClient.sendDnsMessage(Parser.parse(args)), true);
    }

    @Test
    public void testAmazonDNS_mx() {
        String args[] = {"-t", "3", "-r", "45", "-p", "53", "-mx", "@8.8.8.8", "www.amazon.com"};
        DnsClient client = new DnsClient();
        Assert.assertEquals(DnsClient.sendDnsMessage(Parser.parse(args)), true);
    }

    @Test
    public void testGoogleDNS_mx() {
        String args[] = {"-t", "3", "-r", "45", "-p", "53", "-mx", "@8.8.8.8", "gmail.com"};
        DnsClient client = new DnsClient();
        Assert.assertEquals(DnsClient.sendDnsMessage(Parser.parse(args)), true);
    }

    @Test
    public void shouldFAILTEST() {
        String args[] = {"-t", "3", "-r", "45", "-p", "53", "-mx", "@8.8.8", "www.google.com"}; //Wrong server
        DnsClient client = new DnsClient();
        Assert.assertEquals(DnsClient.sendDnsMessage(Parser.parse(args)), false);
    }

    @Test
    public void shouldFAILTEST2() {
        String args[] = {"-t", "3", "-r", "45", "-p", "53", "-mx", "@8.8.8.8", "www.amazon.ca"}; //Wrong argument
        DnsClient client = new DnsClient();
        Assert.assertEquals(DnsClient.sendDnsMessage(Parser.parse(args)), false);
    }

}