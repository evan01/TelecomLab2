/**
 * TelecomLab2
 * Created by eknox on 2016-09-29.
 */
import org.apache.commons.cli.*;

import java.util.List;

public class Parser {

    public static void main(String[] args){
        parse(args);

    }
    public static void parse(String[] args){
        Options options = new Options();

        Option timeoutOption = new Option("t", "timeout", true, "(Optional) Gives how long to wait, in seconds, before retransmitting an unanswered query");
        timeoutOption.setRequired(false);
        options.addOption(timeoutOption);

        Option maxRetryOption = new Option("r", "max-retries", true, "(Optional) is the maximum number of times to retransmit an unanswered query before giving up.");
        maxRetryOption.setRequired(false);
        options.addOption(maxRetryOption);

        Option portOption = new Option("p","port",true,"(Optional) is the UDP port number of the DNS server. Default val: 53");
        portOption.setRequired(false);
        options.addOption(portOption);

        Option serverOptionMX = new Option("mx","Query type: Mail Server",false,"(Optional) Whether to send a Mail Server Query");
        serverOptionMX.setRequired(false);
        options.addOption(serverOptionMX);

        Option serverOptionNS = new Option("ns","Query type: Name Server",false,"(Optional) Whether to send a Name Server Query");
        serverOptionNS.setRequired(false);
        options.addOption(serverOptionNS);

        Option ServerAddress = new Option("server","(Required) is the ipv4 address of the DNS server in an a.b.c.d format");
        ServerAddress.setRequired(true);
        options.addOption(ServerAddress);

        Option Name = new Option("name","(Required) is the domain name to query for");
        Name.setRequired(true);
        options.addOption(Name);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if(cmd.hasOption("t")) {
                // print the date and time
                System.out.println("Has t!!");
            }
            System.out.println("Done");

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("DNS SENDER PROGRAM", options);

            System.exit(1);
            return;
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");

        System.out.println(inputFilePath);
        System.out.println(outputFilePath);
    }
}
