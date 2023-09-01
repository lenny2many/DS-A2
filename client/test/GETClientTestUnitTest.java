package client.test;

import client.GETClient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class GETClientTestUnitTest {

    private Socket mockSocket;
    private GETClient client;

    @Before
    public void setUp() throws IOException {
        mockSocket = Mockito.mock(Socket.class);
        client = new GETClient(mockSocket);
    }

    @Test
    public void testGETRequest() throws IOException {
        // Mock the socket's input and output streams
        String mockResponse = "HTTP/1.1 200 OK\r\n\r\nMock server response!";
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(mockResponse.getBytes()));
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // Make the request
        String response = client.makeRequest();

        // Assert the response
        assertTrue(response.contains("Mock server response!"));
    }
}
