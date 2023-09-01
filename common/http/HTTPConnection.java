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

    public HTTPConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendRequest(String request) throws IOException {
        out.write(request, 0, request.length());
        out.flush();
    }

    public String readResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line).append("\n");
        }
        return response.toString();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
