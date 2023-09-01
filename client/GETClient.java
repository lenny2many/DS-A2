package client;

import common.HTTPConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GETClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4567;

    private static final String GETRequest = "client/resources/GETRequest.txt";


    public static void main(String[] args) {
        try (HTTPConnection conn = new HTTPConnection(HOST, PORT);) {
            // Build GET request
            String request = new String(Files.readAllBytes(Paths.get(GETRequest)));
            conn.sendRequest(request);

            // Read response
            String response = conn.readResponse();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
