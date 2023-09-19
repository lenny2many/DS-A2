package common.http.messages;

import java.util.Map;

public class HTTPResponse extends HTTPMessage {
    private String statusCode;

    public HTTPResponse(HTTPMessage httpMessage, String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 " + statusCode + "\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        sb.append("\r\n");
        sb.append(getBody());
        return sb.toString();
    }
}