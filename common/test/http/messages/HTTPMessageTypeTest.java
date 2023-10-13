package http.messages;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HTTPMessageTypeTest {

    private HTTPMessage httpMessage;
    
    @Before
    public void setUp() {
        httpMessage = new HTTPMessage();
    }

    @After
    public void tearDown() {
        // additional cleanup if needed
    }

    // DETERMINEMESSAGETYPE TESTS

    @Test
    public void testDetermineMessageType_ValidRequestLine() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testDetermineMessageType_ValidRequestLine ---\n");

        String validRequestLine = "GET /resource HTTP/1.1";

        try {
            logMessages.add("Attempting to determine message type with a valid request line:\n" + validRequestLine + "\n");
            HTTPMessage determinedMessage = httpMessage.determineMessageType(validRequestLine);

            assertTrue(determinedMessage instanceof HTTPRequest);
            logMessages.add("Test passed: Message was determined as HTTPRequest successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Message was not determined as HTTPRequest.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testDetermineMessageType_ValidResponseLine() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testDetermineMessageType_ValidResponseLine ---\n");

        String validResponseLine = "HTTP/1.1 200 OK";

        try {
            logMessages.add("Attempting to determine message type with a valid response line:\n" + validResponseLine + "\n");
            HTTPMessage determinedMessage = httpMessage.determineMessageType(validResponseLine);

            assertTrue(determinedMessage instanceof HTTPResponse);
            logMessages.add("Test passed: Message was determined as HTTPResponse successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Message was not determined as HTTPResponse.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDetermineMessageType_InvalidFirstLine() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testDetermineMessageType_InvalidFirstLine ---\n");

        String invalidFirstLine = "INVALID LINE FORMAT";

        try {
            logMessages.add("Attempting to determine message type with an invalid first line:\n" + invalidFirstLine + "\n");
            httpMessage.determineMessageType(invalidFirstLine);
        } catch (IllegalArgumentException e) {
            logMessages.add("Test passed: IllegalArgumentException was thrown as expected.\n");
            throw e;
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
}
