package CS.test.content_server;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Files.class})
public class ContentServerTest {

    private static final String requestLocation = "content_server/resources/PUTRequest.txt";
    private static final String payloadFileTest = "content_server/resources/TestWeatherData.txt";
    private static final String expectedRequestTemplate = "PUT /weather.json HTTP/1.1\n" + //
                                                          "User-Agent: ATOMClient/1/0\n" + //
                                                          "\n" + //
                                                          "{{payload}}";
    private static final String expectedPayload = "Some sample weather data here.";
    private static final String expectedRequest = expectedRequestTemplate.replace("{{payload}}", expectedPayload);

    @InjectMocks
    ContentServer contentServer;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        
        mockStatic(Files.class);
        when(Files.readAllBytes(Paths.get(requestLocation))).thenReturn(expectedRequestTemplate.getBytes());
        when(Files.readAllBytes(Paths.get(payloadFileTest))).thenReturn(expectedPayload.getBytes());
    }

    @Test
    public void testBuildRequest() throws IOException {
        String result = contentServer.buildRequest(requestLocation, payloadFileTest);
        assertEquals(expectedRequest, result);
    }
}
