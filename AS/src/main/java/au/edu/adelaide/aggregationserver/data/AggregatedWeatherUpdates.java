package au.edu.adelaide.aggregationserver.data;

import static au.edu.adelaide.aggregationserver.AggregationServerConstants.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.LinkedList;
import java.util.List;
import java.util.LinkedHashMap;
import java.time.ZonedDateTime;


public class AggregatedWeatherUpdates implements Serializable {
    private static final long serialVersionUID = 1L;
    public Map<UUID, LinkedList<WeatherUpdate>> contentServerUpdates = new HashMap<>();
    public Map<String, WeatherUpdate> mostRecentUpdatesByStation = new LinkedHashMap<>();

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

    public String getMostRecentUpdateJson(String weatherStationId) {
        if ("recent".equals(weatherStationId)) {
            return getMostRecentWeatherUpdate().weatherData.toJSONString();
        } else {
            return getMostRecentUpdateByStation(weatherStationId).weatherData.toJSONString();
        }
    }

    public boolean isUpdateMoreRecent(WeatherUpdate weatherUpdate) {
        WeatherUpdate mostRecentUpdate = getMostRecentUpdateByStation(weatherUpdate.weatherStationId);
        return mostRecentUpdate == null || weatherUpdate.timestamp.isAfter(mostRecentUpdate.timestamp);
    }

    public void removeStaleContentServer(UUID uuid) {
        // Remove content server
        removeContentServer(uuid);
        // Check the content server UUID from the most recent updates data structure for stale content server
        mostRecentUpdatesByStation.entrySet().removeIf(entry -> entry.getValue().contentServerUUID.equals(uuid));
    }

    private void removeContentServer(UUID contentServerUUID) {
        contentServerUpdates.remove(contentServerUUID);
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

    public List<UUID> getStaleContentServerUUIDs() {
        return this.contentServerUpdates.keySet().stream()
            .filter(this::isContentServerStale)
            .collect(Collectors.toList());
    }

    public boolean isContentServerStale(UUID contentServerUUID) {
        LinkedList<WeatherUpdate> updates = contentServerUpdates.get(contentServerUUID);
        if (updates == null || updates.isEmpty()) {
            return true;
        }
        return updates.getFirst().timestamp.plusSeconds(STALE_DATA_THRESHOLD).isBefore(ZonedDateTime.now());
    }
}
