package common.http.messages;

import java.util.Map;
import java.util.HashMap;

public class HTTPRequest extends HTTPMessage {
    private Map<String, String> requestLine = new HashMap<>();

    public HTTPRequest(HTTPMessage httpMessage, String requestMethod, String requestURI) {
        this.requestLine.put("requestMethod", requestMethod);
        this.requestLine.put("requestURI", requestURI);
    }

    public String getRequestMethod() {
        return requestLine.get("requestMethod");
    }

    public String getRequestURI() {
        return requestLine.get("requestURI");
    }
    
    public String getRequestLine() {
        return requestLine.get("requestMethod") + " " + requestLine.get("requestURI") + " " + protocolVersion;
    }
}