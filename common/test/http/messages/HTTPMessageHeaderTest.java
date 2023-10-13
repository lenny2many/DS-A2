package http.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HTTPMessageHeaderTest {

    private HTTPMessage httpMessage;

    private static final Logger logger = Logger.getLogger(HTTPMessage.class.getName());

    @Before
    public void setUp() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.OFF);

        httpMessage = new HTTPMessage();
    }

    // HTTPMessage::setHeader(String headerName, String headerValue)

    @Test
    public void testSetHeader_ValidHeader() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetHeader_ValidHeader ---\n");

        try {
            String headerName = "Content-Type";
            String headerValue = "application/json";
            logMessages.add("Setting a valid header:\n" + headerName + ": " + headerValue + "\n");
            httpMessage.setHeader(headerName, headerValue);

            assertEquals(headerValue, httpMessage.getHeader(headerName));
            logMessages.add("Test passed: Header was set successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Header was not set successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    public void testSetHeader_InvalidHeaderValue() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetHeader_InvalidHeaderValue ---\n");

        try {
            String headerName = "Content-Length";
            String invalidHeaderValue = "-100";
            logMessages.add("Setting an invalid header value:\n" + headerName + ": " + invalidHeaderValue + "\n");
            httpMessage.setHeader(headerName, invalidHeaderValue);
        } catch (IllegalArgumentException e) {
            logMessages.add("Test passed: IllegalArgumentException was thrown as expected.\n");
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    public void testSetHeader_NullHeaderName() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetHeader_NullHeaderName ---\n");

        try {
            logMessages.add("Setting a null header name.\n");
            httpMessage.setHeader(null, "some value");
            
            assertNull(httpMessage.getHeader(null));
            logMessages.add("Test passed: Null header was not set.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Null header was set.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testSetHeader_UnsupportedHeader() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetHeader_UnsupportedHeader ---\n");

        try {
            String unsupportedHeaderName = "Unsupported-Header";
            String headerValue = "some value";
            logMessages.add("Setting an unsupported header:\n" + unsupportedHeaderName + ": " + headerValue + "\n");
            httpMessage.setHeader(unsupportedHeaderName, headerValue);

            assertNull(httpMessage.getHeader(unsupportedHeaderName));
            logMessages.add("Test passed: Unsupported header was not set.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Unsupported header was set.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    // HTTPMessage::getHeader(String headerName) & HTTPMessage::getHeaders()

    @Test
    public void testGetHeader_ExistingHeader() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetHeader_ExistingHeader ---\n");

        try {
            String headerName = "Content-Type";
            String headerValue = "application/json";
            httpMessage.setHeader(headerName, headerValue);

            assertEquals(headerValue, httpMessage.getHeader(headerName));
            logMessages.add("Test passed: Header was retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Header was not retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testGetHeader_NonExistingHeader() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetHeader_NonExistingHeader ---\n");

        try {
            String headerName = "Content-Type";
            httpMessage.getHeader(headerName);
            assertNull(httpMessage.getHeader(headerName));
            logMessages.add("Test passed: Received null as expected");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Header was retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testGetHeaders_AllHeaders() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetHeaders_AllHeaders ---\n");

        httpMessage.setHeader("Content-Type", "application/json");
        httpMessage.setHeader("Content-Length", "100");

        try {
            Map<String, String> headers = httpMessage.getHeaders();

            assertEquals(2, headers.size());
            assertEquals("application/json", headers.get("Content-Type"));
            assertEquals("100", headers.get("Content-Length"));
            logMessages.add("Test passed: All headers were retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: All headers were not retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testGetHeaders_NoHeaders() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetHeaders_NoHeaders ---\n");

        try {
            Map<String, String> headers = httpMessage.getHeaders();

            assertEquals(0, headers.size());
            logMessages.add("Test passed: No headers were retrieved as expected.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Headers were retrieved unexpectedly.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
}
