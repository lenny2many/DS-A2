package common.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;


public abstract class HTTPServer implements AutoCloseable {
    private final ServerSocket serverSocket;

    public HTTPServer(int port) throws RuntimeException {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server started and listening on port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Could not create server socket on port " + port, e);
        }
    }

    public abstract String buildResponse();

    public void acceptClientConnection() {
        try (Socket clientSocket = serverSocket.accept();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"))) {

            System.out.println("Received a connection from " + clientSocket.getInetAddress());
            String httpResponse = buildResponse();
            out.write(httpResponse);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
        while (true) {
            this.acceptClientConnection();
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
