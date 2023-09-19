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

    public void sendHTTPRequest(String request_location, String... payload_file) {
        try (HTTPConnection conn = new HTTPConnection(socket)) {

            String[] filesToSend = payload_file.length == 0 ? new String[]{null} : payload_file;

                for (String file : filesToSend) {
                    if (file == null) {
                        System.out.println("Sending a request without payload");
                    } else {
                        System.out.println("Sending payload file: " + file);
                    }
                    
                    String httpRequest = buildRequest(request_location, file);
                    conn.sendMessage(httpRequest);
                    
                    HTTPResponse httpResponse = null;
                    while (httpResponse == null) {
                        httpResponse = (HTTPResponse) conn.readMessage();
                    }

                    System.out.println("Response from server: " + httpResponse.toString());
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() throws IOException {
        socket.close();
    }
}
