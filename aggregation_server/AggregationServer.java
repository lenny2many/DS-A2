package aggregation_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.time.ZonedDateTime;
import java.util.Deque;
import java.util.LinkedList;

import common.http.HTTPServer;
import common.http.messages.HTTPRequest;
import common.util.JSONObject;


public class AggregationServer extends HTTPServer {
    private static final int PORT = 4567;

    private static final String DATA_FILE = "aggregation_server/resources/WeatherData.txt";
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

        public void writeToFile() throws IOException {
            // Write the recentUpdates queue to the data file
            System.out.println("Writing to data file");

            try (FileOutputStream fileOut = new FileOutputStream(DATA_FILE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(recentUpdates);
            } catch (IOException i) {
                i.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public void readFromFile() throws IOException, ClassNotFoundException {
            // Check if file exists
            File dataFile = new File(DATA_FILE);
            if (!dataFile.exists()) {
                System.out.println("Data file not found.");
                return;
            }
            
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
            return buildErrorResponse("Failed to read data");
        }
    }

    @Override
    public String handlePUTRequest(HTTPRequest httpRequest) {
        try {
            // add the new data and then save to intermediate file for persistence
            aggregatedWeatherData.addUpdate(httpRequest.getBody());
            aggregatedWeatherData.writeToFile();
            return buildPUTResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("Failed to write data");
        }
    }

    private String buildErrorResponse(String message) {
        return "HTTP/1.1 500 Internal Server Error\r\nContent-Length:" + message.length() + "\r\n\r\n" + message;
    }

    private String buildGETResponse(String weatherUpdate) {
        return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\n\r\n%s\r\n", weatherUpdate.length(), weatherUpdate);
    }

    private String buildPUTResponse() {
        String httpBody = "Aggregation Server successfully received PUT request at " + ZonedDateTime.now() + "\r\n";
        return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\n\r\n%s\r\n", httpBody.length(), httpBody);
    }

    public static void main(String[] args) {
        try (AggregationServer server = new AggregationServer(new ServerSocket(PORT))) {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}