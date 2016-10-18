package lab; /**
 * TelecomLab2
 * Created by eknox on 2016-10-06.
 */
import lab.Parser;
import lab.DNSOptions;
import lab.DnsClient;
import org.junit.Test;
import org.junit.BeforeClass;

public class DnsClientTest {
    @BeforeClass
    public static void setUp(){

        System.out.println("setting up the test");
    }

    @Test
    public void createSocket() throws Exception {
//        System.out.println("setting up the test");

    }

    @Test
    public void testGoogleDNS(){
        DNSOptions opts = new DNSOptions();
//        opts.name = "www.mcgill.ca";

        try {
            opts.server = Parser.validateAndGetIP("@8.8.8.8");
//            DnsClient.sendRequestUsingUDP(opts);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}