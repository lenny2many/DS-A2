package au.edu.adelaide.aggregationserver;

import au.edu.adelaide.aggregationserver.data.AggregatedWeatherUpdates;
import au.edu.adelaide.aggregationserver.data.WeatherUpdate;
import au.edu.adelaide.aggregationserver.requesthandlers.HTTPRequestHandler;

import util.CLI;
import util.LamportClock;

import static au.edu.adelaide.aggregationserver.AggregationServerConstants.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents an Aggregation Server which manages weather updates and
 * provides an HTTP interface to interact with the system.
 */
public class AggregationServer {
    private AggregatedWeatherUpdates aggregatedWeatherUpdates;
    private DataManager dataManager;
    public LamportClock lamportClock;
    private static final Logger logger = Logger.getLogger(AggregationServer.class.getName());

    /**
     * Initializes a new instance of the Aggregation Server.
     */
    public AggregationServer() {
        this.dataManager = new DataManager();
        initialiseResources();
    }

    /**
     * Initialises resources required for the Aggregation Server.
     */
    private void initialiseResources() {
        this.aggregatedWeatherUpdates = new AggregatedWeatherUpdates();
        dataManager.loadDataForAggregationServer(this.aggregatedWeatherUpdates);
        dataManager.startCleanupScheduler(this.aggregatedWeatherUpdates);
        this.lamportClock = new LamportClock();
    }
    
    /**
     * Adds a new weather update and persists it.
     *
     * @param weatherUpdate The new weather update.
     * @throws IOException if an I/O error occurs during persistence.
     */
    public void addWeatherUpdate(WeatherUpdate weatherUpdate) throws IOException {
        aggregatedWeatherUpdates.addUpdate(weatherUpdate, weatherUpdate.weatherStationId);
        dataManager.persistNewestData(this.aggregatedWeatherUpdates, weatherUpdate.contentServerUUID);
    }

    /**
     * Checks if the provided weather update is more recent than the last update.
     *
     * @param weatherUpdate The weather update to check.
     * @return {@code true} if the update is more recent, otherwise {@code false}.
     */
    public boolean isUpdateMoreRecent(WeatherUpdate weatherUpdate) {
        return aggregatedWeatherUpdates.isUpdateMoreRecent(weatherUpdate);
    }

    /**
     * Retrieves the most recent weather update as a JSON string for a given station.
     *
     * @param weatherStationId The ID of the weather station.
     * @return A JSON string representing the most recent update.
     */
    public String getMostRecentUpdateJson(String weatherStationId) {
        return aggregatedWeatherUpdates.getMostRecentUpdateJson(weatherStationId);
    }

    /**
     * Removes stale content server data and its associated data file.
     *
     * @param uuid The UUID of the content server.
     */
    public void removeStaleContentServerData(UUID uuid) {
        aggregatedWeatherUpdates.removeStaleContentServer(uuid);
        dataManager.deleteContentServerDataFile(uuid);
    }

    /**
     * Updates the timestamp for a content server to prevent its data from being considered stale.
     *
     * @param uuid The UUID of the content server.
     */
    public void updateContentServerTimestamp(UUID uuid) {
        aggregatedWeatherUpdates.updateContentServerTimestamp(uuid);
    }

    /**
     * Checks if a new file was created during the last data persistence.
     *
     * @return {@code true} if a new file was created, otherwise {@code false}.
     */
    public boolean checkNewFileStatus() {
        return dataManager.wasNewFileCreated();
    }

    /**
     * The entry point of the program.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        CLI cli = CLI.initialiseCLI(BASE_STORAGE_PATH + PROGRAM_HELP_FILE_PATH);
        Map<String, String> argMap = cli.parseCLIArguments(args);
        if (argMap != null) {
            startAggregationServer(argMap);
        }
    }

    /**
     * Starts the Aggregation Server using specified arguments.
     *
     * @param argMap A map of command-line arguments.
     */
    private static void startAggregationServer(Map<String, String> argMap) {
        Integer port = Integer.parseInt(argMap.getOrDefault("port", DEFAULT_PORT));
        AggregationServer aggregationServer = new AggregationServer();
        try (ServerSocket serverSocket = new ServerSocket(port);
             HTTPRequestHandler httpRequestHandler = new HTTPRequestHandler(serverSocket, aggregationServer)) {
                
                httpRequestHandler.run();
        } catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE, "Invalid port number", nfe);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Failed to initialize server socket", ioe);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred", e);
        }
    }
}