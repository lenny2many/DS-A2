package aggregation_server;

import common.http.HTTPServer;


public class AggregationServer extends HTTPServer {
    private static final int PORT = 4567;

    private static final String responseCode = "AggregationServer/resources/200Response.txt";
    private static final String payload = "AggregationServer/resources/dummy.json";

    public AggregationServer(int port) {
        super(4567);
    }

    @Override
    public String buildResponse() {
        String httpResponse = "HTTP/1.1 200 OK\r\n\r\nHello, this is a simple HTTP server!";
        return httpResponse;
    }

    public static void main(String[] args) {
        try (AggregationServer server = new AggregationServer(PORT)) {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}