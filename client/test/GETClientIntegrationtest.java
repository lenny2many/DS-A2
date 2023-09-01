import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.Socket;

import static org.junit.Assert.assertTrue;

public class GETClientIntegrationTest {

    private static Socket socket;
    private static GETClient client;

    @BeforeClass
    public static void setUp() throws IOException {
        socket = new Socket(GETClient.HOST, GETClient.PORT);
        client = new GETClient(socket);
    }

    @Test
    public void testMakeRequest() throws IOException {
        // Make the request
        String response = client.makeRequest();

        // Assert the response from the real server
        assertTrue(response.contains("HTTP/1.1 200 OK"));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        socket.close();
    }
}
