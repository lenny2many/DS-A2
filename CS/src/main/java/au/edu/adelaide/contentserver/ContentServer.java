package au.edu.adelaide.contentserver;

import util.CLI;
import http.HTTPClient;
import util.IOUtility;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.Writer;

import static au.edu.adelaide.contentserver.ContentServerConstants.*;

public class ContentServer extends HTTPClient {
    private UUID uuid;
    private IOUtility ioUtility;
    private ScheduledExecutorService heartbeatScheduler;

    public ContentServer(Socket socket, Writer out, BufferedReader in) throws IOException {
        super(socket, out, in);
        uuid = UUID.randomUUID();
        ioUtility = new IOUtility();
    }

    @Override
    protected String buildRequest(String request_file, String... payload_file) throws IOException {
        String request = ioUtility.readTxtFile(request_file);
        // Replace uuid
        request = request.replace("{{UUID}}", this.uuid.toString());
        
        // Replace payload if provided
        if (payload_file.length > 0 && payload_file[0] != null) {
            String payload = ioUtility.readTxtFile(payload_file[0]);

            // Replace date in weather data
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String currentTime = LocalDateTime.now().format(dtf);
            payload = payload.replace("{{date}}", currentTime);

            // Replace payload length and payload in request
            request = request.replace("{{payload_length}}", String.valueOf(payload.length()))
                        .replace("{{payload}}", payload);
        }
        
        return request;
    }

    private void startHeartbeat() {
        heartbeatScheduler = Executors.newScheduledThreadPool(1);
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Sending heartbeat\n");
                sendHTTPRequest(HEARTBEAT_REQUEST_FILE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    public void shutdown() {
        try {
            // send a shutdown message to the server
            sendHTTPRequest(SHUTDOWN_REQUEST_FILE);
            // stop the heartbeat if it is running
            if (heartbeatScheduler != null) {
                stopHeartbeat();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopHeartbeat() {
        heartbeatScheduler.shutdown();
        try {
            // wait for tasks to complete, for a maximum of 10 seconds
            if (!heartbeatScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                heartbeatScheduler.shutdownNow(); // cancel currently executing tasks
                // wait again, for a maximum of 10 seconds
                if (!heartbeatScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Executor service did not terminate");
                }
            }
        } catch (InterruptedException e) {
            // on interruption, cancel if not terminated
            heartbeatScheduler.shutdownNow();
            Thread.currentThread().interrupt(); // preserve interrupt status
        }
    }

    public static void main(String[] args) throws IOException {
        CLI cli = CLI.initialiseCLI(CONTENT_SERVER_HELP_FILE);
        Map<String, String> argMap = cli.parseCLIArguments(args);
        if (argMap == null) return;

        String host = argMap.getOrDefault("host", DEFAULT_HOST);
        int port = Integer.parseInt(argMap.getOrDefault("port", DEFAULT_PORT));
        // Socket socket = new Socket(host, port);
        String weatherDataFilePath = argMap.getOrDefault("weather", DEFAULT_WEATHER_DATA_FILE);

        try (Socket socket = new Socket(host, port);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ContentServer contentServer = new ContentServer(socket, out, in)) {
            // contentServer.startHeartbeat();
            contentServer.sendHTTPRequest(PUT_REQUEST_FILE, weatherDataFilePath);
            Thread.sleep(1000);
            contentServer.sendHTTPRequest(PUT_REQUEST_FILE, RESOURCE_PATH + "data/London.txt");
            
            // sleep to see if heartbeat mechanism works
            // Thread.sleep(30000);
            System.out.println("Content server shutting down");
            contentServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
