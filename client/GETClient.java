package client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.http.HTTPClient;


public class GETClient extends HTTPClient {

    private static final String request_location = "client/resources/GETRequest.txt";

    @Override
    protected String buildRequest(String request_location, String payload_file) throws IOException {
        // Build GET request
        String request = new String(Files.readAllBytes(Paths.get(request_location)));
        return request;
    }

    public static void main(String[] args) {
        new GETClient().run();
    }
}
