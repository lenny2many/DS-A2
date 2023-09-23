package au.edu.adelaide.aggregationserver;

import static au.edu.adelaide.aggregationserver.AggregationServerConstants.*;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.stream.Stream;

import java.util.List;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.LinkedHashMap;


import common.util.CLI;
import common.http.HTTPServer;
import common.http.messages.HTTPRequest;
import common.util.JSONObject;
import common.util.IOUtility;


public class AggregationServer extends HTTPServer {
       
    private AggregatedWeatherUpdates aggregatedWeatherUpdates;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean fileCreated = false;

    public AggregationServer(ServerSocket serverSocket) throws RuntimeException {
        super(serverSocket);
        intialiseAggregatedWeatherUpdates();
    }

    private void intialiseAggregatedWeatherUpdates() {
        this.aggregatedWeatherUpdates = new AggregatedWeatherUpdates();
        loadContentServerData();
        loadStationData();
        CleanupUtility.startCleanupScheduler(scheduler, this.aggregatedWeatherUpdates);
    }

    private void loadContentServerData() {
        File serverDataDir = new File(BASE_STORAGE_PATH + URI_PREFIX);
        if (serverDataDir.exists() && serverDataDir.isDirectory()) {
            File[] files = serverDataDir.listFiles();
            if (files != null) { // null-check to prevent possible NullPointerException
                for (File file : files) {
                    try {
                        String uuidStr = file.getName().replace(FILE_EXTENSION, "");
                        UUID serverUuid = UUID.fromString(uuidStr);

                        LinkedList<WeatherUpdate> updates = (LinkedList<WeatherUpdate>) IOUtility.loadFromFile(file.getAbsolutePath());
                        if (updates == null) {
                            continue; // Skip if the file couldn't be loaded or was empty
                        }
                        aggregatedWeatherUpdates.contentServerUpdates.put(serverUuid, updates);
                        System.out.println("Loaded content server data for: " + serverUuid);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Skipping file with invalid UUID: " + file.getName());
                    }
                }
            }
        }
    }
    
    private void loadStationData() {
        Map<String, WeatherUpdate> stationUpdates = (Map<String, WeatherUpdate>) IOUtility.loadFromFile(STATION_FILE);
        if (stationUpdates != null) {
            aggregatedWeatherUpdates.mostRecentUpdatesByStation = stationUpdates;
        }
    }

    @Override
    public String handleGETRequest(HTTPRequest httpRequest) {
        try {
            String requestURI = httpRequest.getRequestURI();
            String[] requestURIComponents = requestURI.split("/");
            if (requestURIComponents.length > 1) {
                switch (requestURIComponents[1]) {
                    case "heartbeat":
                        UUID contentServerUUID = UUID.fromString(requestURIComponents[2]);
                        String response = handleHeartbeatRequest(httpRequest, contentServerUUID);
                        return response;
                    case "weather":
                        String weatherStationId = requestURIComponents[2];
                        System.out.println("Received weather request for: " + weatherStationId);
                        return handleWeatherRequest(httpRequest, weatherStationId);
                    case "shutdown":
                        UUID contentServerUUID2 = UUID.fromString(requestURIComponents[2]);
                        return handleContentServerShutdownRequest(httpRequest, contentServerUUID2);
                    default:
                        throw new IllegalArgumentException("Invalid request URI");
                }
            } else if (requestURI.equals("/")) {
                return handleWeatherRequest(httpRequest, "recent");
            }
            else {
                throw new IllegalArgumentException("Invalid request URI");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, 
                                 ERROR_RESPONSE + ZonedDateTime.now());
        }
    }

    private String handleHeartbeatRequest(HTTPRequest httpRequest, UUID contentServerUUID) {
        try {
            System.out.println("Received heartbeat from: " + contentServerUUID);
            aggregatedWeatherUpdates.updateContentServerTimestamp(contentServerUUID);
            return buildResponse(OK_STATUS_CODE, 
                                 HEARTBEAT_RESPONSE + ZonedDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, 
                                 ERROR_RESPONSE + ZonedDateTime.now());
        }
    }

    private String handleWeatherRequest(HTTPRequest httpRequest, String weatherStationId) {
        try {
            String responseBody = null;
            if (weatherStationId.equals("recent")) {
                responseBody = aggregatedWeatherUpdates.getMostRecentWeatherUpdate().weatherData.toJSONString();
            } else {
                responseBody = aggregatedWeatherUpdates.getMostRecentUpdateByStation(weatherStationId).weatherData.toJSONString();
            }
            return buildResponse(OK_STATUS_CODE, responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, 
                                 ERROR_RESPONSE + ZonedDateTime.now());
        }
    }
    
    private String handleContentServerShutdownRequest(HTTPRequest httpRequest, UUID contentServerUUID) {
        try {
            System.out.println("Received graceful shutdown request for content server: " + contentServerUUID);
            CleanupUtility.removeStaleContentServerData(aggregatedWeatherUpdates, contentServerUUID);
            Map<String, String> headers = new HashMap<>();
            headers.put("Connection", "close");
            headers = Collections.unmodifiableMap(headers);
            return buildResponse(OK_STATUS_CODE, 
                                 SHUTDOWN_RESPONSE + ZonedDateTime.now(),
                                 headers);
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, 
                                 ERROR_RESPONSE + ZonedDateTime.now());
        }
    }

    @Override
    public String handlePUTRequest(HTTPRequest httpRequest) {
        try {
            // Extract content server UUID and weather data from HTTP request
            WeatherUpdate weatherUpdate = extractWeatherUpdate(httpRequest);

            // Add the weather update
            aggregatedWeatherUpdates.addUpdate(weatherUpdate, weatherUpdate.weatherStationId);

            // Persist the updates
            persistNewestData(aggregatedWeatherUpdates, weatherUpdate.contentServerUUID);

            // Return appropraite HTTP response
            return buildResponse(this.fileCreated ? HTTP_CREATED_STATUS_CODE : OK_STATUS_CODE,
                                 PUT_RESPONSE + ZonedDateTime.now());

        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, 
                                 ERROR_RESPONSE + ZonedDateTime.now());
        }
    }

    @Override
    public String handlePOSTRequest(HTTPRequest httpRequest) {
        return buildResponse(METHOD_NOT_IMPLEMENTED_STATUS_CODE, 
                             POST_RESPONSE);
    }

    @Override
    public String handleDELETERequest(HTTPRequest httpRequest) {
        return buildResponse(METHOD_NOT_IMPLEMENTED_STATUS_CODE, 
                             DELETE_RESPONSE);
    }

    private String buildResponse(String statusCode, String response) {
        return buildResponse(statusCode, response, Collections.emptyMap());
    }
    
    private String buildResponse(String statusCode, String response, Map<String, String> headers) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 " + statusCode + "\r\n");
        responseBuilder.append("Date: " + ZonedDateTime.now() + "\r\n");
        responseBuilder.append("Server: AggregationServer\r\n");
        responseBuilder.append("Content-Length: " + response.length() + "\r\n");
        responseBuilder.append("Connection: " + headers.getOrDefault("Connection", "keep-alive") + "\r\n");
        
        responseBuilder.append("\r\n");
        responseBuilder.append(response);
        return responseBuilder.toString();
    }    

    private void persistNewestData(AggregatedWeatherUpdates aggregatedWeatherUpdates, UUID contentServerUUID) throws IOException {
        String serverSpecificFile = BASE_STORAGE_PATH + URI_PREFIX + contentServerUUID + FILE_EXTENSION;
        fileCreated = IOUtility.createFileIfNotExists(serverSpecificFile);
        IOUtility.saveToFile(aggregatedWeatherUpdates.contentServerUpdates.get(contentServerUUID), serverSpecificFile);
        IOUtility.saveToFile(aggregatedWeatherUpdates.mostRecentUpdatesByStation, STATION_FILE);
    }

    private WeatherUpdate extractWeatherUpdate(HTTPRequest httpRequest) {
        UUID contentServerUUID = UUID.fromString(httpRequest.getRequestURI().replace(URI_PREFIX, ""));
        JSONObject weatherData = null;
        try {
            weatherData = new JSONObject(httpRequest.getBody());
            System.out.println("Received weather data with ID: " + weatherData.get("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new WeatherUpdate(contentServerUUID, weatherData, ZonedDateTime.now());
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    private static void startAggregationServer(Map<String, String> argMap) {
        Integer port = Integer.parseInt(argMap.getOrDefault("port", DEFAULT_PORT));
        try (AggregationServer server = new AggregationServer(new ServerSocket(port))) {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CLI cli = CLI.initialiseCLI(BASE_STORAGE_PATH + PROGRAM_HELP_FILE_PATH);

        Map<String, String> argMap = cli.parseCLIArguments(args);

        if (argMap == null) {
            return;
        }

        startAggregationServer(argMap);
    }
}

class WeatherUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    UUID contentServerUUID;
    String weatherStationId;
    JSONObject weatherData;
    ZonedDateTime timestamp;

    public WeatherUpdate(UUID contentServerUUID, JSONObject weatherData, ZonedDateTime timestamp) {
        this.contentServerUUID = contentServerUUID;
        this.weatherStationId = weatherData.get("id");
        this.weatherData = weatherData;
        this.timestamp = timestamp;
    }
}

class AggregatedWeatherUpdates implements Serializable {
    private static final long serialVersionUID = 1L;
    Map<UUID, LinkedList<WeatherUpdate>> contentServerUpdates = new HashMap<>();
    Map<String, WeatherUpdate> mostRecentUpdatesByStation = new LinkedHashMap<>();

    public void addUpdate(WeatherUpdate update, String weatherStationId) {
        addContentServerUpdate(update);
        addStationUpdate(weatherStationId, update);
    }

    public void addContentServerUpdate(WeatherUpdate update) {
        LinkedList<WeatherUpdate> updates = contentServerUpdates.getOrDefault(update.contentServerUUID, new LinkedList<>());
        updates.addFirst(update);
        if (updates.size() > MAX_CONTENT_SERVER_UPDATES) {
            updates.removeLast();
        }
        contentServerUpdates.put(update.contentServerUUID, updates);
    }

    private void addStationUpdate(String weatherStationId, WeatherUpdate update) {
        mostRecentUpdatesByStation.put(weatherStationId, update);
    }

    public void updateContentServerTimestamp(UUID contentServerUUID) {
        LinkedList<WeatherUpdate> updates = contentServerUpdates.get(contentServerUUID);
        if (updates != null && !updates.isEmpty()) {
            updates.getFirst().timestamp = ZonedDateTime.now();
        }
    }

    public WeatherUpdate getMostRecentWeatherUpdate() {
        if (!mostRecentUpdatesByStation.isEmpty()) {
            return mostRecentUpdatesByStation.entrySet().iterator().next().getValue();
        }
        throw new RuntimeException("No weather updates exist.");
    }

    public WeatherUpdate getMostRecentUpdateByStation(String weatherStationId) {
        return mostRecentUpdatesByStation.get(weatherStationId);
    }

    public boolean isContentServerStale(UUID contentServerUUID) {
        LinkedList<WeatherUpdate> updates = contentServerUpdates.get(contentServerUUID);
        if (updates == null || updates.isEmpty()) {
            return true;
        }
        return updates.getFirst().timestamp.plusSeconds(STALE_DATA_THRESHOLD).isBefore(ZonedDateTime.now());
    }

    public void removeContentServer(UUID contentServerUUID) {
        contentServerUpdates.remove(contentServerUUID);
    }
}

class CleanupUtility {

    public static void startCleanupScheduler(ScheduledExecutorService scheduler, AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        System.out.println("Starting cleanup schedule...");
        
        // Schedule the cleanup task to run every 30 seconds
        scheduler.scheduleAtFixedRate(() -> runCleanupTask(aggregatedWeatherUpdates), 0, CLEANUP_SCHEDULE_INTERVAL, TimeUnit.SECONDS);
    }

    private static void runCleanupTask(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        try {
            System.out.println("Running cleanup task...");
            
            boolean staleContentServersExist = false;
            for(UUID uuid : getStaleContentServerUUIDs(aggregatedWeatherUpdates)) {
                try {
                    removeStaleContentServerData(aggregatedWeatherUpdates, uuid);
                    staleContentServersExist = true;
                    System.out.println("Removed stale content server data for: " + uuid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (staleContentServersExist && !aggregatedWeatherUpdates.contentServerUpdates.isEmpty()) {
                System.out.println("Persisting data updates...");
                IOUtility.saveToFile(aggregatedWeatherUpdates.contentServerUpdates, AggregationServerConstants.STATION_FILE);
            } else if (Files.exists(Paths.get(AggregationServerConstants.STATION_FILE)) && aggregatedWeatherUpdates.contentServerUpdates.isEmpty()) {
                System.out.println("No content servers exist. Clearing station data...");
                IOUtility.deleteFile(AggregationServerConstants.STATION_FILE);
            }
            
            System.out.println("Cleanup task complete.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred during the cleanup task.");
        }
    }

    private static List<UUID> getStaleContentServerUUIDs(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        List<UUID> staleUUIDs = aggregatedWeatherUpdates.contentServerUpdates.keySet().stream()
            .filter(uuid -> aggregatedWeatherUpdates.isContentServerStale(uuid))
            .collect(Collectors.toList());
        
        return staleUUIDs;
    }

    public static void removeStaleContentServerData(AggregatedWeatherUpdates aggregatedWeatherUpdates, UUID uuid) {
        // Remove content server
        aggregatedWeatherUpdates.removeContentServer(uuid);

        // Check the content server UUID from the most recent updates data structure for stale content server
        aggregatedWeatherUpdates.mostRecentUpdatesByStation.entrySet().removeIf(entry -> entry.getValue().contentServerUUID.equals(uuid));

        // Delete file
        try {
            String filePath = BASE_STORAGE_PATH + URI_PREFIX + uuid + FILE_EXTENSION;
            IOUtility.deleteFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
