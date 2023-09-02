package client;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import client.GETClient;

import java.io.IOException;

public class GETClientTest {
    private static final String request_location = "client/resources/GETRequest.txt";
    private static final String expectedRequest = "GET /get HTTP/1.1\n" + //
            "Host: httpbin.org\n" + //
            "User-Agent: SimpleGETClient\n" + //
            "Connection: close\n";

    GETClient clientTest = new GETClient();

    @Before
    public void setUp() {
        // Any setup if required
    }

    @Test
    public void testBuildRequest() throws IOException {
        String result = clientTest.buildRequest(request_location, "");
        assertEquals(expectedRequest, result);
    }
}
