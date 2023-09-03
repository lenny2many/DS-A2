package aggregation_server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import common.http.HTTPServer;
import common.http.messages.HTTPRequest;


public class AggregationServer extends HTTPServer {
    private static final int PORT = 4567;

    private static final String DATA_FILE = "aggregation_server/resources/data.txt";
    private static List<WeatherUpdate> weatherUpdates = new ArrayList<>();

    private class WeatherUpdate {
        String contentServerID;
        ZonedDateTime lastUpdated;
        String weatherData;
    }

    public AggregationServer(ServerSocket serverSocket) throws RuntimeException {
        super(serverSocket);
    }

    @Override
    public String handleGETRequest(HTTPRequest httpRequest) {
        String httpResponse = buildGETResponse();
        return httpResponse;
    }

    @Override
    public String handlePUTRequest(HTTPRequest httpRequest) {
        try {
            // Write the PUT request body to the data file
            Files.write(new File(DATA_FILE).toPath(), httpRequest.getBody().getBytes());
            return buildPUTResponse();
        } catch (IOException e) {
            e.printStackTrace();
            return buildErrorResponse("Failed to write data");
        }
    }

    private String buildErrorResponse(String message) {
        return "HTTP/1.1 500 Internal Server Error\r\nContent-Length:" + message.length() + "\r\n\r\n" + message;
    }

    private String buildGETResponse() {
        return "HTTP/1.1 200 OK\r\nContent-Length:57\r\n\r\nHello, this is a simple HTTP server! GET request received";
    }

    private String buildPUTResponse() {
        return "HTTP/1.1 200 OK\r\nContent-Length:57\r\n\r\nHello, this is a simple HTTP server! PUT request received";
    }

    public static void main(String[] args) {
        try (AggregationServer server = new AggregationServer(new ServerSocket(PORT))) {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}