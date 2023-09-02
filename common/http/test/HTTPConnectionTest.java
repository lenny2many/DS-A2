package common.http.test;

import common.http.HTTPConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HTTPConnection.class)
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
    public void testSendRequest() throws Exception {
        OutputStreamWriter mockOut = Mockito.mock(OutputStreamWriter.class);
        when(mockServerSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        // Whenever a new OutputStreamWriter is created, return the mockOut
        whenNew(OutputStreamWriter.class).withAnyArguments().thenReturn(mockOut);

        try (HTTPConnection conn = new HTTPConnection(mockServerSocket)) {
            conn.sendRequest(mockRequest);
        }
        
        // Verify that the mockOut stub was written to
        verify(mockOut).write(mockRequest, 0, mockRequest.length());
    }

    @Test
    public void testReadResponse() throws IOException {
        when(mockServerSocket.getInputStream()).thenReturn(new ByteArrayInputStream(mockResponse.getBytes()));

        String response;
        try (HTTPConnection conn = new HTTPConnection(mockServerSocket)) {
            response = conn.readResponse();
        }

        assertEquals(mockResponse, response);
    }

    @Test
    public void testReadResponseEmptyResponse() throws IOException {
        String emptyResponse = "";
        when(mockServerSocket.getInputStream()).thenReturn(new ByteArrayInputStream(emptyResponse.getBytes()));

        try (HTTPConnection conn = new HTTPConnection(mockServerSocket)) {
            String response = conn.readResponse();
            assertEquals(emptyResponse, response);
        }
    }

    @Test
    public void testClose() throws IOException {
        try (HTTPConnection conn = new HTTPConnection(mockServerSocket)) {
            // Just open and close
        }
        verify(mockServerSocket, times(1)).close();
    }
}
