package content_server;

import common.HTTPConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ContentServer {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4567;
    
    private static final String PUTRequest = "content_server/resources/PUTRequest.txt";
    private static final String payload = "content_server/resources/WeatherData.txt";

    public static void main(String[] args) {
        try (HTTPConnection conn = new HTTPConnection(HOST, PORT);) {
            // Build PUT request
            String request = new String(Files.readAllBytes(Paths.get(PUTRequest)));
            request = request.replace("{{payload}}", new String(Files.readAllBytes(Paths.get(payload))));
            conn.sendRequest(request);

            // Read response
            String response = conn.readResponse();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
