package client;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.http.HTTPClient;


public class GETClient extends HTTPClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "4567";

    public GETClient(Socket socket) throws IOException {
        super(socket);
    }

    private static final String request_location = "client/resources/GETRequest.txt";

    @Override
    protected String buildRequest(String request_location, String... payload_file) throws IOException {
        // Build GET request
        String request = new String(Files.readAllBytes(Paths.get(request_location)));
        return request;
    }

    public static String[] CLI(String[] args) {
        String protocol;
        String host;
        String port;

        if (args.length == 0) {
            System.out.println("No arguments provided\n");
            displayHelp();
            return null;
        }

        if (args.length > 0) {
            String[] parts = args[0].split(":");

            if (args[0].equals("--help") || args[0].equals("-h")) {
                displayHelp();
                return null;
            } else if (args[0].equals("--default") || args[0].equals("-d")) {
                protocol = "http";
                host = DEFAULT_HOST;
                port = DEFAULT_PORT;
            } else

            if (parts.length == 2) {
                if (!isNumeric(parts[1])) {
                    System.out.println("Invalid port");
                    displayHelp();
                    return null;
                }
                protocol = "http";
                host = parts[0];
                port = parts[1];
            } else if (parts.length == 3) {
                if (!isNumeric(parts[2])) {
                    System.out.println("Invalid port\n");
                    displayHelp();
                    return null;
                }
                protocol = parts[0];
                host = parts[1].replace("//", "");
                port = parts[2];
            } else {
                System.out.println("Invalid server address\n");
                displayHelp();
                return null;
            }
        } else {
            protocol = "http";
            host = DEFAULT_HOST;
            port = DEFAULT_PORT;
        }

        return new String[] {protocol, host, port};
    }

    public static void displayHelp() {
        System.out.println("Usage: java [DIRECTORY].GETClient [OPTIONS]\n");
        System.out.println("Options:");
        System.out.println("  http://[SERVERNAME]:[PORT]    Connect to the specified server and port");
        System.out.println("  --default, -d                 Connect to the default server and port (localhost:4567)");
        System.out.println("  --help, -h                    Display this help message and exit\n");
        System.out.println("Examples:");
        System.out.println("  java GETClient http://example:4567");
        System.out.println("  java GETClient example:4567\n");
        System.out.println("Defaults:");
        System.out.println(String.format("  SERVERNAME: %s", DEFAULT_HOST));
        System.out.println(String.format("  PORT: %s", DEFAULT_PORT));
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static void main(String[] args) {
        String[] cli_args = CLI(args);
        if (cli_args == null) {
            return;
        }

        try (GETClient client = new GETClient(new Socket(cli_args[1], Integer.parseInt(cli_args[2])));) {
            client.sendHTTPRequest(request_location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
