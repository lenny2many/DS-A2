package common.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class HTTPServer implements AutoCloseable {
    private final ServerSocket serverSocket;

    public HTTPServer(ServerSocket serverSocket) throws RuntimeException {
        this.serverSocket = serverSocket;
        System.out.println("Server started and listening on port " + serverSocket.getLocalPort());
    }

    public abstract String buildResponse();

    public void acceptClientConnection(Socket clientSocket) {
        try (HTTPConnection conn = new HTTPConnection(clientSocket);) {
            System.out.println("Received a connection from " + clientSocket.getInetAddress());
            conn.readBuffer();
            String httpResponse = this.buildResponse();
            conn.sendData(httpResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void run() {
        while (true) {
            try (Socket clientSocket = serverSocket.accept();) {
                this.acceptClientConnection(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
