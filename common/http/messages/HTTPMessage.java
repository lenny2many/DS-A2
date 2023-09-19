package common.http.messages;

import java.util.Map;
import java.util.HashMap;

public class HTTPMessage {
    protected Map<String, String> headers;
    protected String body;
    protected String protocolVersion = "HTTP/1.1";

    public HTTPMessage() {
        this.headers = new HashMap<>();
    }

    public HTTPMessage determineMessageType(String firstLine) {
        String[] parts = firstLine.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid first line");
        }
        
        // Initialise requestline variables
        String requestMethod = "";
        String requestURI = "";

        // Initialise statusline variables
        String statusCode = "";

        // Analyse each part of the first line to determine if it's a request or response
        boolean isValidRequest = false;
        boolean isValidResponse = false;

        // Check first part
        if (parts[0].equals("GET") ||
            parts[0].equals("PUT") ||
            parts[0].equals("POST") ||
            parts[0].equals("DELETE")) {
            isValidRequest = true;
            requestMethod = parts[0];
        } else if (parts[0].equals("HTTP/1.1") || parts[0].equals("HTTP/1.0")) {
            isValidResponse = true;
            protocolVersion = parts[0];
        } else {
            throw new IllegalArgumentException("Invalid first line");
        }

        // Check second part
        if (parts[1].startsWith("/") && isValidRequest) {
            isValidRequest = true;
            requestURI = parts[1];
        } else if (isValidStatusCode(parts[1]) && isValidResponse) {
            isValidResponse = true;
            statusCode = parts[1] + " " + parts[2]; // If status code is valid, assume rest of line is valid
        } else {
            throw new IllegalArgumentException("Invalid first line");
        }

        // Check third part
        if (parts[2].equals("HTTP/1.1") || parts[2].equals("HTTP/1.0") && isValidRequest) {
            isValidRequest = true;
            protocolVersion = parts[2];
        } else if (isValidResponse) {
            // do nothing
        } else {
            throw new IllegalArgumentException("Invalid first line");
        }

        if (isValidRequest) {
            firstLine = firstLine;
            return new HTTPRequest(this, requestMethod, requestURI);
        } else if (isValidResponse) {
            firstLine = firstLine;
            return new HTTPResponse(this, statusCode);
        } else {
            throw new IllegalArgumentException("Invalid first line");
        }
    }

    private boolean isValidStatusCode(String statusCode) {
        // Check if status code is a valid integer
        Integer statusCodeInt;
        try {
            statusCodeInt = Integer.parseInt(statusCode);
        } catch (NumberFormatException e) {
            return false;
        }
        return statusCodeInt >= 100 && statusCodeInt <= 599;
    }

    public void setHeader(String headerName, String headerValue) {
        // Handle invalid header values
        if (headerName.equals("Connection")) {
            if (headerValue.equals("keep-alive") || headerValue.equals("close")) {
                headers.put(headerName, headerValue);
            } else {
                throw new IllegalArgumentException("Invalid Connection header value");
            }
        } else if (headerName.equals("Content-Type")) {
            if (headerValue.equals("application/json")) {
                headers.put(headerName, headerValue);
            } else {
                throw new IllegalArgumentException("Invalid Content-Type header value");
            }
        } else if (headerName.equals("Content-Length")) {
            try {
                int contentLength = Integer.parseInt(headerValue);
                headers.put(headerName, headerValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid Content-Length header value");
            }
        } else {
            headers.put(headerName, headerValue);
        }
        
        headers.put(headerName, headerValue);
    }

    public boolean shouldKeepConnectionAlive() {
        // if HTTP/1.0 and no Connection header, default to close
        if (this.protocolVersion.equals("HTTP/1.0")) {
            return headers.getOrDefault("Connection", "close").equals("keep-alive");
        }
        // if HTTP/1.1 and no Connection header, default to keep-alive (persistent connection)
        else if (this.protocolVersion.equals("HTTP/1.1")) {
            return headers.getOrDefault("Connection", "keep-alive").equals("keep-alive");
        }

        return false;
    }

    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }
}
