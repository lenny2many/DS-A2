package aggregation_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import common.http.HTTPServer;
import common.http.messages.HTTPRequest;
import common.util.JSONObject;


public class AggregationServer extends HTTPServer {
    // AGGREGATION SERVER DEFAULTS
    private static final String DEFAULT_PORT = "4567";
    private static final String LATEST_UPDATES_FILE = "LATEST_UPDATES";
    private static final String BASE_PATH = "aggregation_server/resources/data/";
    // STATUS CODES
    private static final String INTERNAL_SERVER_ERROR = "500 Internal server error";
    private static final String METHOD_NOT_IMPLEMENTED = "400 Method not implemented";
    private static final String EMPTY_REQUEST_BODY = "204 Empty Request Body";
    private static final String HTTP_CREATED = "201 HTTP_CREATED";
    private static final String OK = "200 OK";
    // PUT BODY RESPONSES
    private static final String PUT_RESPONSE_BODY = "Aggregation Server successfully received PUT request at ";
    // HTTP REQUEST DEFAULTS
    private static final String URI_PREFIX = "/data/CS_";

    private AggregatedWeatherData aggregatedWeatherData;
    

    private static class AggregatedWeatherData implements Serializable {
        private static final int MAX_UPDATES = 20;
        private Map<UUID, Deque<WeatherData>> aggregatedWeatherData;
        private Map<String, WeatherData> latestUpdates;
        private Boolean FileCreated = false;
         
        private static class WeatherData implements Serializable {
            JSONObject weatherData;
            ZonedDateTime lastUpdated;

            public WeatherData(JSONObject weatherData, ZonedDateTime lastUpdated) {
                this.lastUpdated = lastUpdated;
                this.weatherData = weatherData;
            }
            
            private String getStationID() {
                return weatherData.get("id");
            }

            private String getWeatherDataString() {
                return weatherData.toSimpleListString();
            }
        }

        public AggregatedWeatherData() {
            aggregatedWeatherData = new HashMap<>();
            latestUpdates = new HashMap<>();

            try {
                readDataFromFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void addUpdate(UUID contentServerUUID, JSONObject weatherUpdate) {
            // Convert JSONObject to WeatherData object
            WeatherData latestUpdate = new WeatherData(weatherUpdate, ZonedDateTime.now());

            System.out.println("Adding update for station ID: " + latestUpdate.getStationID());

            // Replace the most recent running update for the station ID
            latestUpdates.put(latestUpdate.getStationID(), latestUpdate);

            // Replace the most recent update
            latestUpdates.put("latestUpdate", latestUpdate);

            // Retrieve the associated queue with the UUID or create a new one
            Deque<WeatherData> updatesQueue = aggregatedWeatherData.getOrDefault(contentServerUUID, new LinkedList<>());

            // Add the new update to the end of the queue
            updatesQueue.addLast(latestUpdate);

            // Utilise cyclic buffer to rotate the weather updates maintaining order
            if (updatesQueue.size() > MAX_UPDATES) {
                updatesQueue.removeFirst();
            }

            // Put the queue back in the map (this is necessary if a new queue was created)
            aggregatedWeatherData.put(contentServerUUID, updatesQueue);
        }

        public String getMostRecentUpdate(String... stationID) {
            if (latestUpdates.isEmpty()) {
                return "No data available";
            }

            if (stationID.length == 0) {
                return latestUpdates.get("latestUpdate").getWeatherDataString();
            }
            return latestUpdates.get(stationID[0]).getWeatherDataString();
        }
        
        private void writeDataToFile(UUID contentServerUUID) throws IOException {
            String filePath = BASE_PATH + "CS_" + contentServerUUID.toString();
            File file = new File(filePath);
            System.out.println("Writing data to file: " + filePath);
            file.getParentFile().mkdirs();
            FileCreated = !file.exists();

            // Store data in separate files for each Content Server
            try (FileOutputStream fileOut = new FileOutputStream(filePath);
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(aggregatedWeatherData);
            } catch (IOException i) {
                System.out.println("Error writing data to file.");
                throw i;
            }

            // Store the most recent updates in a separate file
            try (FileOutputStream fileOut = new FileOutputStream(BASE_PATH + LATEST_UPDATES_FILE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(latestUpdates);
            } catch (IOException i) {
                System.out.println("Error writing data to file.");
                throw i;
            }
        }

        private void readDataFromFile() throws IOException, ClassNotFoundException {
            // For each UUID, read its corresponding data file
            for (UUID contentServerUUID : getAllContentServerUUIDs()) {
                String contentServerFilePath = BASE_PATH + contentServerUUID.toString();
                try (FileInputStream fileIn = new FileInputStream(contentServerFilePath);
                     ObjectInputStream in = new ObjectInputStream(fileIn)) {
                    Deque<WeatherData> updatesForServer = (Deque<WeatherData>) in.readObject();
                    aggregatedWeatherData.put(contentServerUUID, updatesForServer);
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println("Error reading data for Content Server with UUID: " + contentServerUUID);
                    ex.printStackTrace();
                }
            }
        
            // Read the latest updates from its file
            File latestUpdatesFile = new File(BASE_PATH + LATEST_UPDATES_FILE);
            if (latestUpdatesFile.exists()) {
                try (FileInputStream fileIn = new FileInputStream(latestUpdatesFile);
                     ObjectInputStream in = new ObjectInputStream(fileIn)) {
                    Map<String, WeatherData> readLatestUpdates = (Map<String, WeatherData>) in.readObject();
                    latestUpdates.putAll(readLatestUpdates);
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println("Error reading latest updates.");
                    ex.printStackTrace();
                }
            }
        }
        

        private Set<UUID> getAllContentServerUUIDs() {
            // Assuming files are named as URI_PREFIX + UUID, extract UUIDs from filenames
            File directory = new File(BASE_PATH);
            if (!directory.exists() || !directory.isDirectory()) {
                System.out.println("Directory " + BASE_PATH + " does not exist or is not a directory.");
                return Collections.emptySet();
            }

            File[] files = directory.listFiles();
            if (files == null) {
                System.out.println("Error accessing the directory or it's empty.");
                return Collections.emptySet();
            }

            Set<UUID> uuids = new HashSet<>();
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith(URI_PREFIX)) {
                    String name = file.getName();
                    String uuidStr = name.substring(URI_PREFIX.length());
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        uuids.add(uuid);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid UUID in filename: " + name);
                    }
                }
            }
            return uuids;
        }

    }

    public AggregationServer(ServerSocket serverSocket) throws RuntimeException {
        super(serverSocket);
        aggregatedWeatherData = new AggregatedWeatherData();
    }

    @Override
    public String handleGETRequest(HTTPRequest httpRequest) {
        try {
            JSONObject weatherUpdate = new JSONObject(aggregatedWeatherData.getMostRecentUpdate());
            return buildResponse(OK, weatherUpdate.toJsonString());
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(INTERNAL_SERVER_ERROR, "Failed to retrieve data");
        }
    }

    @Override
    public String handlePUTRequest(HTTPRequest httpRequest) {
        try {
            // Extract UUID from request URI
            UUID contentServerUUID = extractUUID(httpRequest);

            // Extract weather data from the request body
            JSONObject weatherUpdate = extractWeatherData(httpRequest);

            // Add the weather update to the Aggregation Servers running list of updates for the Content Server
            aggregatedWeatherData.addUpdate(contentServerUUID, weatherUpdate);

            // Save the updates to file
            aggregatedWeatherData.writeDataToFile(contentServerUUID);

            // Check FileCreated flag.
            // If new file was created, respond with 201 HTTP_CREATED and reset flag
            if (aggregatedWeatherData.FileCreated == true) {
                aggregatedWeatherData.FileCreated = false;
                return buildResponse(HTTP_CREATED, PUT_RESPONSE_BODY + ZonedDateTime.now());
            }

            // Else return 200 OK
            return buildResponse(OK, PUT_RESPONSE_BODY + ZonedDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(INTERNAL_SERVER_ERROR, "Failed to save data");
        }
    }

    private String buildResponse(String statusCode, String responseBody) {
        return String.format("HTTP/1.1 %s\r\nContent-Length: %d\r\n\r\n%s\r\n", statusCode, responseBody.length(), responseBody);
    }

    private UUID extractUUID(HTTPRequest httpRequest) {
        String requestURI = httpRequest.getRequestURI();
        validateRequestURI(requestURI);
        return validateAndReturnUUID(requestURI.substring(URI_PREFIX.length()));
    }

    private void validateRequestURI(String requestURI) {
        // Check if the URI starts with the expected prefix
        if (!requestURI.startsWith(URI_PREFIX)) {
            throw new IllegalArgumentException("Request URI does not start with the expected prefix: " + URI_PREFIX);
        }
    }
    
    private UUID validateAndReturnUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID: " + uuid);
        }
    }

    private JSONObject extractWeatherData(HTTPRequest httpRequest) {
        String body = httpRequest.getBody();
        JSONObject weatherUpdate;
        try {
            weatherUpdate = new JSONObject(body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON from request body");
        }
        verifyWeatherData(weatherUpdate);
        return weatherUpdate;
    }
    
    private void verifyWeatherData(JSONObject weatherUpdate) {    
        if (weatherUpdate.toString().equals("{}")) {
            throw new IllegalArgumentException("Empty JSON object");
        }
    
        if (weatherUpdate.get("id") == null || weatherUpdate.get("id").isEmpty()) {
            throw new IllegalArgumentException("Invalid location ID");
        }
    }    

    @Override
    public String handlePOSTRequest(HTTPRequest httpRequest) {
        return buildResponse(METHOD_NOT_IMPLEMENTED, "POST not supported");
    }

    @Override
    public String handleDELETERequest(HTTPRequest httpRequest) {
        return buildResponse(METHOD_NOT_IMPLEMENTED, "DELETE not supported");
    }

    public static String[] CLI(String[] args) {
        String port;

        if (args.length == 0) {
            System.out.println("No arguments provided\n");
            displayHelp();
            return null;
        }

        if (args.length == 1) {
            if (args[0].equals("--help") || args[0].equals("-h")) {
                displayHelp();
                return null;
            } else if (args[0].equals("--default") || args[0].equals("-d")) {
                port = DEFAULT_PORT;
            } else if (isNumeric(args[0])) {
                port = args[0];
            } else {
                System.out.println("Invalid arguments specified\n");
                displayHelp();
                return null;
            }
        } else {
            System.out.println("Invalid arguments specified\n");
            displayHelp();
            return null;
        }

        return new String[] { port };
    }

    private static void displayHelp() {
        System.out.println("Usage: java AggregationServer [PORT]\n");
        System.out.println("Options:");
        System.out.println("  PORT                     The port number to listen on");
        System.out.println("  --default, -d            Use the default port {" + DEFAULT_PORT + "}");
        System.out.println("  --help, -h               Display this message\n");
        System.out.println("Examples:");
        System.out.println("  java AggregationServer 4567");
        System.out.println("  java AggregationServer -d\n");
        System.out.println("Defaults:");
        System.out.println(String.format("  PORT: %s", DEFAULT_PORT));
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static void main(String[] args) {
        String[] cli_args = CLI(args);
        if (cli_args == null) {
            return;
        }

        try (AggregationServer server = new AggregationServer(new ServerSocket(Integer.parseInt(cli_args[0])))) {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}