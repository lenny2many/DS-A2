package aggregation_server;

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
import java.util.Deque;
import java.util.LinkedList;

import common.http.HTTPServer;
import common.http.messages.HTTPRequest;
import common.util.JSONObject;


public class AggregationServer extends HTTPServer {
    private static final String DEFAULT_PORT = "4567";
    private static final String DATA_FILE = "aggregation_server/resources/WeatherData";
    private static final String INTERNAL_SERVER_ERROR = "500 Internal server error";
    private static final String METHOD_NOT_IMPLEMENTED = "400 Method not implemented";
    private static final String EMPTY_REQUEST_BODY = "204 Empty Request Body";
    private static final String HTTP_CREATED = "201 HTTP_CREATED";
    private static final String OK = "200 OK";

    private AggregatedWeatherData aggregatedWeatherData;
    

    private static class AggregatedWeatherData implements Serializable {
        private static final int MAX_UPDATES = 20;
        private Deque<WeatherData> recentUpdates = new LinkedList<>();
         
        private static class WeatherData implements Serializable {
            String weatherData;
            ZonedDateTime lastUpdated;

            public WeatherData(String weatherData, ZonedDateTime lastUpdated) {
                this.lastUpdated = lastUpdated;
                this.weatherData = weatherData;
            }
        }

        public AggregatedWeatherData() {
            try {
                readFromFile();
            } catch (Exception e) {
                System.out.println("Failed to read from file");
                e.printStackTrace();
            }

            if (recentUpdates == null) {
                recentUpdates = new LinkedList<>();
            }
        }

        public void addUpdate(String newData) {
            recentUpdates.addLast(new WeatherData(newData, ZonedDateTime.now()));
    
            if (recentUpdates.size() > MAX_UPDATES) {
                recentUpdates.removeFirst();
            }
        }

        public String getMostRecentUpdate() {
            if (recentUpdates.isEmpty()) {
                return "No data available";
            }
            return recentUpdates.getLast().weatherData;
        }
        
        private void saveUpdatesToFile() throws IOException {
            try (FileOutputStream fileOut = new FileOutputStream(DATA_FILE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(recentUpdates);
            } catch (IOException i) {
                System.out.println("Error writing data to file.");
                throw i;
            }
        }

        @SuppressWarnings("unchecked")
        public void readFromFile() throws IOException, ClassNotFoundException {
            System.out.println("Reading from data file");
            
            try (FileInputStream fileIn = new FileInputStream(DATA_FILE);
                ObjectInputStream in = new ObjectInputStream(fileIn)) {
                
                // Cast the deserialized object back to Deque<WeatherData>
                recentUpdates = (Deque<WeatherData>) in.readObject();
            } catch (IOException i) {
                i.printStackTrace();
            }
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
            return buildGETResponse(weatherUpdate.toJsonString());
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse(INTERNAL_SERVER_ERROR, "Failed to read data");
        }
    }

    @Override
    public String handlePUTRequest(HTTPRequest httpRequest) {
        try {
            aggregatedWeatherData.addUpdate(httpRequest.getBody());

            // checks on body of request for content validation
            if (httpRequest.getBody().isEmpty() || httpRequest.getBody().equals("null")) {
                System.out.println("Empty request body");
                return buildPUTResponse(EMPTY_REQUEST_BODY);
            } 
            
            try {
                JSONObject weatherUpdate = new JSONObject(httpRequest.getBody());
                System.out.println("Body of request was parsed successfully");
                //  check if the json object is empty
                if (httpRequest.getBody().equals("{}")) {
                    System.out.println("Empty JSON object");
                    return buildPUTResponse(EMPTY_REQUEST_BODY);
                } else if (weatherUpdate.get("id") == null || weatherUpdate.get("id").isEmpty()) {
                        System.out.println("Invalid location ID");
                    return buildErrorResponse(INTERNAL_SERVER_ERROR, "Invalid location");
                }
            } catch (Exception e) {
                System.out.println("Failed to parse JSON");
                return buildErrorResponse(INTERNAL_SERVER_ERROR, "Failed to parse JSON");
            }

            Path dataFilePath = Paths.get(DATA_FILE);
            // If file doesn't exist, create and save to it
            if (!Files.exists(dataFilePath)) {
                System.out.println("File does not exist, creating new file");
                aggregatedWeatherData.saveUpdatesToFile();
                return buildPUTResponse(HTTP_CREATED);
            }
    
            return buildPUTResponse(OK);
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse(INTERNAL_SERVER_ERROR, "Failed to write data");
        }
    }

    private String buildGETResponse(String weatherUpdate) {
        return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\n\r\n%s\r\n", weatherUpdate.length(), weatherUpdate);
    }

    private String buildPUTResponse(String statusCode) {
        String httpBody = "Aggregation Server successfully received PUT request at " + ZonedDateTime.now() + "\r\n";
        return String.format("HTTP/1.1 " + statusCode + "\r\nContent-Length: %d\r\n\r\n%s\r\n", httpBody.length(), httpBody);
    }

    private String buildErrorResponse(String statusCode, String message) {
        return "HTTP/1.1 " + statusCode + "\r\nContent-Length:" + message.length() + "\r\n\r\n" + message;
    }

    @Override
    public String handlePOSTRequest(HTTPRequest httpRequest) {
        return buildErrorResponse(METHOD_NOT_IMPLEMENTED, "POST not supported");
    }

    @Override
    public String handleDELETERequest(HTTPRequest httpRequest) {
        return buildErrorResponse(METHOD_NOT_IMPLEMENTED, "DELETE not supported");
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