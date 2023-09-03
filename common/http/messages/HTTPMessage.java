package common.http.messages;

public class HTTPMessage {
    private String header;
    private String body;

    public HTTPMessage(String message) {
        String[] parts = message.split("\r\n\r\n");
        this.header = parts[0];
        if (parts.length > 1) {
            this.body = parts[1];
        }
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String toString() {
        return header + "\r\n\r\n" + body;
    }
}
