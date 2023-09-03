package common.http.messages;

public class HTTPResponse extends HTTPMessage {

    public HTTPResponse(String message) {
        super(message);
    }

    public HTTPResponse(HTTPMessage httpMessage) {
        super(httpMessage.toString());
    }

    public int getStatusCode() {
        String[] lines = getHeader().split("\r\n");
        String[] statusLine = lines[0].split(" ");
        return Integer.parseInt(statusLine[1]);
    }
}
