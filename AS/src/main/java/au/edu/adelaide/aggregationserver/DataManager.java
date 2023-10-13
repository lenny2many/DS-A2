package au.edu.adelaide.aggregationserver;

import static au.edu.adelaide.aggregationserver.AggregationServerConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.edu.adelaide.aggregationserver.data.AggregatedWeatherUpdates;
import au.edu.adelaide.aggregationserver.data.WeatherUpdate;
import util.IOUtility;

/**
 * Manages data related operations for the AggregationServer, such as loading, persisting, and cleanup tasks.
 */
public class DataManager {
    private static final Logger LOGGER = Logger.getLogger(DataManager.class.getName());
    private IOUtility ioUtility;
    private boolean fileCreated = false;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DataManager() {
        this.ioUtility = new IOUtility();
    }

    /**
     * Constructs a DataManager instance.
     * @param ioUtility An IOUtility instance for performing IO operations.
     */
    public DataManager(IOUtility ioUtility) {
        this.ioUtility = ioUtility;
    }

    /**
     * Loads data for the AggregationServer.
     * @param aggregatedWeatherUpdates The container for the loaded data.
     */
    public void loadDataForAggregationServer(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        loadContentServerData(aggregatedWeatherUpdates);
        loadStationData(aggregatedWeatherUpdates);
    }
    
    /**
     * Starts services for the AggregationServer.
     * @param aggregatedWeatherUpdates The container for the data.
     */
    public void startCleanupScheduler(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        scheduler.scheduleAtFixedRate(() -> runCleanupTask(aggregatedWeatherUpdates), CLEANUP_SCHEDULE_DELAY, CLEANUP_SCHEDULE_INTERVAL, TimeUnit.SECONDS);
    }
    
    /**
     * Persists the newest data for a content server.
     *
     * @param aggregatedWeatherUpdates Data that needs to be persisted.
     * @param contentServerUUID        The UUID of the content server.
     * @param ioUtility                The utility to perform IO operations.
     * @throws IOException If an I/O error occurs.
     */
    public void persistNewestData(AggregatedWeatherUpdates aggregatedWeatherUpdates, UUID contentServerUUID) throws IOException {
        String serverSpecificFile = buildFilePath(contentServerUUID);
        
        fileCreated = ioUtility.createFileIfNotExists(serverSpecificFile);

        // Persist the newest data for the content server.
        ioUtility.saveToFile(aggregatedWeatherUpdates.contentServerUpdates.get(contentServerUUID), serverSpecificFile);
        ioUtility.saveToFile(aggregatedWeatherUpdates.mostRecentUpdatesByStation, STATION_FILE);
    }

    /**
     * Deletes the data file for a given content server.
     * @param contentServerUUID The UUID of the content server.
     */
    public void deleteContentServerDataFile(UUID contentServerUUID) {
        String filePath = BASE_STORAGE_PATH + URI_PREFIX + contentServerUUID + FILE_EXTENSION;
        try {
            ioUtility.deleteFile(filePath);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "An error occurred while deleting file: " + filePath, e);
        }
    }

    private void loadStationData(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        try {
            Object loadedData = ioUtility.loadFromFile(STATION_FILE);
            if(loadedData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, WeatherUpdate> stationUpdates = (Map<String, WeatherUpdate>) loadedData;
                Optional.ofNullable(stationUpdates)
                        .ifPresent(updates -> aggregatedWeatherUpdates.mostRecentUpdatesByStation = updates);
            } else {
                LOGGER.log(Level.WARNING, "Unexpected data type loaded from file: " + STATION_FILE);
            }
        } catch (FileNotFoundException fnf) {
            LOGGER.log(Level.INFO, "No station data found. Skipping load.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "An unexpected error occurred while loading station data: " + STATION_FILE);
        }
    }

    private void loadContentServerData(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        File serverDataDir = new File(BASE_STORAGE_PATH + URI_PREFIX);
        if (serverDataDir.exists() && serverDataDir.isDirectory()) {
            Optional.ofNullable(serverDataDir.listFiles())
                    .ifPresent(files -> processFiles(files, aggregatedWeatherUpdates));
        }
    }

    private void processFiles(File[] files, AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        for (File file : files) {
            String uuidStr = file.getName().replace(FILE_EXTENSION, "");
            try {
                UUID serverUuid = UUID.fromString(uuidStr);
                Object loadedData = ioUtility.loadFromFile(file.getAbsolutePath());
                
                if(loadedData instanceof LinkedList) {
                    @SuppressWarnings("unchecked")
                    LinkedList<WeatherUpdate> updates = (LinkedList<WeatherUpdate>) loadedData;
                    Optional.ofNullable(updates)
                            .ifPresent(updateList -> {
                                aggregatedWeatherUpdates.contentServerUpdates.put(serverUuid, updateList);
                                LOGGER.log(Level.INFO, "Loaded content server data for: " + serverUuid);
                            });
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected data type loaded from file: " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Skipping file with invalid UUID: " + file.getName());
            }
        }
    }

    private void runCleanupTask(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        LOGGER.log(Level.INFO, "Running cleanup task");
        try {
            removeStaleContentServers(aggregatedWeatherUpdates);
            clearStationDataIfNeeded(aggregatedWeatherUpdates);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred during the cleanup task.", e);
        }
    }
    
    private List<UUID> removeStaleContentServers(AggregatedWeatherUpdates aggregatedWeatherUpdates) {
        List<UUID> staleContentServers = aggregatedWeatherUpdates.getStaleContentServerUUIDs();
    
        for(UUID uuid : staleContentServers) {
            try {
                LOGGER.log(Level.INFO, "Removing stale content server data for: " + uuid);
                aggregatedWeatherUpdates.removeStaleContentServer(uuid);
                ioUtility.deleteFile(buildFilePath(uuid));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "An error occurred while removing stale content server data for: " + uuid, e);
            }
        }
        
        return staleContentServers;
    }
    
    private void clearStationDataIfNeeded(AggregatedWeatherUpdates aggregatedWeatherUpdates) throws IOException {
        if (Files.exists(Paths.get(STATION_FILE)) && aggregatedWeatherUpdates.mostRecentUpdatesByStation.isEmpty()) {
            LOGGER.log(Level.INFO, "No content servers exist. Clearing station data");
            ioUtility.deleteFile(STATION_FILE);
        }
    }

    /**
     * Builds the path for specific server file.
     *
     * @param contentServerUUID UUID of the content server.
     * @return Path as a string.
     */
    private String buildFilePath(UUID contentServerUUID) {
        return BASE_STORAGE_PATH + URI_PREFIX + contentServerUUID + FILE_EXTENSION;
    }

    public boolean wasNewFileCreated() {
        return fileCreated;
    }
}
