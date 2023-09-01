package common.http;

import java.io.IOException;
import java.net.Socket;

public abstract class HTTPClient {
    private final String request_location = "";
    private final String payload_file = "";

    protected abstract String buildRequest(String request_location, String payload_file) throws IOException;

    public void run(Socket socket) {
        try (HTTPConnection conn = new HTTPConnection(socket)) {
            String request = buildRequest(this.request_location, this.payload_file);
            conn.sendRequest(request);
            String response = conn.readResponse();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
