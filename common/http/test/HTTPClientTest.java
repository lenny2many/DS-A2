package common.http.test;

import common.http.HTTPClient;
import common.http.HTTPConnection;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.io.IOException;
import java.net.Socket;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({HTTPClient.class, HTTPConnection.class})
public class HTTPClientTest {

    private static class DummyHTTPClient extends HTTPClient {
        @Override
        protected String buildRequest(String request_location, String payload_file) throws IOException {
            return "";
        }
    }

    private DummyHTTPClient testClient;

    @Mock
    Socket mockSocket;

    @Mock
    HTTPConnection mockConn;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        testClient = new DummyHTTPClient();
        whenNew(HTTPConnection.class).withAnyArguments().thenReturn(mockConn);
        when(mockConn.readResponse()).thenReturn("");
    }

    @Test
    public void testRun() throws Exception {
        testClient.run(mockSocket);

        verify(mockConn).sendRequest(anyString());
        verify(mockConn).readResponse();
    }
}

