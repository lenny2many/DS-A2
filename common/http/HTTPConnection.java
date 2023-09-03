package common.http;

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

    public String readBuffer() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        int contentLength = -1;

        // Read up to end of headers (start of body)
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            response.append(line).append("\r\n");

            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        response.append("\r\n");        

        if (contentLength > 0) {
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            response.append(body);
        }    

        return response.toString();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
