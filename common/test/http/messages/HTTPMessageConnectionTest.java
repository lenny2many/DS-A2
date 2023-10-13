package http.messages;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HTTPMessageConnectionTest {
    
    private HTTPMessage httpMessage;

    @Before
    public void setUp() {
        httpMessage = new HTTPMessage();
    }
    
    // HTTPMessage::shouldKeepConnectionAlive()
    
    @Test
    public void testShouldKeepConnectionAlive_HTTP10_WithoutKeepAlive() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testShouldKeepConnectionAlive_HTTP10_WithoutKeepAlive ---\n");

        try {
            logMessages.add("Setting protocol to HTTP/1.0 and not setting Connection header.\n");
            httpMessage.setProtocolVersion("HTTP/1.0");
            
            boolean result = httpMessage.shouldKeepConnectionAlive();
            
            assertFalse(result);
            logMessages.add("Test passed: Connection should not be kept alive.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Connection was kept alive unexpectedly.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
    
    @Test
    public void testShouldKeepConnectionAlive_HTTP11_WithClose() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testShouldKeepConnectionAlive_HTTP11_WithClose ---\n");

        try {
            logMessages.add("Setting protocol to HTTP/1.1 and setting Connection header to close.\n");
            httpMessage.setProtocolVersion("HTTP/1.1");
            httpMessage.setHeader("Connection", "close");
            
            boolean result = httpMessage.shouldKeepConnectionAlive();
            
            assertFalse(result);
            logMessages.add("Test passed: Connection should not be kept alive as per the header.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Connection was kept alive unexpectedly.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
    
    @Test
    public void testShouldKeepConnectionAlive_UnspecifiedProtocol() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testShouldKeepConnectionAlive_UnspecifiedProtocol ---\n");

        try {
            logMessages.add("Not setting protocol and not setting Connection header. Should default to HTTP/1.1 and keep-alive.\n");
            // No protocol or Connection header set. Should default to HTTP/1.1 and keep-alive.
            
            boolean result = httpMessage.shouldKeepConnectionAlive();

            assertTrue(result);
            logMessages.add("Test passed: Connection should be kept alive as per the default.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Connection was not kept alive unexpectedly.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
}
