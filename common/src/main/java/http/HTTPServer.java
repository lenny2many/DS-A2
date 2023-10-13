package http;

import http.messages.HTTPRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic HTTP server which is capable of handling client requests
 * and responding according to the implemented HTTP methods.
 */
public abstract class HTTPServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private static final Logger LOGGER = Logger.getLogger(HTTPServer.class.getName());

    /**
     * Constructs a HTTPServer.
     *
     * @param serverSocket The server socket through which the server communicates.
     * @throws RuntimeException If the server socket is invalid.
     */
    public HTTPServer(ServerSocket serverSocket) throws RuntimeException {
        this.serverSocket = serverSocket;
        threadPool = Executors.newFixedThreadPool(10);
        LOGGER.log(Level.INFO, "Server started and listening on port " + serverSocket.getLocalPort());
    }

    /**
     * Abstract methods to handle the following HTTP requests.
     * GET
     * PUT
     * POST
     * DELETE
     *
     * @param httpRequest The incoming HTTP request.
     * @return The server's response.
     */
    public abstract String handleGETRequest(HTTPRequest httpRequest);
    public abstract String handlePUTRequest(HTTPRequest httpRequest);
    public abstract String handlePOSTRequest(HTTPRequest httpRequest);
    public abstract String handleDELETERequest(HTTPRequest httpRequest);
    public abstract String handleError();

    /**
     * Handles the incoming client request by processing the HTTP method
     * and sends back an appropriate response.
     *
     * @param clientSocket The socket connecting to the client.
     * @throws IOException If an I/O error occurs when creating the HTTP connection.
     */
    public void receiveClientRequest(Socket clientSocket, BufferedReader in, BufferedWriter out) throws IOException {
        try (HTTPConnection conn = new HTTPConnection(clientSocket, out, in)) {
            boolean keepAlive = false;
            HTTPRequest httpRequest = new HTTPRequest();
            do {
                try {
                    httpRequest = (HTTPRequest) conn.readMessage();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error reading client request", e.getMessage());
                    conn.sendMessage(this.handleError());
                }

                // Check if the HTTP request is valid
                if (httpRequest == null) {
                    continue;
                }
                System.out.println("--------------------------------------");
                LOGGER.log(Level.INFO, "Received client request from " + clientSocket.getInetAddress() + ":\n" + httpRequest.toString());
                String httpResponse = null;
                switch (httpRequest.getRequestMethod() != null ? httpRequest.getRequestMethod() : "") {
                    case "GET":
                        httpResponse = this.handleGETRequest(httpRequest);
                        break;
                    case "PUT":
                        httpResponse = this.handlePUTRequest(httpRequest);
                        break;
                    case "POST":
                        httpResponse = this.handlePOSTRequest(httpRequest);
                        break;
                    case "DELETE":
                        httpResponse = this.handleDELETERequest(httpRequest);
                        break;
                    default:
                        LOGGER.log(Level.WARNING, "Received an invalid HTTP method from: " + clientSocket.getInetAddress());
                        break;
                }

                if (keepAlive = httpRequest.shouldKeepConnectionAlive()) {
                    LOGGER.log(Level.INFO, "Keeping the connection alive upon request from: " + clientSocket.getInetAddress());
                } else {
                    LOGGER.log(Level.INFO, "Closing the connection upon completion of the request from: " + clientSocket.getInetAddress());
                }

                conn.sendMessage(httpResponse);
                LOGGER.log(Level.INFO, "Sent response to client " + clientSocket.getInetAddress() + ":\n" + httpResponse);
            } while (keepAlive);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "I/O exception while processing request from: " + clientSocket.getInetAddress(), e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error while processing request from: " + clientSocket.getInetAddress(), e);
        }
    }
    
    /**
     * Continuously listens for client connections, accepts incoming connections,
     * and delegates the request handling to a thread pool.
     */
    public void run() {
        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClientSocket(clientSocket));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
            }
        }
    }

    /**
     * Handles the client socket by processing the request and closing resources afterward.
     *
     * @param clientSocket The client socket.
     */
    private void handleClientSocket(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.receiveClientRequest(clientSocket, in, out);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error receiving client request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing client socket", e);
            }
        }
    }

    /**
     * Closes the server socket.
     *
     * @throws IOException If an I/O error occurs when closing the socket.
     */
    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
