package com.github.wenqiglantz.service.customerservice;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addRequiredOption("p", "port", true, "The port this app will listen on");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int port = Integer.parseInt(cmd.getOptionValue("port"));

        SpringApplication app = new SpringApplication(CustomerServiceApplication.class);
        app.run(String.format("--server.port=%d", port));
    }
}
