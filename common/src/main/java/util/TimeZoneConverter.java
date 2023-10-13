package util;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeZoneConverter {

    public static ZonedDateTime convertToUTC(String localDateTimeStr, String timeZoneStr) {
        // Assuming the provided local_date_time_full format is "yyyyMMddHHmmss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Convert the timeZoneStr to a ZoneId
        ZoneId zoneId;
        switch (timeZoneStr) {
            case "GMT":
                zoneId = ZoneId.of("GMT");
                break;
            // Add more cases if needed
            default:
                zoneId = ZoneId.systemDefault(); // default to system timezone if unknown
                break;
        }

        // Parse the localDateTimeStr into a ZonedDateTime
        ZonedDateTime localDateTime = ZonedDateTime.parse(localDateTimeStr, formatter.withZone(zoneId));

        // Convert the local date-time to UTC
        return localDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    }
}