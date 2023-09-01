package common.http.test;

import common.http.HTTPConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class HTTPConnectionTest {
    private static final String mockRequest = "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n";
    private static final String mockResponse = "HTTP/1.1 200 OK\r\n\r\nMock server response!";

    private Socket mockServerSocket;

    @Before
    public void setUp() throws IOException {
        // Mock the socket's input and output streams
        mockServerSocket = Mockito.mock(Socket.class);
        when(mockServerSocket.getInputStream()).thenReturn(new ByteArrayInputStream(mockResponse.getBytes()));
        when(mockServerSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    }

    @Test
    public void testSendRequest() throws IOException {
        String response;
        try (HTTPConnection conn = new HTTPConnection(mockServerSocket)) {
            // Make the request
            conn.sendRequest(mockRequest);
            response = conn.readResponse();
        }
        
        assertEquals(response, mockResponse);
    }
}
