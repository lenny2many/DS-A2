package common.http;

import java.io.IOException;
import java.net.Socket;

public abstract class HTTPClient implements AutoCloseable {
    private final Socket socket;

    public HTTPClient(Socket socket) {
        this.socket = socket;
    }

    protected abstract String buildRequest(String request_location, String... payload_file) throws IOException;

    public void sendHTTPRequest(String request_location, String... payload_file) {
        try (HTTPConnection conn = new HTTPConnection(socket)) {
            String request = buildRequest(request_location, payload_file[0]);
            conn.sendData(request);
            String response = conn.readBuffer();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
