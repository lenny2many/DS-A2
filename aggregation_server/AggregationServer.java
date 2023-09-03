package aggregation_server;

import java.net.ServerSocket;

import common.http.HTTPServer;


public class AggregationServer extends HTTPServer {
    private static final int PORT = 4567;

    private static final String responseCode = "AggregationServer/resources/200Response.txt";
    private static final String payload = "AggregationServer/resources/dummy.json";

    public AggregationServer(ServerSocket serverSocket) throws RuntimeException {
        super(serverSocket);
    }

    @Override
    public String buildResponse() {
        String httpResponse = "HTTP/1.1 200 OK\r\nContent-Length:36\r\n\r\nHello, this is a simple HTTP server!";
        return httpResponse;
    }

    public void determineRequestHeader(String request) {
        String[] requestLines = request.split("\r\n");
        String[] requestLine = requestLines[0].split(" ");
        String requestMethod = requestLine[0];
        String requestLocation = requestLine[1];
        String requestProtocol = requestLine[2];
        System.out.println("Request Method: " + requestMethod);
        System.out.println("Request Location: " + requestLocation);
        System.out.println("Request Protocol: " + requestProtocol);
    }

    public static void main(String[] args) {
        try (AggregationServer server = new AggregationServer(new ServerSocket(PORT))) {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}