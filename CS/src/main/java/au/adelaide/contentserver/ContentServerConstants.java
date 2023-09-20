package au.adelaide.contentserver;

class ContentServerConstants {
    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT = "4567";
    public static final String RESOURCE_PATH = "src/main/resources/";
    public static final String CONTENT_SERVER_HELP_FILE = RESOURCE_PATH + "help/ContentServerHelp.txt";
    public static final String PUT_REQUEST_FILE = RESOURCE_PATH + "PUTRequest.txt";
    public static final String DEFAULT_WEATHER_DATA_FILE = RESOURCE_PATH + "WeatherData.txt";
    public static final String HEARTBEAT_REQUEST_FILE = RESOURCE_PATH + "HeartbeatRequest.txt";
    public static final String SHUTDOWN_REQUEST_FILE = RESOURCE_PATH + "ShutdownRequest.txt";
    public static final int HEARTBEAT_INTERVAL = 5;
}
