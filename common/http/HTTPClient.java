package common.http;

import java.io.IOException;
import java.net.Socket;

import common.http.messages.HTTPResponse;

public abstract class HTTPClient implements AutoCloseable {
    private final HTTPConnection httpConn;

    public HTTPClient(Socket socket) throws IOException {
        httpConn = new HTTPConnection(socket);
    }

    protected abstract String buildRequest(String request_location, String... payload_file) throws IOException;

    public void sendHTTPRequest(String request_location, String... payload_file) {
        if (request_location == null || request_location.isEmpty()) {
            throw new IllegalArgumentException("Request location cannot be null or empty");
        }
    
        // If multiple payloads are provided, take only the first one.
        String file = (payload_file != null && payload_file.length > 0) ? payload_file[0] : null;
    
        if (file != null) {
            System.out.println("Sending payload file: " + file + "\n");
        }
        
        String httpRequest = null;
        try {
            httpRequest = buildRequest(request_location, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        synchronized (httpConn) {
            try {
                httpConn.sendMessage(httpRequest);
        
                HTTPResponse httpResponse = null;
                while (httpResponse == null) {
                    httpResponse = (HTTPResponse) httpConn.readMessage();
                }
        
                System.out.println("Response from server:\n" + httpResponse.toString() + "\n");

                if (httpResponse.shouldKeepConnectionAlive()) {
                    System.out.println("Server is keeping connection alive\n");
                    System.out.println("----------------------------------------\n");
                } else {
                    System.out.println("Server is closing connection");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
    
    @Override
    public void close() throws IOException {
        httpConn.manuallyClose();
    }
}
