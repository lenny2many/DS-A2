package common.http;

import java.io.IOException;
import java.net.Socket;

import common.http.messages.HTTPResponse;

public abstract class HTTPClient implements AutoCloseable {
    private final Socket socket;

    public HTTPClient(Socket socket) {
        this.socket = socket;
    }

    protected abstract String buildRequest(String request_location, String... payload_file) throws IOException;

    public HTTPResponse sendHTTPRequest(String request_location, String... payload_file) {
        try (HTTPConnection conn = new HTTPConnection(socket)) {
            String request;
            if (payload_file.length > 0) {
                request = buildRequest(request_location, payload_file[0]);
            } else {
                request = buildRequest(request_location);
            }
            conn.sendData(request);
            HTTPResponse response = new HTTPResponse(conn.readBuffer());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
