package http;

import http.HTTPConnection;
import http.messages.HTTPMessage;
import http.messages.HTTPRequest;
import http.messages.HTTPResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.net.ProtocolException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class HTTPConnectionTest {

    private HTTPConnection httpConnection;
    private Socket mockSocket;
    private OutputStreamWriter mockOut;
    private BufferedReader mockIn;

    private static final Logger logger = Logger.getLogger(HTTPConnection.class.getName());

    @Before
    public void setUp() throws IOException {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.OFF);

        mockSocket = mock(Socket.class);
        mockOut = mock(OutputStreamWriter.class);
        mockIn = mock(BufferedReader.class);

        httpConnection = new HTTPConnection(mockSocket, mockOut, mockIn);
    }

    @After
    public void tearDown() throws IOException {
        httpConnection.close();
    }

    // SENDMESSAGE TESTS

    /**
     * Test Name: testSendMessage_ValidMessage
     * Type: Positive test case.
     * Description: Ensure that a valid HTTP message can be sent successfully.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testSendMessage_ValidMessage() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSendMessage_ValidMessage ---\n");
        
        String testMessage = "GET / HTTP/1.1\r\nHost: example.com\r\n\r\n";

        try {
            logMessages.add("Attempting to send a valid message:\n" + testMessage + "\n");
            httpConnection.sendMessage(testMessage);
    
            verify(mockOut).write(testMessage, 0, testMessage.length());
            verify(mockOut).flush();
            logMessages.add("Test passed: Message was sent successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Message was not sent successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    /**
     * Test Name: testSendMessage_NullMessage
     * Type: Negative test case.
     * Description: Check how the method behaves when a null message is provided.
     * @throws IOException if an I/O error occurs.
     */
    @Test(expected = NullPointerException.class)
    public void testSendMessage_NullMessage() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testSendMessage_NullMessage ---\n");

        try {
            logMessages.add("Attempting to send a null message.\n");
            httpConnection.sendMessage(null);
        } catch (NullPointerException e) {
            logMessages.add("Test passed: NullPointerException was thrown as expected.\n");
            throw e;
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    // READFIRSTLINE TESTS

    /**
     * Test Name: testReadMessage_ValidRequest
     * Type: Positive test case.
     * Description: Validate that an HTTP request message is parsed correctly.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_ValidRequest() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_ValidRequest ---\n");

        // Prepare mock behavior
        when(mockIn.readLine()).thenReturn("GET / HTTP/1.1", "Content-Length: 0", "", null);

        try {
            logMessages.add("Attempting to read a valid request message.\n");
            HTTPRequest httpMessage = (HTTPRequest) httpConnection.readMessage();
        
            assertTrue("Expected HTTPRequest, but got: " + httpMessage.getClass().getSimpleName(), httpMessage instanceof HTTPRequest);
            logMessages.add("Test passed: HTTPRequest was parsed correctly.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: " + e.getMessage() + "\n");
            throw e; // rethrow the assertion error to keep the test failure
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    /**
     * Test Name: testReadMessage_ValidResponse
     * Type: Positive test case.
     * Description: Validate that an HTTP response message is parsed correctly.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_ValidResponse() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_ValidResponse ---\n");

        // Prepare mock behavior
        when(mockIn.readLine()).thenReturn(
            "HTTP/1.1 200 OK", 
            "Content-Length: 0",
            "", 
            null);

        try {
            logMessages.add("Attempting to read a valid response message.\n");
            HTTPResponse httpMessage = (HTTPResponse) httpConnection.readMessage();
        
            assertTrue("Expected HTTPResponse, but got: " + httpMessage.getClass().getSimpleName(), httpMessage instanceof HTTPResponse);
            logMessages.add("Test passed: HTTPResponse was parsed correctly.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: " + e.getMessage() + "\n");
            throw e; // rethrow the assertion error to keep the test failure
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    /**
     * Test Name: testReadMessage_NullFirstLine
     * Description: When the start line is null, validate that the method behaves as expected, likely throwing an IOException.
     * @throws IOException if an I/O error occurs.
     */
    @Test(expected = IOException.class) 
    public void testReadMessage_NullFirstLine() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_NullFirstLine ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(null);

        try {
            logMessages.add("Attempting to read a null first line.\n");
            httpConnection.readMessage();
        } catch (IOException e) {
            logMessages.add("Test passed: IOException was thrown as expected.\n");
            throw e;
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    /**
     * Test Name: testReadMessage_EmptyFirstLine
     * Description: Ensure that when the start line is empty, the method behaves as expected, likely treating it as an invalid request.
     * @throws IOException if an I/O error occurs.
     */
    @Test 
    public void testReadMessage_EmptyFirstLine() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_EmptyFirstLine ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn("");

        try {
            logMessages.add("Attempting to read an empty first line.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
            assertNull("HTTPMessage should be null.", httpMessage);
            logMessages.add("Test passed: HTTPMessage was null as expected.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: " + e.getMessage() + "\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    // READHEADERS TESTS

    /**
     * Test Name: testReadMessage_ValidHeaders
     * Type: Positive test case.
     * Description: Ensure that readMessage can parse valid headers through readHeaders.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_ValidHeaders() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_ValidHeaders ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "Content-Length: 0",
            "Content-Type: application/json",
            "",
            null                
        );

        try {
            logMessages.add("Attempting to read valid headers.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
        
            assertEquals("Content-Length header does not match expected value.", "0", httpMessage.getHeader("Content-Length"));
            assertEquals("Content-Type header does not match expected value.", "application/json", httpMessage.getHeader("Content-Type"));
            logMessages.add("Test passed: Parsed HTTPMessage correctly for valid headers.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: " + e.getMessage() + "\n");
            throw e; // rethrow the assertion error to keep the test failure
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    /**
     * Test Name: testReadMessage_NoHeaders
     * Type: Positive test case.
     * Description: Ensure readMessage can handle cases with no headers and doesn’t throw unexpected exceptions.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_NoHeaders() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_NoHeaders ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "",
            null
        );

        try {
            logMessages.add("Attempting to read no headers.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
            logMessages.add("Test passed: Parsed HTTPMessage correctly for no headers.\n");
        } catch (ProtocolException e) {
            logMessages.add("Test passed: ProtocolException was thrown as expected.\n");
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    /**
     * Test Name: testReadMessage_MalformedHeaders
     * Type: Negative test case.
     * Description: Ensure readMessage gracefully handles malformed headers and logs/ignores them as per implementation.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_MalformedHeaders() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_MalformedHeaders ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "Content-Length: 0",
            "Content-Type: application/json",
            "MalformedHeader",
            "",
            null
        );

        try {
            logMessages.add("Attempting to read malformed headers.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
            logMessages.add("Test failed: No exception was thrown.\n");
            throw new AssertionError("No exception was thrown.");
        } catch (IllegalArgumentException e) {
            logMessages.add("Test passed: IllegalArgumentException was thrown as expected.\n");
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    // READBODY TESTS

    /**
     * Test Name: testReadMessage_ValidMessageWithContent
     * Description: Validate that readMessage can handle a message with valid headers and content.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_ValidMessageWithContent() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_ValidMessageWithContent ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "Content-Length: 10",
            "",
            null
        );
        
        when(mockIn.read(any(char[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            char[] buffer = invocation.getArgument(0);
            int offset = invocation.getArgument(1);
            int length = invocation.getArgument(2);
            
            char[] responseData = "1234567890".toCharArray();
            System.arraycopy(responseData, 0, buffer, offset, length);
            
            return length;
        }).thenReturn(-1);
        

        try {
            logMessages.add("Attempting to read a valid message with content.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
        
            assertEquals("Body does not match expected data.", "1234567890", httpMessage.getBody());
            logMessages.add("Test passed: Parsed HTTPMessage correctly for valid headers and content.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: " + e.getMessage() + "\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }


    /**
     * Test Name: testReadMessage_ValidMessageNoContent
     * Description: Validate that readMessage correctly handles a message with a Content-Length of 0.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_ValidMessageNoContent() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_ValidMessageNoContent ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "Content-Length: 0",
            "",
            "",
            null
        );

        try {
            logMessages.add("Attempting to read a valid message with no content.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
        
            assertEquals("Body does not match expected data.", "", httpMessage.getBody());
            logMessages.add("Test passed: Parsed HTTPMessage correctly for valid headers and no content.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: " + e.getMessage() + "\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }


    /**
     * Test Name: testReadMessage_ValidMessageNoContentLengthHeader
     * Description: Validate readMessage's handling of a message without a Content-Length header.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_ValidMessageNoContentLengthHeader() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_ValidMessageNoContentLengthHeader ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "",
            null
        );

        when(mockIn.read(any(char[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            char[] buffer = invocation.getArgument(0);
            int offset = invocation.getArgument(1);
            int length = invocation.getArgument(2);
            
            char[] responseData = "1234567890".toCharArray();
            System.arraycopy(responseData, 0, buffer, offset, length);
            
            return length;
        }).thenReturn(-1);

        try {
            logMessages.add("Attempting to read a valid message with no Content-Length header.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
            throw new AssertionError("No exception was thrown.");
        } catch (ProtocolException e) {
            logMessages.add("Test passed: ProtocolException was thrown as expected.\n");
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }


    // READBODYDATA TESTS

    /**
     * Test Name: testReadMessage_CorrectContentLength
     * Description: Validate that readMessage correctly handles data reading and sets the body content accurately.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_CorrectContentLength() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_CorrectContentLength ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "Content-Length: 10",
            "",
            "1234567890",
            null
        );
        
        when(mockIn.read(any(char[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            char[] buffer = invocation.getArgument(0);
            int off = invocation.getArgument(1);
            int len = invocation.getArgument(2);

            // Mimicking the behavior of a real InputStream to fill the buffer
            new StringReader("1234567890").read(buffer, off, len);
            
            return 10;
        }).thenReturn(-1);

        try {
            logMessages.add("Attempting to read a valid message with correct Content-Length.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
            
            // Validate body
            assertEquals("Body does not match expected data.", "1234567890", httpMessage.getBody());

            // Validate Content-Length header and body length consistency
            String contentLengthHeader = httpMessage.getHeader("Content-Length");
            assertNotNull("Content-Length header should not be null.", contentLengthHeader);
            assertEquals("Content-Length header should be equal to the actual body length.", 
                httpMessage.getBody().length(), 
                Integer.parseInt(contentLengthHeader)
            );

            logMessages.add("Test passed: Parsed HTTPMessage correctly for valid headers and content.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: " + e.getMessage() + "\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    /**
     * Test Name: testReadMessage_EndOfStream
     * Description: Validate that readMessage can handle a premature end of the stream and sets body content accordingly.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testReadMessage_EndOfStream() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testReadMessage_EndOfStream ---\n");

        // Arrange
        when(mockIn.readLine()).thenReturn(
            "GET / HTTP/1.1",
            "Content-Length: 10",
            "",
            null
        );
        
        when(mockIn.read(any(char[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            char[] buffer = invocation.getArgument(0);
            int off = invocation.getArgument(1);
            int len = invocation.getArgument(2);

            // Mimicking the behavior of a real InputStream to fill the buffer
            new StringReader("12345").read(buffer, off, len);
            
            return 5;
        }).thenReturn(-1);

        try {
            logMessages.add("Attempting to read a valid message with premature end of stream.\n");
            HTTPMessage httpMessage = httpConnection.readMessage();
            throw new AssertionError("No exception was thrown.");
        } catch (IOException e) {
            logMessages.add("Test passed: IOException was thrown as expected.\n");
        } catch (Exception e) {
            logMessages.add("Test failed: Unexpected exception was thrown.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }

    @Test
    public void testClose() throws IOException {
        List<String> logMessages = new ArrayList<>();
        logMessages.add("\n--- TEST: testClose ---\n");

        try {
            logMessages.add("Attempting to close the connection.\n");
            httpConnection.close();
            verify(mockSocket).close();
            logMessages.add("Test passed: Connection was closed successfully.\n");
        } catch (AssertionError e) {
            logMessages.add("Test failed: Connection was not closed successfully.\n");
            throw e;
        } finally {
            logMessages.forEach(System.out::println);
        }
    }
}
