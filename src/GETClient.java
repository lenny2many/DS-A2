import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GETClient {

    private static final String HOST = "httpbin.org";
    private static final int PORT = 80;
    private static final String GETRequest = "GETRequest.txt";

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

            String string = String.join("\n", Files.readAllLines(Paths.get(GETRequest))) + "\n\n";
            System.out.println("Sending request:\n" + string);
            out.write(string, 0, string.length());
            out.flush();
            
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
