package content_server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.http.HTTPClient;


public class ContentServer extends HTTPClient {    
    private final String request_location = "content_server/resources/PUTRequest.txt";
    private final String payload_file = "content_server/resources/WeatherData.txt";

    @Override
    protected String buildRequest(String request_location, String payload_file) throws IOException {
        // Build PUT request
        String request = new String(Files.readAllBytes(Paths.get(request_location)));
        request = request.replace("{{payload}}", new String(Files.readAllBytes(Paths.get(payload_file))));
        return request;
    }

    public static void main(String[] args) {
        new ContentServer().run();
    }
}
