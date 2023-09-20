package au.edu.adelaide.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static au.edu.adelaide.client.GETClientConstants.*;

import common.http.HTTPClient;
import common.http.messages.HTTPResponse;
import common.util.JSONObject;
import common.util.CLI;
import common.util.IOUtility;

public class GETClient extends HTTPClient {
    public GETClient(Socket socket) throws IOException {
        super(socket);
    }

    private static final String request_location = "client/resources/GETRequest.txt";

    @Override
    protected String buildRequest(String request_location, String... payload_file) throws IOException {
        String request = IOUtility.readTxtFile(request_location);

        if (payload_file[0] == null) return request;
        String URI = payload_file[0];
        return request.replace("{{URI}}", URI);
    }

    public static void main(String[] args) {
        CLI cli = CLI.initialiseCLI(CLIENT_HELP_FILE);
        Map<String, String> argMap = cli.parseCLIArguments(args);
        if (argMap == null) return;

        String host = argMap.getOrDefault("host", DEFAULT_HOST);
        int port = Integer.parseInt(argMap.getOrDefault("port", DEFAULT_PORT));
        String request_location = argMap.getOrDefault("request", GET_REQUEST_FILE);
        String URI = argMap.getOrDefault("URI", "recent");

        try (Socket socket = new Socket(host, port);
            GETClient client = new GETClient(socket);) {
                client.sendHTTPRequest(request_location, URI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
