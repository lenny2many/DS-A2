package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GETClient {
    private final Socket socket;

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4567;
    private static final String GETRequest = "client/resources/GETRequest.txt";

    public GETClient(Socket socket) {
        this.socket = socket;
    }

    public String makeRequest() throws IOException {

        try {
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String string = String.join("\n", Files.readAllLines(Paths.get(GETRequest))) + "\n\n";
            System.out.println("Sending request:\n" + string);
            out.write(string, 0, string.length());
            out.flush();
            
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    public static void main(String[] args) {
 
    }
}
