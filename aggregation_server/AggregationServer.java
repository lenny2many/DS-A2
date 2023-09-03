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
    public String buildGETResponse() {
        String httpResponse = "HTTP/1.1 200 OK\r\nContent-Length:57\r\n\r\nHello, this is a simple HTTP server! GET request received";
        return httpResponse;
    }

    @Override
    public String buildPUTResponse() {
        String httpResponse = "HTTP/1.1 200 OK\r\nContent-Length:57\r\n\r\nHello, this is a simple HTTP server! PUT request received";
        return httpResponse;
    }

    public static void main(String[] args) {
        try (AggregationServer server = new AggregationServer(new ServerSocket(PORT))) {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}