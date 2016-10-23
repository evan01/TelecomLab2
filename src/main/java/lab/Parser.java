package lab; /**
 * TelecomLab2
 * Created by eknox on 2016-09-29.
 */

import org.apache.commons.cli.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parser {

    public static void main(String[] args) {
        DNSOptions a = parseWithOptions(args, getOptions());
    }

    /**
     * This function will take as input the cL args and return the DNS Options to pass into the program
     *
     * @param args
     */
    public static DNSOptions parse(String[] args) {

        return parseWithOptions(args, getOptions());
    }

    /**
     * Will generate a set of user input options for the CLI libraries to use
     *
     * @return set of Options
     */
    private static Options getOptions() {
        Options options = new Options();

        Option timeoutOption = new Option("t", "timeout", true, "(Optional) Gives how long to wait, in seconds, before retransmitting an unanswered query");
        timeoutOption.setRequired(false);
        options.addOption(timeoutOption);

        Option maxRetryOption = new Option("r", "max-retries", true, "(Optional) is the maximum number of times to retransmit an unanswered query before giving up.");
        maxRetryOption.setRequired(false);
        options.addOption(maxRetryOption);

        Option portOption = new Option("p", "port", true, "(Optional) is the UDP port number of the DNS server. Default val: 53");
        portOption.setRequired(false);
        options.addOption(portOption);

        Option serverOptionMX = new Option("mx", "mailServerQuery", false, "(Optional) Whether to send a Mail Server Query");
        serverOptionMX.setRequired(false);
        options.addOption(serverOptionMX);

        Option serverOptionNS = new Option("ns", "nameServerQuery", false, "(Optional) Whether to send a Name Server Query");
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
     *
     * @param ip The string input
     * @return The concatenated 123.123.123.123 ip address without the @ symbol
     * @throws ParseException In the event that the user entered the wrong input
     */
    public static byte[] validateAndGetIP(String ip) throws ParseException {
        //User Regex to make sure we are getting a valid ip address
        String IPpattern = "@(\\d)+.(\\d)+.(\\d)+.(\\d)+\\Z";
        //Create a pattern object
        Pattern r = Pattern.compile(IPpattern);
        // Now create matcher object.
        Matcher m = r.matcher(ip);

        //Create the byte array
        byte[] ipAddr = new byte[4];
        //Search for the regex!
        //m.group();
        int[] ipAddrInt = new int[4];
        int addrPtr = 0;
        int num = 0;
        if (m.find()) {
            //Then parse the ip passed in
            for (int i = 0; i < ip.length(); i++) {
                if (i == 0 && ip.charAt(0) != '@') {
                    throw new ParseException("Make sure that you have the @ symbol first before address");
                }
                if (i == 0)
                    continue;

                if (ip.charAt(i) != '.') {
                    num = Character.getNumericValue(ip.charAt(i)) + 10 * num;
                    if (i == ip.length() - 1) {
                        ipAddrInt[addrPtr] = num;
                        break;
                    }
                } else {
                    ipAddrInt[addrPtr] = num;
                    addrPtr++;
                    num = 0;
                }
            }

            for (int j = 0; j < 4; j++) {
                ipAddr[j] = (byte) ipAddrInt[j];
            }

            return ipAddr;
        } else {
            throw new ParseException("IP Address is invalid");
        }
    }

    /**
     * This function takes as input the CL Args and the possible options and returns what the user entered
     *
     * @param args    The command line arguments
     * @param options The Apache Cli options
     * @return lab.DNSOptions object that holds all the data nescessary for the DNS request
     */
    private static DNSOptions parseWithOptions(String[] args, Options options) {
        //Create some of the standard cli classes that help with parsing
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        if (args.length < 1) {
            System.out.println("Error, not enough args passed");
            System.exit(1);
        }

        //Create the DNSSettings data object
        DNSOptions dnsOpts = new DNSOptions();

        //Start parsing the arguments given by the user
        try {
            cmd = parser.parse(options, args);

            //First do the required arguments
            dnsOpts.query = args[args.length - 1];
            String stringRepServer = args[args.length - 2];
            dnsOpts.stringServer = stringRepServer;

            //Validate that the server is in the right format, throws exception if not valid, also get rid of @character
            dnsOpts.server = validateAndGetIP(stringRepServer);

            //Next handle the options
            if (cmd.hasOption("t"))
                dnsOpts.timeout = Integer.parseInt(cmd.getOptionValue('t'));

            if (cmd.hasOption("r"))
                dnsOpts.maxRetries = Integer.parseInt(cmd.getOptionValue("r"));

            if (cmd.hasOption("p"))
                dnsOpts.port = Integer.parseInt(cmd.getOptionValue("p"));

            if (cmd.hasOption("mailServerQuery"))
                dnsOpts.queryType = "MX";

            if (cmd.hasOption("nameServerQuery"))
                dnsOpts.queryType = "NS";

            if (cmd.hasOption("mailServerQuery") && cmd.hasOption("nameServerQuery"))
                throw new ParseException("Specify either mx or ns query type,(or none) default is IP");

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println();
            formatter.printHelp(" {args} @[DNS_IP] [question]", options);
            System.exit(1);
        } catch (Exception e) {
            System.out.println("ERROR Incorrect input syntax: " + e.getMessage());
            e.printStackTrace();
            formatter.printHelp(" {args} @[DNS_IP] [question]", options);
            System.exit(1);
        }

        return dnsOpts;
    }
}
