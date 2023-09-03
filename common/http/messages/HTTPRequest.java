package common.http.messages;

public class HTTPRequest extends HTTPMessage {

    public HTTPRequest(String message) {
        super(message);
    }

    public HTTPRequest(HTTPMessage httpMessage) {
        super(httpMessage.toString());
    }

    public String getRequestMethod() {
        String[] lines = getHeader().split("\r\n");
        String[] requestLine = lines[0].split(" ");
        return requestLine[0];
    }
}