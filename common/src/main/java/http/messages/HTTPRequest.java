package http.messages;

import java.util.Map;
import java.util.HashMap;

public class HTTPRequest extends HTTPMessage {
    private Map<String, String> requestLine = new HashMap<>();

    public HTTPRequest() {
        super();
    }

    public HTTPRequest(HTTPMessage httpMessage, String requestMethod, String requestURI, String protocolVersion) {
        this.requestLine.put("requestMethod", requestMethod);
        this.requestLine.put("requestURI", requestURI);
        this.protocolVersion = protocolVersion;
    }

    public String getRequestMethod() {
        return requestLine.get("requestMethod");
    }

    public String getURI() {
        return requestLine.get("requestURI");
    }
    
    public String getRequestLine() {
        return requestLine.get("requestMethod") + " " + requestLine.get("requestURI") + " " + protocolVersion;
    }

    public String toString() {
        return getRequestLine() + "\r\n" + headersToString() + "\r\n\r\n" + getBody();
    }

    public void setRequestMethod(String requestMethod) {
        this.requestLine.put("requestMethod", requestMethod);
    }

    public void setURI(String requestURI) {
        this.requestLine.put("requestURI", requestURI);
    }

    public void setRequestLine(String requestMethod, String requestURI, String protocolVersion) {
        this.requestLine.put("requestMethod", requestMethod);
        this.requestLine.put("requestURI", requestURI);
        this.protocolVersion = protocolVersion;
    }
}