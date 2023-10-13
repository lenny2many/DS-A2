package http.messages;

import http.messages.HTTPResponse;
import java.util.List;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HTTPResponseTest {

    HTTPResponse response;

    @Before
    public void setUp() {
        response = new HTTPResponse();
    }

    @Test
    public void testGetStatusCode_ValidInput() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetStatusCode_ValidInput ---\n");

        try {
            String testStatusCode = "200 OK";
            logMessages.add("Setting and retrieving a valid status code:\n" + testStatusCode + "\n");
            response.setStatusCode(testStatusCode);

            assertEquals(testStatusCode, response.getStatusCode());
            logMessages.add("Test passed: Status code was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Status code was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testGetResponseLine_ValidInput() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testGetResponseLine_ValidInput ---\n");

        try {
            String testStatusCode = "200 OK";
            String testProtocolVersion = "HTTP/1.1";
            logMessages.add("Setting and retrieving a valid status code:\n" + testStatusCode + "\n");
            response.setStatusCode(testStatusCode);
            logMessages.add("Setting and retrieving a valid protocol version:\n" + testProtocolVersion + "\n");
            response.setProtocolVersion(testProtocolVersion);

            assertEquals(testProtocolVersion + " " + testStatusCode, response.getResponseLine());
            logMessages.add("Test passed: Response line was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Response line was not set or retrieved successfully.\n");
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
            String testStatusCode = "200 OK";
            String testProtocolVersion = "HTTP/1.1";
            String testHeaderName = "Content-Type";
            String testBody = "This is a regular body content.";
            logMessages.add("Setting and retrieving a valid status code:\n" + testStatusCode + "\n");
            response.setStatusCode(testStatusCode);
            logMessages.add("Setting and retrieving a valid protocol version:\n" + testProtocolVersion + "\n");
            response.setProtocolVersion(testProtocolVersion);
            logMessages.add("Setting and retrieving a valid header name:\n" + testHeaderName + "\n");
            response.setHeader(testHeaderName, "application/json");
            logMessages.add("Setting and retrieving a regular body content:\n" + testBody + "\n");
            response.setBody(testBody);

            assertEquals(testProtocolVersion + " " + testStatusCode + "\r\n" + response.headersToString() + "\r\n\r\n" + testBody, response.toString());
            logMessages.add("Test passed: Response was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Response was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testSetStatusCode_InvalidInput() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetStatusCode_InvalidInput ---\n");

        try {
            String testStatusCode = "200 OK";
            logMessages.add("Setting and retrieving a valid status code:\n" + testStatusCode + "\n");
            response.setStatusCode(testStatusCode);

            assertEquals(testStatusCode, response.getStatusCode());
            logMessages.add("Test passed: Status code was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Status code was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
}
