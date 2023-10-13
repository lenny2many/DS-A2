package au.edu.adelaide.aggregationserver.requesthandlers;

import static au.edu.adelaide.aggregationserver.AggregationServerConstants.*;

import au.edu.adelaide.aggregationserver.AggregationServer;
import au.edu.adelaide.aggregationserver.data.WeatherUpdate;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import http.HTTPServer;
import http.messages.HTTPRequest;
import util.JSONObject;
import util.LamportClock.EventType;

/**
 * Handles HTTP requests sent to the aggregation server, such as GET, PUT, POST, and DELETE.
 * This handler manages the receipt, processing, and response of HTTP requests in a manner
 * appropriate for the aggregation server's functionality.
 */
public class HTTPRequestHandler extends HTTPServer {
    private AggregationServer aggregationServer;
    private static final Logger LOGGER = Logger.getLogger(HTTPRequestHandler.class.getName());
    
    private static final String HEARTBEAT = "heartbeat";
    private static final String WEATHER = "weather";
    private static final String SHUTDOWN = "shutdown";

    /**
     * Constructs a new HTTPRequestHandler.
     *
     * @param serverSocket        The server socket to handle HTTP requests.
     * @param aggregationServer   The aggregation server to which this handler is attached.
     */
    public HTTPRequestHandler(ServerSocket serverSocket, AggregationServer aggregationServer) {
        super(serverSocket);
        this.aggregationServer = aggregationServer;
    }

    /**
     * Main running loop for handling HTTP requests.
     */
    @Override
    public void run() {
        super.run();
    }

    /**
     * Handles GET HTTP requests by delegating to specific handlers based on URI components.
     *
     * @param httpRequest The HTTP request to be handled.
     * @return The HTTP response as a string.
     */
    @Override
    public String handleGETRequest(HTTPRequest httpRequest) {
        try {
            String[] requestURIComponents = getRequestURIComponents(httpRequest);
            
            if (requestURIComponents.length > 1) {
                return handleRequestByComponent(requestURIComponents, httpRequest);
            } else if (isBaseRequestURI(requestURIComponents)) {
                return handleWeatherRequest(httpRequest, "recent");
            } else {
                throw new IllegalArgumentException("Invalid request URI");
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Handles PUT HTTP requests, typically involving updating or creating resources.
     *
     * @param httpRequest The HTTP request to be handled.
     * @return The HTTP response as a string.
     */
    @Override
    public String handlePUTRequest(HTTPRequest httpRequest) {
        try {
            int receivedTimestamp = Integer.parseInt(httpRequest.getHeader("Timestamp"));
            EventType eventType = aggregationServer.lamportClock.processReceivedTimestamp(receivedTimestamp);

            // Extract content server UUID and weather data from HTTP request
            WeatherUpdate weatherUpdate = extractWeatherUpdate(httpRequest);
            processRequestBasedOnEvent(eventType, weatherUpdate);

            // Prepare headers for response
            Map<String, String> headers = HTTPResponseHandler.prepareResponseHeaders(aggregationServer);

            // Return appropriate HTTP response
            return HTTPResponseHandler.buildResponse(aggregationServer.checkNewFileStatus() ? HTTP_CREATED_STATUS_CODE : OK_STATUS_CODE,
                                                     PUT_RESPONSE + ZonedDateTime.now(),
                                                     headers);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Handles POST HTTP requests, although in the current implementation this method is not supported.
     *
     * @param httpRequest The HTTP request to be handled.
     * @return The HTTP response as a string.
     */
    @Override
    public String handlePOSTRequest(HTTPRequest httpRequest) {
        return HTTPResponseHandler.buildResponse(METHOD_NOT_IMPLEMENTED_STATUS_CODE, 
                                                 POST_RESPONSE);
    }

    /**
     * Handles DELETE HTTP requests, although in the current implementation this method is not supported.
     *
     * @param httpRequest The HTTP request to be handled.
     * @return The HTTP response as a string.
     */
    @Override
    public String handleDELETERequest(HTTPRequest httpRequest) {
        return HTTPResponseHandler.buildResponse(METHOD_NOT_IMPLEMENTED_STATUS_CODE, 
                             DELETE_RESPONSE);
    }

    @Override
    public String handleError() {
        return HTTPResponseHandler.buildResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, 
                                                 ERROR_RESPONSE + ZonedDateTime.now());
    }

    /**
     * Parses the weather data from the HTTP request body and constructs a WeatherUpdate object.
     *
     * @param httpRequest The HTTP request containing the weather data.
     * @return A WeatherUpdate object containing the parsed weather data.
     * @throws Exception If the parsing of the weather data fails.
     */
    private WeatherUpdate extractWeatherUpdate(HTTPRequest httpRequest) throws Exception {
        UUID contentServerUUID = extractUUIDFromRequestURI(httpRequest.getURI());
        JSONObject weatherData = parseWeatherData(httpRequest.getBody());
        return new WeatherUpdate(contentServerUUID, weatherData);
    }

    /**
     * Splits the request URI into components for easier handling.
     *
     * @param httpRequest The HTTP request containing the URI.
     * @return An array of strings, each representing a component of the URI.
     */
    private String[] getRequestURIComponents(HTTPRequest httpRequest) {
        String requestURI = httpRequest.getURI();
        return requestURI.split("/");
    }

    /**
     * Checks whether the request URI is a base URI.
     *
     * @param requestURIComponents The components of the request URI.
     * @return A boolean indicating whether the URI is a base URI.
     */
    private boolean isBaseRequestURI(String[] requestURIComponents) {
        return requestURIComponents[0].equals("/");
    }

    /**
     * Handles requests by delegating to specific handlers based on the request URI components.
     *
     * @param requestURIComponents The components of the request URI.
     * @param httpRequest          The HTTP request to be handled.
     * @return The HTTP response as a string.
     */
    private String handleRequestByComponent(String[] requestURIComponents, HTTPRequest httpRequest) {
        switch (requestURIComponents[1]) {
            case HEARTBEAT:
                UUID contentServerUUID = UUID.fromString(requestURIComponents[2]);
                return handleHeartbeatRequest(httpRequest, contentServerUUID);
            case WEATHER:
                String weatherStationId = requestURIComponents[2];
                return handleWeatherRequest(httpRequest, weatherStationId);
            case SHUTDOWN:
                UUID contentServerUUID2 = UUID.fromString(requestURIComponents[2]);
                return handleContentServerShutdownRequest(httpRequest, contentServerUUID2);
            default:
                throw new IllegalArgumentException("Invalid request URI");
        }
    }

    /**
     * Processes the request based on the event type and updates the weather data if applicable.
     *
     * @param eventType    The type of the event, in terms of Lamport's logical clock.
     * @param weatherUpdate The updated weather data.
     * @throws Exception If processing the request fails.
     */
    private void processRequestBasedOnEvent(EventType eventType, WeatherUpdate weatherUpdate) throws Exception {
        switch (eventType) {
            case BEFORE:
                break;
            case CONCURRENT:
                LOGGER.log(Level.INFO, "Received concurrent event.");
                // Break ties by comparing weather data timestamps
                if (aggregationServer.isUpdateMoreRecent(weatherUpdate)) {
                    aggregationServer.addWeatherUpdate(weatherUpdate);
                }
                break;
            case AFTER:
                LOGGER.log(Level.INFO, "Received most recent event.");
                aggregationServer.addWeatherUpdate(weatherUpdate);
                break;
        }
    }

    /**
     * Handles a request to fetch the most recent weather data for a given weather station.
     *
     * @param httpRequest      The HTTP request to be handled.
     * @param weatherStationId The ID of the weather station.
     * @return The HTTP response as a string.
     */
    private String handleWeatherRequest(HTTPRequest httpRequest, String weatherStationId) {
        try {
            String responseBody = aggregationServer.getMostRecentUpdateJson(weatherStationId);
            return HTTPResponseHandler.buildResponse(OK_STATUS_CODE, 
                                                     responseBody);
        } catch (Exception e) {
            return handleException(e);
        }
    }
    
    /**
     * Handles a request to shutdown a content server gracefully.
     *
     * @param httpRequest       The HTTP request to be handled.
     * @param contentServerUUID The UUID of the content server.
     * @return The HTTP response as a string.
     */
    private String handleContentServerShutdownRequest(HTTPRequest httpRequest, UUID contentServerUUID) {
        try {
            System.out.println("Received graceful shutdown request for content server: " + contentServerUUID);
            Map<String, String> headers = HTTPResponseHandler.prepareResponseHeaders(aggregationServer);
            return HTTPResponseHandler.buildResponse(OK_STATUS_CODE, 
                                                     SHUTDOWN_RESPONSE + ZonedDateTime.now(),
                                                     headers);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Extracts a UUID from the request URI, which is typically used to identify a content server.
     *
     * @param requestURI The request URI containing the UUID.
     * @return The extracted UUID.
     * @throws Exception If extracting the UUID fails.
     */
    private UUID extractUUIDFromRequestURI(String requestURI) throws Exception {
        try {
            return UUID.fromString(requestURI.replace(URI_PREFIX, ""));
        } catch (IllegalArgumentException e) {
            String errorMessage = "Failed to parse content server UUID from URI: " + requestURI;
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new Exception(errorMessage, e);
        }
    }
    
    /**
     * Parses the weather data from a string and constructs a JSONObject.
     *
     * @param body The string containing the weather data.
     * @return A JSONObject containing the parsed weather data.
     * @throws Exception If parsing the weather data fails.
     */
    private JSONObject parseWeatherData(String body) throws Exception {
        try {
            JSONObject weatherData = new JSONObject(body);
            return weatherData;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse weather data from request body: " + body);
            throw new Exception("Failed to parse weather data from request body");
        }
    }

    /**
     * Handles exceptions that occur during the processing of HTTP requests.
     *
     * @param e The exception that occurred.
     * @return The HTTP response as a string, indicating that an internal server error occurred.
     */
    private String handleException(Exception e) {
        LOGGER.log(Level.WARNING, "An exception occurred: " + e.getMessage());
        return handleError();
    }

    /**
     * Closes the server socket and releases all associated resources.
     *
     * @throws IOException If an I/O error occurs while closing the socket.
     */
    @Override
    public void close() throws IOException {
        super.close();
    }

    /**
     * Handles a request to update the heartbeat timestamp of a content server.
     *
     * @param httpRequest       The HTTP request containing the content server's heartbeat.
     * @param contentServerUUID The UUID of the content server.
     * @return The HTTP response as a string.
     *
     * @deprecated This method might be deprecated due to changes in how heartbeats are managed. 
     * Consider utilizing an alternative strategy for managing server heartbeats.
     */
    @Deprecated
    private String handleHeartbeatRequest(HTTPRequest httpRequest, UUID contentServerUUID) {
        try {
            LOGGER.log(Level.INFO, "Received heartbeat from: {0}", contentServerUUID);
            aggregationServer.updateContentServerTimestamp(contentServerUUID);
            return HTTPResponseHandler.buildResponse(OK_STATUS_CODE, 
                                                     HEARTBEAT_RESPONSE + ZonedDateTime.now());
        } catch (Exception e) {
            return handleException(e);
        }
    }
}
