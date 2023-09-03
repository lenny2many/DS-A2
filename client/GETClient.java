package client;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.http.HTTPClient;


public class GETClient extends HTTPClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4567;

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

    public static void main(String[] args) {
        try (GETClient client = new GETClient(new Socket(HOST, PORT))) {
            client.sendHTTPRequest(request_location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
