package common.http;

import common.http.messages.HTTPRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class HTTPServer implements AutoCloseable {
    private final ServerSocket serverSocket;

    public HTTPServer(ServerSocket serverSocket) throws RuntimeException {
        this.serverSocket = serverSocket;
        System.out.println("Server started and listening on port " + serverSocket.getLocalPort());
    }

    public abstract String handleGETRequest(HTTPRequest httpRequest);
    public abstract String handlePUTRequest(HTTPRequest httpRequest);
    public abstract String handlePOSTRequest(HTTPRequest httpRequest);
    public abstract String handleDELETERequest(HTTPRequest httpRequest);

    public void receiveClientRequest(Socket clientSocket) {
        try (HTTPConnection conn = new HTTPConnection(clientSocket);) {
            HTTPRequest httpRequest = new HTTPRequest(conn.readBuffer());

            String httpResponse = null;
            switch (httpRequest.getRequestMethod()) {
                case "GET":
                    System.out.println("GET request received: " + httpRequest.getRequestLine());
                    httpResponse = this.handleGETRequest(httpRequest);
                    
                    break;
                case "PUT":
                    System.out.println("PUT request received: " + httpRequest.getRequestLine());
                    httpResponse = this.handlePUTRequest(httpRequest);
                    break;
                case "POST":
                    System.out.println("POST request received");
                    httpResponse = this.handlePOSTRequest(httpRequest);
                    break;
                case "DELETE":
                    System.out.println("DELETE request received");
                    httpResponse = this.handleDELETERequest(httpRequest);
                    break;
                default:
                    System.out.println("Invalid request received");
                    break;
            }

            conn.sendData(httpResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void run() {
        while (true) {
            try (Socket clientSocket = serverSocket.accept();) {
                System.out.println("Received a connection from " + clientSocket.getInetAddress());
                this.receiveClientRequest(clientSocket);
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
