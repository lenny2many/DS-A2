package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.Socket;
import http.messages.HTTPResponse;

public abstract class HTTPClient implements AutoCloseable {
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 2000;
    private final HTTPConnection httpConn;
    private String lastRequest;
    private boolean requestSuccess = false;
    private boolean connectionClosed = false;

    public HTTPClient(Socket socket, Writer out, BufferedReader in) throws IOException {
        httpConn = new HTTPConnection(socket, out, in);
    }

    protected abstract String buildRequest(String request_location, String... payload_file) throws IOException;

    public void sendHTTPRequest(String request_location, String... payload_file) {
        validateRequestLocation(request_location);

        String file = extractFirstPayloadFile(payload_file);
        lastRequest = buildHttpRequest(request_location, file);

        synchronized (httpConn) {
            sendRequestWithRetries(MAX_RETRIES);
        }
    }

    private void validateRequestLocation(String request_location) {
        if (request_location == null || request_location.isEmpty()) {
            throw new IllegalArgumentException("Request location cannot be null or empty");
        }
    }

    private String extractFirstPayloadFile(String... payload_file) {
        return (payload_file != null && payload_file.length > 0) ? payload_file[0] : null;
    }

    private String buildHttpRequest(String request_location, String file) {
        try {
            return buildRequest(request_location, file);
        } catch (Exception e) {
            throw new RuntimeException("Error building HTTP request", e);
        }
    }

    private void sendRequestWithRetries(int retriesLeft) {
        while (retriesLeft > 0 && !requestSuccess && !connectionClosed) {
            try {
                httpConn.sendMessage(lastRequest);
                System.out.println("Request sent to server:\n" + lastRequest + "\n");
                handleServerResponse();
            } catch (Exception e) {
                
            }

            if (!requestSuccess) {
                waitForRetry();
                retriesLeft--;
            }
        }

        if (!requestSuccess && !connectionClosed) {
            System.out.println("Failed to send request after " + MAX_RETRIES + " attempts.");
        } else if (connectionClosed) {
            System.out.println("Connection closed by server");
        }
    }

    private void waitForRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Sleep between retries interrupted", e);
        }
    }

    private void handleServerResponse() throws IOException {
        HTTPResponse httpResponse = retrieveHttpResponse();
        System.out.println("Response from server:\n" + httpResponse + "\n");

        if (httpResponse.getStatusCode().split(" ")[0].matches("2\\d\\d")) {
            requestSuccess = true;
            System.out.println("Request successful\n");
        }

        if (httpResponse.shouldKeepConnectionAlive() && !requestSuccess) {
            System.out.println("Server is keeping connection alive");
            System.out.println("Request failed. Attempting to resend\n");
        } else {
            System.out.println("Server is closing connection");
            connectionClosed = true;
        }
    }

    private HTTPResponse retrieveHttpResponse() throws IOException {
        HTTPResponse httpResponse = null;
        while (httpResponse == null) {
            httpResponse = (HTTPResponse) httpConn.readMessage();
        }
        return httpResponse;
    }

    @Override
    public void close() throws IOException {
        httpConn.manuallyClose();
    }
}
