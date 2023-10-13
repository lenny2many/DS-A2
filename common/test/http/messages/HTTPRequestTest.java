package http.messages;

import http.messages.HTTPRequest;
import java.util.List;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HTTPRequestTest {

    private HTTPRequest httpMessage;

    @Before
    public void setUp() {
        httpMessage = new HTTPRequest();
    }

    @Test
    public void testGetRequestMethod_ValidInput() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetRequestMethod_ValidInput ---\n");

        try {
            String testRequestMethod = "GET";
            logMessages.add("Setting and retrieving a valid request method:\n" + testRequestMethod + "\n");
            httpMessage.setRequestMethod(testRequestMethod);

            assertEquals(testRequestMethod, httpMessage.getRequestMethod());
            logMessages.add("Test passed: Request method was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Request method was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testGetURI_ValidInput() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetURI_ValidInput ---\n");

        try {
            String testURI = "/index.html";
            logMessages.add("Setting and retrieving a valid URI:\n" + testURI + "\n");
            httpMessage.setURI(testURI);

            assertEquals(testURI, httpMessage.getURI());
            logMessages.add("Test passed: URI was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: URI was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testGetRequestLine_ValidInput() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetRequestLine_ValidInput ---\n");

        try {
            String testRequestMethod = "GET";
            String testURI = "/index.html";
            String testProtocolVersion = "HTTP/1.1";
            logMessages.add("Setting and retrieving a valid request method:\n" + testRequestMethod + "\n");
            httpMessage.setRequestMethod(testRequestMethod);
            logMessages.add("Setting and retrieving a valid URI:\n" + testURI + "\n");
            httpMessage.setURI(testURI);
            logMessages.add("Setting and retrieving a valid protocol version:\n" + testProtocolVersion + "\n");
            httpMessage.setProtocolVersion(testProtocolVersion);

            assertEquals(testRequestMethod + " " + testURI + " " + testProtocolVersion, httpMessage.getRequestLine());
            logMessages.add("Test passed: Request line was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Request line was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testToString_ValidInput() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testToString_ValidInput ---\n");

        try {
            String testRequestMethod = "GET";
            String testURI = "/index.html";
            String testProtocolVersion = "HTTP/1.1";
            logMessages.add("Setting and retrieving a valid request method:\n" + testRequestMethod + "\n");
            httpMessage.setRequestMethod(testRequestMethod);
            logMessages.add("Setting and retrieving a valid URI:\n" + testURI + "\n");
            httpMessage.setURI(testURI);
            logMessages.add("Setting and retrieving a valid protocol version:\n" + testProtocolVersion + "\n");
            httpMessage.setProtocolVersion(testProtocolVersion);

            assertEquals(testRequestMethod + " " + testURI + " " + testProtocolVersion + "\r\n" + httpMessage.headersToString() + "\r\n\r\n" + httpMessage.getBody(), httpMessage.toString());
            logMessages.add("Test passed: HTTP message was converted to string successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: HTTP message was not converted to string successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
}
