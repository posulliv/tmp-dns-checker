package com.starburstdata.dnschecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

public class DnsChecker implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(DnsChecker.class);

    @CommandLine.Parameters(
            index = "0",
            description = "Configuration file."
    )
    public String configFile;
    private String[] hostnames;
    private int checkInterval = 5;

    private DnsChecker() {}

    @Override
    public Integer call() throws Exception
    {
        parseConfigFile();
        logger.info("The beginning...");
        while (true) {
            checkHosts();
            sleep(checkInterval * 1000);
        }
    }

    public static CommandLine create()
    {
        return new CommandLine(new DnsChecker());
    }

    public static void main(String[] args)
    {
        System.exit(create().execute(args));
    }

    private void parseConfigFile() throws IOException
    {
        Properties props = new Properties();
        props.load(new FileInputStream(configFile));
        String interval = props.getProperty("check-interval");
        if (interval != null) {
            checkInterval = Integer.parseInt(interval);
        }
        String hosts = props.getProperty("hostnames");
        if (hosts != null) {
            hostnames = hosts.split(",");
        }
    }

    private void checkHosts()
    {
        for (String hostname : hostnames) {
            checkHost(hostname);
        }
    }

    private void checkHost(String hostname)
    {
        try {
            InetAddress host = InetAddress.getByName(hostname);
            logger.info("host reachable: {}", hostname);
        }
        catch (UnknownHostException e) {
            logger.error("host unreachable: {}", hostname, e);
        }
    }
}
