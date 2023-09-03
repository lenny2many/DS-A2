package content_server;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.http.HTTPClient;


public class ContentServer extends HTTPClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4567;

    public ContentServer(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    protected String buildRequest(String request_location, String... payload_file) throws IOException {
        // Build PUT request
        System.out.println(request_location);
        String request = new String(Files.readAllBytes(Paths.get(request_location)));
        String payload = new String(Files.readAllBytes(Paths.get(payload_file[0])));
        request = request.replace("{{payload_length}}", Integer.toString(payload.length()));
        request = request.replace("{{payload}}", payload);
        return request;
    }

    public static void main(String[] args) {
        try (ContentServer contentServer = new ContentServer(new Socket(HOST, PORT));) {
            String location = "content_server/resources/";
            contentServer.sendHTTPRequest(location+"PUTRequest.txt", location+"WeatherData.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
