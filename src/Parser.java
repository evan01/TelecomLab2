/**
 * TelecomLab2
 * Created by eknox on 2016-09-29.
 */
import org.apache.commons.cli.*;
import java.util.regex.*;

public class Parser {

    /**
     * This function will take as input the cL args and return the DNS Options to pass into the program
     * @param args
     */
    public static DNSOptions parse(String[] args){
        return parseWithOptions(args,getOptions());
    }

    /**
     * Will generate a set of user input options for the CLI libraries to use
     * @return set of Options
     */
    public static Options getOptions(){
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

        Option serverOptionMX = new Option("mx","mailServerQuery",false,"(Optional) Whether to send a Mail Server Query");
        serverOptionMX.setRequired(false);
        options.addOption(serverOptionMX);

        Option serverOptionNS = new Option("ns","nameServerQuery",false,"(Optional) Whether to send a Name Server Query");
        serverOptionNS.setRequired(false);
        options.addOption(serverOptionNS);

//        Option ServerAddress = new Option("server","(Required) is the ipv4 address of the DNS server in an a.b.c.d format");
//        ServerAddress.setRequired(true);
//        options.addOption(ServerAddress);
//
//        Option ServerName = new Option("name","(Required) is the domain name to query for");
//        ServerName.setRequired(true);
//        ServerName.setValueSeparator('@');
//        options.addOption(ServerName);

        return options;
    }

    /**
     * Takes in a string ip address of possible format @123.123.123.123, checks to see if valid or not
     * @param ip The string input
     * @return The concatenated 123.123.123.123 ip address without the @ symbol
     * @throws ParseException In the event that the user entered the wrong input
     */
    public static String validateAndGetIP(String ip) throws ParseException{
        //User Regex to make sure we are getting a valid ip address
        String IPpattern = "@\\d{3}.\\d{3}.\\d{3}.\\d{3}";
        //Create a pattern object
        Pattern r = Pattern.compile(IPpattern);
        // Now create matcher object.
        Matcher m = r.matcher(ip);
        if (m.find()){
            return m.group(0).substring(1);
        }else {
            throw new ParseException("IP Address is invalid");
        }
    }

    /**
     * This function takes as input the CL Args and the possible options and returns what the user entered
     * @param args The command line arguments
     * @param options The Apache Cli options
     * @return DNSOptions object that holds all the data nescessary for the DNS request
     */
    public static DNSOptions parseWithOptions(String[] args,Options options){
        //Create some of the standard cli classes that help with parsing
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        //Create the DNSSettings data object
        DNSOptions dnsOpts = new DNSOptions();

        //Start parsing the arguments given by the user
        try {
            String optionVAL = new String();
            cmd = parser.parse(options,args);

            //First do the required arguments
            dnsOpts.name = args[args.length-1];
            dnsOpts.server = args[args.length-2];

            //Validate that the server is in the right format, throws exception if not valid, also get rid of @character
            dnsOpts.server = validateAndGetIP(dnsOpts.server);

            //Next handle the options
            if (cmd.hasOption("t"))
                dnsOpts.timeout = Integer.parseInt(cmd.getOptionValue('t'));

            if (cmd.hasOption("r"))
                dnsOpts.maxRetries = Integer.parseInt(cmd.getOptionValue("r"));

            if (cmd.hasOption("p"))
                dnsOpts.port = Integer.parseInt(cmd.getOptionValue("p"));

            if (cmd.hasOption("mailServerQuery"))
                dnsOpts.queryType = "mx";

            if (cmd.hasOption("nameServerQuery"))
                dnsOpts.queryType = "ns";

            if (cmd.hasOption("mailServerQuery") && cmd.hasOption("nameServerQuery"))
                throw new ParseException("Specify either mx or ns query type, default is IP");

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("DNS SENDER PROGRAM", options);

            System.exit(1);
        }

        return dnsOpts;
    }
}
