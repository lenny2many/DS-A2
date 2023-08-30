import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 4567;
    private static final String responseCode = "AggregationServer/resources/200Response.txt";
    private static final String payload = "AggregationServer/resources/dummy.json";

    public void serverStart() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started and listening on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"))) {

                    System.out.println("Received a connection from " + clientSocket.getInetAddress());

                    // Sending a 200 OK response
                    String httpResponse = "HTTP/1.1 200 OK\r\n\r\nHello, this is a simple HTTP server!";
                    out.write(httpResponse);
                    out.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
