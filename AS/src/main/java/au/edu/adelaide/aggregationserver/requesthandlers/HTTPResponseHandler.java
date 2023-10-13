package au.edu.adelaide.aggregationserver.requesthandlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import au.edu.adelaide.aggregationserver.AggregationServer;


public class HTTPResponseHandler {
    public static Map<String, String> prepareResponseHeaders(AggregationServer aggregationServer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        headers.put("Timestamp", String.valueOf(aggregationServer.lamportClock.peekTime()));
        return Collections.unmodifiableMap(headers);
    }

    public static String buildResponse(String statusCode, String response) {
        return buildResponse(statusCode, response, Collections.emptyMap());
    }
    
    public static String buildResponse(String statusCode, String response, Map<String, String> headers) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 " + statusCode + "\r\n");
        responseBuilder.append("Server: AggregationServer/1.0 (Unix)\r\n");
        responseBuilder.append("Content-Length: " + response.length() + "\r\n");
        
        // Default Connection header
        if (!headers.containsKey("Connection")) {
            responseBuilder.append("Connection: keep-alive\r\n");
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            responseBuilder.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        
        responseBuilder.append("\r\n");
        responseBuilder.append(response);
        return responseBuilder.toString();
    }
}