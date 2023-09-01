package common.http;

import java.io.IOException;

public abstract class HTTPClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4567;

    private final String request_location = "";
    private final String payload_file = "";

    protected abstract String buildRequest(String request_location, String payload_file) throws IOException;

    public void run() {
        try (HTTPConnection conn = new HTTPConnection(HOST, PORT)) {
            String request = buildRequest(this.request_location, this.payload_file);
            conn.sendRequest(request);
            String response = conn.readResponse();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
