package http.messages;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HTTPMessageBodyTest {

    private HTTPMessage httpMessage;

    @Before
    public void setUp() {
        httpMessage = new HTTPMessage();
    }

    // HTTPMessage::setBody(String body) && HTTPMessage::getBody()

    @Test
    public void testSetAndGetBody_RegularContent() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetAndGetBody_RegularContent ---\n");

        try {
            String testBody = "This is a regular body content.";
            logMessages.add("Setting and retrieving a regular body content:\n" + testBody + "\n");
            httpMessage.setBody(testBody);

            assertEquals(testBody, httpMessage.getBody());
            logMessages.add("Test passed: Body content was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Body content was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testSetAndGetBody_EmptyBody() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetAndGetBody_EmptyBody ---\n");

        try {
            String testBody = "";
            logMessages.add("Setting and retrieving an empty body content:\n" + testBody + "\n");
            httpMessage.setBody(testBody);

            assertEquals(testBody, httpMessage.getBody());
            logMessages.add("Test passed: Empty body content was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Empty body content was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testSetAndGetBody_NullBody() {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSetAndGetBody_NullBody ---\n");

        try {
            logMessages.add("Setting and retrieving a null body content.\n");
            httpMessage.setBody(null);

            assertNull(httpMessage.getBody());
            logMessages.add("Test passed: Null body content was set and retrieved successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Null body content was not set or retrieved successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
}
