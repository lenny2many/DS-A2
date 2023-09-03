package common.http;

import common.http.messages.HTTPMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class HTTPConnection implements AutoCloseable {
    private final Socket socket;
    protected OutputStreamWriter out;
    protected BufferedReader in;

    public HTTPConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendData(String request) throws IOException {
        out.write(request, 0, request.length());
        out.flush();
    }

    public HTTPMessage readBuffer() throws IOException {
        StringBuilder message = new StringBuilder();
        String line;
        int contentLength = -1;

        // Read up to end of headers (start of body)
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            message.append(line).append("\r\n");

            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        message.append("\r\n");

        // Read body
        if (contentLength > 0) {
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            message.append(body);
        }

        return new HTTPMessage(message.toString());
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
