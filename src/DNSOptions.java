/**
 * TelecomLab2
 * Created by eknox on 2016-09-29.
 */
public class DNSOptions {
    public int timeout;
    public int maxRetries;
    public int port;
    public String queryType;
    public String server;
    public String name;


    public DNSOptions(){
        this.timeout = 5;
        this.maxRetries = 3;
        this.port = 53;
        this.queryType = "ip";
    }
}
