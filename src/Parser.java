/**
 * TelecomLab2
 * Created by eknox on 2016-09-29.
 */
import org.apache.commons.cli.*;

import java.util.concurrent.ExecutionException;
import java.util.regex.*;
import java.util.List;

public class Parser {

    public static void main(String[] args){
        //First get all the possible inputs into the program
        Options options = getOptions();

        //Next parse the options and get all the values the user entered
        parse(args,options);
    }

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

        Option serverOptionMX = new Option("mx","Query type: Mail Server",false,"(Optional) Whether to send a Mail Server Query");
        serverOptionMX.setRequired(false);
        options.addOption(serverOptionMX);

        Option serverOptionNS = new Option("ns","Query type: Name Server",false,"(Optional) Whether to send a Name Server Query");
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

    public static void parse(String[] args,Options options){
        //Create some of the standard cli classes that help with parsing
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        //Start parsing the arguments given by the user
        try {
            cmd = parser.parse(options,args);

            //First do the required arguments
            String name = args[args.length-1];
            String server = args[args.length-2];

            //Validate that the server is in the right format, throws exception if not valid, also get rid of @character
            server = validateAndGetIP(server);

            //Next handle the options




        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("DNS SENDER PROGRAM", options);

            System.exit(1);
            return;
        }
    }
}
