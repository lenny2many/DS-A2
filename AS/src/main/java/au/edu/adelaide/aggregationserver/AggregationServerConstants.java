package au.edu.adelaide.aggregationserver;

public class AggregationServerConstants {

    // AGGREGATION SERVER DEFAULTS
    public static final String DEFAULT_PORT = "4567";
    public static final String URI_PREFIX = "/data/";
    public static final String BASE_STORAGE_PATH = "src/main/resources/";
    public static final String FILE_EXTENSION = ".weather";
    public static final String STATION_FILE = BASE_STORAGE_PATH + URI_PREFIX + "weatherUpdatesByStation" + FILE_EXTENSION;
    public static final String PROGRAM_HELP_FILE_PATH = "help/AggregationServerHelp.txt";
    public static final Integer MAX_CONTENT_SERVER_UPDATES = 20;
    public static final Integer CLEANUP_SCHEDULE_INTERVAL = 15; // seconds
    public static final Integer STALE_DATA_THRESHOLD = 30; // seconds
    // AGGREGATION HTTP STATUS CODES
    public static final String INTERNAL_SERVER_ERROR_STATUS_CODE = "500 Internal server error";
    public static final String METHOD_NOT_IMPLEMENTED_STATUS_CODE = "400 Method not implemented";
    public static final String EMPTY_REQUEST_BODY_STATUS_CODE = "204 Empty Request Body";
    public static final String HTTP_CREATED_STATUS_CODE = "201 HTTP_CREATED";
    public static final String OK_STATUS_CODE = "200 OK";
    // AGGREGATION SERVER RESPONSES
    public static final String PUT_RESPONSE = "Aggregation Server successfully received PUT request at ";
    public static final String POST_RESPONSE = "Aggregation Server does not support POST requests";
    public static final String DELETE_RESPONSE = "Aggregation Server does not support DELETE requests";
    public static final String ERROR_RESPONSE = "Aggregation Server failed to process request at ";

}
