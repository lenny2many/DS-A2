package http.messages;

import java.util.Map;

public class HTTPResponse extends HTTPMessage {
    private String statusCode;

    public HTTPResponse() {
        super();
    }

    public HTTPResponse(HTTPMessage httpMessage, String statusCode, String protocolVersion) {
        this.statusCode = statusCode;
        this.protocolVersion = protocolVersion;
    }

    public String getStatusCode() {
        return statusCode.trim();
    }

    public String getResponseLine() {
        return protocolVersion + " " + statusCode;
    }

    public String toString() {
        return getProtocolVersion() + " " + getStatusCode() + "\r\n" + headersToString() + "\r\n\r\n" + getBody();
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public void setResponseLine(String statusCode, String protocolVersion) {
        this.statusCode = statusCode;
        this.protocolVersion = protocolVersion;
    }
}