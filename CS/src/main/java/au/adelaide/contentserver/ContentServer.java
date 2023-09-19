package au.adelaide.contentserver;

import common.util.CLI;
import common.http.HTTPClient;
import common.http.messages.HTTPResponse;
import common.util.IOUtility;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import static au.adelaide.contentserver.ContentServerConstants.*;

public class ContentServer extends HTTPClient {
    

    private UUID uuid;

    public ContentServer(Socket socket) throws IOException {
        super(socket);
        uuid = UUID.randomUUID();
    }

    @Override
    protected String buildRequest(String request_file, String... payload_file) throws IOException {
        String request = IOUtility.readTxtFile(request_file);
        if (payload_file == null || payload_file.length == 0) return request;

        String payload = IOUtility.readTxtFile(payload_file[0]);
        return request.replace("{{UUID}}", uuid.toString())
                      .replace("{{payload_length}}", String.valueOf(payload.length()))
                      .replace("{{payload}}", payload);
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
            ContentServer contentServer = new ContentServer(socket)) {
            contentServer.sendHTTPRequest(PUT_REQUEST_FILE, weatherDataFilePath, RESOURCE_PATH + "London.txt");
        }
    }
}
