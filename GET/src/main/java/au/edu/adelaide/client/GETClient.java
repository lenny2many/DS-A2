package au.edu.adelaide.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.Writer;

import static au.edu.adelaide.client.GETClientConstants.*;

import http.HTTPClient;
import http.messages.HTTPResponse;
import util.JSONObject;
import util.CLI;
import util.IOUtility;

public class GETClient extends HTTPClient {
    private IOUtility ioUtility;

    public GETClient(Socket socket, Writer out, BufferedReader in) throws IOException {
        super(socket, out, in);
        ioUtility = new IOUtility();
    }

    private static final String request_location = "client/resources/GETRequest.txt";

    @Override
    protected String buildRequest(String request_location, String... payload_file) throws IOException {
        String request = ioUtility.readTxtFile(request_location);

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
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            GETClient client = new GETClient(socket, out, in)) {
                client.sendHTTPRequest(request_location, URI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
