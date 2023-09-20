package common.http;

import common.http.messages.HTTPRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class HTTPServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public HTTPServer(ServerSocket serverSocket) throws RuntimeException {
        this.serverSocket = serverSocket;
        threadPool = Executors.newFixedThreadPool(10);
        System.out.println("Server started and listening on port " + serverSocket.getLocalPort());
    }

    public abstract String handleGETRequest(HTTPRequest httpRequest);
    public abstract String handlePUTRequest(HTTPRequest httpRequest);
    public abstract String handlePOSTRequest(HTTPRequest httpRequest);
    public abstract String handleDELETERequest(HTTPRequest httpRequest);

    public void receiveClientRequest(Socket clientSocket) throws IOException {
        try (HTTPConnection conn = new HTTPConnection(clientSocket);) {
            boolean keepAlive = false;
            do {
                HTTPRequest httpRequest = (HTTPRequest) conn.readMessage();

                if (httpRequest == null) {
                    continue;
                }
                
                String httpResponse = null;
                switch (httpRequest.getRequestMethod()) {
                    case "GET":
                        System.out.println("GET request received: \n" + httpRequest.getRequestLine() + "\n");
                        httpResponse = this.handleGETRequest(httpRequest);
                        break;
                    case "PUT":
                        System.out.println("PUT request received: \n" + httpRequest.getRequestLine() + "\n");
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
                conn.sendMessage(httpResponse);
                if (keepAlive = httpRequest.shouldKeepConnectionAlive()) {
                    System.out.println("Client is keeping connection alive\n");
                    System.out.println("----------------------------------------\n");
                } else {
                    System.out.println("Client is closing connection");
                }
            } while (keepAlive);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();
                System.out.println("Received a connection from " + clientSocket.getInetAddress());

                threadPool.submit(() -> {
                    try {
                        this.receiveClientRequest(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

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
