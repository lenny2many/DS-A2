package content_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.util.UUID;

import common.http.HTTPClient;
import common.http.messages.HTTPResponse;


public class ContentServer extends HTTPClient {
    private static final String DEFAULT_PROTOCOL = "http";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "4567";
    private static final String DEFAULT_FILE = "resources/WeatherData.txt";

    private UUID uuid = UUID.randomUUID();

    public ContentServer(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    protected String buildRequest(String request_location, String... payload_file) throws IOException {
        // Read request template
        String request;
        try (InputStream is = getClass().getResourceAsStream(request_location);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            request = reader.lines().collect(Collectors.joining("\n"));
        }

        // Read payload
        String payload;
        try (InputStream is = getClass().getResourceAsStream(payload_file[0]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            payload = reader.lines().collect(Collectors.joining("\n"));
        }
        
        // Replace placeholders in request template
        request = request.replace("{{UUID}}", uuid.toString());
        request = request.replace("{{payload_length}}", Integer.toString(payload.length()));
        request = request.replace("{{payload}}", payload);
        return request;
    }

    public static String[] CLI(String[] args) {
        String protocol;
        String host;
        String port;
        String file;

        if (args.length == 0) {
            System.out.println("No arguments provided\n");
            displayHelp();
            return null;
        }

        if (args.length == 1) {
            if (args[0].equals("--help") || args[0].equals("-h")) {
                displayHelp();
                return null;
            } else if (args[0].equals("--default") || args[0].equals("-d")) {
                protocol = DEFAULT_PROTOCOL;
                host = DEFAULT_HOST;
                port = DEFAULT_PORT;
                file = DEFAULT_FILE;
            }
            else {
                System.out.println("Invalid arguments specified\n");
                displayHelp();
                return null;
            }
        } else if (args.length == 2) {
            String[] parts = args[0].split(":");
            
            // distamantle the server address and extract the protocol, host, and port
            if (parts.length == 2) {
                // [SERVERNAME]:[PORT]
                if (!isNumeric(parts[1])) {
                    System.out.println("Invalid port");
                    displayHelp();
                    return null;
                }
                protocol = "http";
                host = parts[0];
                port = parts[1];
            } else if (parts.length == 3) {
                // [PROTOCOL]://[SERVERNAME]:[PORT]
                if (!isNumeric(parts[2])) {
                    System.out.println("Invalid port\n");
                    displayHelp();
                    return null;
                }
                protocol = parts[0];
                host = parts[1].replace("//", "");
                port = parts[2];
            } else {
                // INVALID
                System.out.println("Invalid server address\n");
                displayHelp();
                return null;
            }

            //  check if valid file path
            if (!Files.exists(Paths.get(args[1]))) {
                System.out.println("Invalid file path\n");
                displayHelp();
                return null;
            } else {
                file = args[1];
            }
        } else {
            System.out.println("Invalid arguments specified\n");
            displayHelp();
            return null;
        }

        return new String[] {protocol, host, port, file};
    }

    public static void displayHelp() {
        System.out.println("Usage: java [DIRECTORY].ContentServer [OPTIONS]\n");
        System.out.println("Options:");
        System.out.println("  http://[SERVERNAME]:[PORT]    Connect to the specified server and port");
        System.out.println("  --default, -d                 Connect to the default server and port (localhost:4567) & use default file (resources/WeatherData.txt)");
        System.out.println("  --help, -h                    Display this help message and exit\n");
        System.out.println("Examples:");
        System.out.println("  java ContentServer http://example:4567 resources/WeatherData.txt");
        System.out.println("  java ContentServer example:4567 resources/WeatherData.txt\n");
        System.out.println("Defaults:");
        System.out.println(String.format("  SERVERNAME: %s", DEFAULT_HOST));
        System.out.println(String.format("  PORT: %s", DEFAULT_PORT));
        System.out.println(String.format("  FILE: %s", "resources/WeatherData.txt"));
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static void main(String[] args) {
        String[] cli_args = CLI(args);
        if (cli_args == null) {
            return;
        }

        try (ContentServer contentServer = new ContentServer(new Socket(cli_args[1], Integer.parseInt(cli_args[2])));) {
            HTTPResponse response = contentServer.sendHTTPRequest("resources/PUTRequest.txt", cli_args[3]);
            System.out.println("Response from server: " + response.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
