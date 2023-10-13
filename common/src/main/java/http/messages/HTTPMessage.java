package http.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an HTTP message and provides functionality 
 * to determine the type of HTTP message and manipulate headers.
 */
public class HTTPMessage {
    protected Map<String, String> headers;
    protected String body;
    protected String protocolVersion = "HTTP/1.1";

    private static final Logger LOGGER = Logger.getLogger(HTTPMessage.class.getName());

    /**
     * Initializes a new HTTPMessage instance.
     */
    public HTTPMessage() {
        this.headers = new HashMap<>();
    }

    /**
     * Determines and initializes an HTTP message type (request/response) based on the provided first line.
     *
     * @param firstLine The first line of an HTTP message.
     * @return An instance of HTTPRequest or HTTPResponse based on the analysis of the first line.
     * @throws IllegalArgumentException if the first line is invalid or does not adhere to HTTP specifications.
     */
    public HTTPMessage determineMessageType(String firstLine) {
        String[] parts = firstLine.split(" ");
        
        // Validate the basic structure of the first line
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid first line");
        }
    
        boolean isRequest = isRequestMessage(parts);
        boolean isResponse = isResponseMessage(parts);
    
        if (isRequest) {
            return new HTTPRequest(this, parts[0], parts[1], parts[2]);
        } else if (isResponse) {
            return new HTTPResponse(this, parts[1] + " " + parts[2], parts[0]);
        } else {
            LOGGER.log(Level.WARNING, "Invalid first line: " + firstLine);
            throw new IllegalArgumentException("Invalid first line");
        }

    }
    
    private boolean isRequestMessage(String[] parts) {
        boolean isValidMethod = parts[0].matches("GET|PUT|POST|DELETE");
        boolean isValidUri = parts[1].startsWith("/");
        boolean isValidProtocol = parts[2].matches("HTTP/1.[01]");
        
        return isValidMethod && isValidUri && isValidProtocol;
    }
    
    private boolean isResponseMessage(String[] parts) {
        boolean isValidProtocol = parts[0].matches("HTTP/1.[01]");
        boolean isValidStatusCode = isValidStatusCode(parts[1]);
    
        return isValidProtocol && isValidStatusCode;
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

    /**
     * Sets a header field with the provided name and value.
     *
     * @param headerName  The name of the header.
     * @param headerValue The value of the header.
     * @throws IllegalArgumentException If the header name/value pair is invalid.
     */
    public void setHeader(String headerName, String headerValue) {
        switch ((headerName != null) ? headerName : "") {
            case "Connection":
                validateAndSetConnectionHeader(headerValue);
                break;
            case "Content-Type":
                validateAndSetContentTypeHeader(headerValue);
                break;
            case "Content-Length": 
                validateAndSetContentLengthHeader(headerValue);
                break;
            case "Server":
                headers.put("Server", headerValue);
                break;
            case "User-Agent":
                headers.put("User-Agent", headerValue);
                break;
            case "Timestamp":
                headers.put("Timestamp", headerValue);
                break;
            default:
                LOGGER.log(Level.WARNING, "Header name unsupported: {0}", headerName);
                break;
        }
    }

    private void validateAndSetConnectionHeader(String headerValue) {
        if (headerValue.equals("keep-alive") || headerValue.equals("close")) {
            headers.put("Connection", headerValue);
        } else {
            LOGGER.log(Level.WARNING, "Invalid Connection header value: {0}", headerValue);
            throw new IllegalArgumentException("Invalid Connection header value");
        }
    }

    private void validateAndSetContentTypeHeader(String headerValue) {
        if (headerValue.equals("application/json")) {
            headers.put("Content-Type", headerValue);
        } else {
            LOGGER.log(Level.WARNING, "Invalid Content-Type header value: {0}", headerValue);
            throw new IllegalArgumentException("Invalid Content-Type header value");
        }
    }

    private void validateAndSetContentLengthHeader(String headerValue) {
        try {
            int contentLength = Integer.parseInt(headerValue);
            if (contentLength < 0) {
                throw new IllegalArgumentException("Content-Length header value cannot be negative");
            }
            headers.put("Content-Length", headerValue);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid Content-Length header value: {0}", headerValue);
            throw e;
        }
    }

    /**
     * Determines whether the connection should be kept alive based on the headers and protocol version.
     *
     * @return true if the connection should be kept alive, otherwise false.
     */
    public boolean shouldKeepConnectionAlive() {
        // if HTTP/1.0 and no Connection header, default to close
        if (this.protocolVersion.equals("HTTP/1.0")) {
            return headers.getOrDefault("Connection", "close").equals("keep-alive");
        }
        // if HTTP/1.1 and no Connection header, default to keep-alive (persistent connection)
        else if (this.protocolVersion.equals("HTTP/1.1")) {
            return headers.getOrDefault("Connection", "keep-alive").equals("keep-alive");
        } else {
            LOGGER.log(Level.WARNING, "Unsupported protocol version: {0}", this.protocolVersion);
            throw new IllegalArgumentException("Unsupported protocol version");
        }
    }

    /**
     * Retrieves the value of a header field given its name.
     *
     * @param headerName The name of the header.
     * @return The value of the header, or null if it doesn't exist.
     */
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String headersToString() {
        if (headers.isEmpty()) {
            return "";
        } else {
            return headers.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Sets the body of the HTTP message.
     *
     * @param body The body content as a string.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Retrieves the body of the HTTP message.
     *
     * @return The body content as a string.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Sets the protocol version of the HTTP message.
     *
     * @param protocolVersion The protocol version as a string.
     */
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Retrieves the protocol version of the HTTP message.
     *
     * @return The protocol version as a string.
     */
    public String getProtocolVersion() {
        return this.protocolVersion;
    }
}
