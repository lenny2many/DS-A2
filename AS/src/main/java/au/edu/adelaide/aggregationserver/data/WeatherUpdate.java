package au.edu.adelaide.aggregationserver.data;

import java.io.Serializable;
import java.util.UUID;
import java.time.ZonedDateTime;

import util.JSONObject;
import util.TimeZoneConverter;


public class WeatherUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    public UUID contentServerUUID;
    public String weatherStationId;
    public JSONObject weatherData;
    public ZonedDateTime timestamp;

    public WeatherUpdate(UUID contentServerUUID, JSONObject weatherData) {
        this.contentServerUUID = contentServerUUID;
        this.weatherStationId = weatherData.get("id");
        this.weatherData = weatherData;

        // Convert the local date-time to UTC
        this.timestamp = TimeZoneConverter.convertToUTC(weatherData.get("local_date_time_full"), weatherData.get("time_zone"));
    }
}
