package content_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ContentServer {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4567;
    private static final String PUTRequest = "content_server/resources/PUTRequest.txt";
    private static final String payload = "content_server/resources/WeatherData.txt";
    public String request;

    public ContentServer() {
    
    }
    
    public void sendPUTRequest(OutputStreamWriter out) {
        try {
            request = String.join("\n", Files.readAllLines(Paths.get(PUTRequest))) + "\r\n\r\n";
            request = request +  String.join("\n", Files.readAllLines(Paths.get(payload)));

            System.out.println("Sending request:\n" + request);

            out.write(request, 0, request.length());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void readResponse(BufferedReader in) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ContentServer contentServer = new ContentServer();

        try (Socket socket = new Socket(HOST, PORT);
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

            contentServer.sendPUTRequest(out);
            contentServer.readResponse(in);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
