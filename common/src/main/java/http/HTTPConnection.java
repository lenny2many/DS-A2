package http;

import http.messages.HTTPMessage;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.ProtocolException;

/**
 * Manages an HTTP connection, enabling the sending and receiving of HTTP messages.
 */
public class HTTPConnection implements AutoCloseable {
    protected Socket socket;
    protected Writer out;
    protected BufferedReader in;

    private static final Logger logger = Logger.getLogger(HTTPConnection.class.getName());

    /**
     * Constructs an HTTPConnection.
     *
     * @param socket The socket to be used for the connection.
     * @throws IOException if an I/O error occurs when creating the input and output streams, or if the socket is closed.
     */
    public HTTPConnection(Socket socket, Writer out, BufferedReader in) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    /**
     * Sends a message over the HTTP connection.
     *
     * @param message The message to be sent.
     * @throws IOException if an I/O error occurs.
     */
    public void sendMessage(String message) throws IOException {
        out.write(message, 0, message.length());
        out.flush();
    }

    /**
     * Reads an HTTP message from the connection.
     *
     * @return An HTTPMessage object containing the data read from the connection.
     * @throws IOException if an I/O error occurs or if the connection is unexpectedly closed.
     */
    public HTTPMessage readMessage() throws IOException {
        HTTPMessage httpMessage = new HTTPMessage();
        httpMessage = readFirstLine(httpMessage);
        if (httpMessage == null) {
            return null;
        }
        httpMessage = readHeaders(httpMessage);
        httpMessage = readBody(httpMessage);
        
        return httpMessage;
    }


    private HTTPMessage readFirstLine(HTTPMessage httpMessage) throws IOException {
        String line = in.readLine();

        if (line == null) {
            throw new IOException("Connection closed");
        } else if (line.isEmpty()) {
            return null;
        }

        try {
            httpMessage = httpMessage.determineMessageType(line);
        } catch (Exception e) {
            throw e;
        }

        return httpMessage;
    }

    private HTTPMessage readHeaders(HTTPMessage httpMessage) throws IOException {
        String line;
        // Read all headers in the message until /r/n/r/n or connection closed
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            try {         
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {   
                    httpMessage.setHeader(parts[0].trim(), parts[1].trim());
                } else {
                    throw new IllegalArgumentException("Invalid header: " + line);
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw e;
            }
        }

        return httpMessage;
    }

    private HTTPMessage readBody(HTTPMessage httpMessage) throws ProtocolException, IOException {
        int contentLength = 0;
        
        try {
            contentLength = Integer.parseInt(httpMessage.getHeader(HTTPConstants.HEADER_CONTENT_LENGTH));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Invalid Content-Length header: " + httpMessage.getHeader(HTTPConstants.HEADER_CONTENT_LENGTH));
            throw new ProtocolException("Invalid Content-Length header");
        }
        
        // Read and set body data if Content-Length is positive
        if (contentLength > 0) {
            httpMessage.setBody(readBodyData(contentLength));
        } else {
            httpMessage.setBody("");
        }
        
        return httpMessage;
    }
    
    private String readBodyData(int contentLength) throws IOException {
        if(contentLength < 0) {
            throw new IllegalArgumentException("Content length cannot be negative");
        }
        
        char[] bodyChars = new char[contentLength];
        int bytesRead = 0;
        
        while (bytesRead < contentLength) {
            int result = in.read(bodyChars, bytesRead, contentLength - bytesRead);
            
            // Handle end of stream
            if (result == -1) {
                throw new IOException("Connection closed before all data was read");
            }
            
            // If '\r\n' occurs before expected content length, consider it as a client mistake
            if (bodyChars[bytesRead] == '\r' && bodyChars[bytesRead + 1] == '\n') {
                throw new IOException("Encountered CRLF before expected content length was read");
            }
    
            bytesRead += result;
        }
    
        // Validate bytesRead against contentLength
        if (bytesRead != contentLength) {
            throw new IOException("Incorrect Content-Length header. Expected: " + contentLength + " but got: " + bytesRead);
        }
        
        return new String(bodyChars, 0, bytesRead);
    }
    
    /**
     * Closes the connection and its associated streams.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        closeStreams();
        socket.close();
    }

    /**
     * Manually closes the connection and its associated streams.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void manuallyClose() throws IOException {
        closeStreams();
        socket.close();
    }

    /**
     * Closes the input and output streams.
     *
     * @throws IOException if an I/O error occurs.
     */
    private void closeStreams() throws IOException {
        in.close();
        out.close();
    }
}
