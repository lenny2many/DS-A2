package common.http;

import common.http.messages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class HTTPConnection implements AutoCloseable {
    private final Socket socket;
    protected OutputStreamWriter out;
    protected BufferedReader in;

    public HTTPConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.socket.setSoTimeout(20000);
        this.out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) throws IOException {
        out.write(message, 0, message.length());
        out.flush();
        System.out.println("Message sent: \n" + message);
    }

    public HTTPMessage readMessage() throws IOException {
        HTTPMessage httpMessage = new HTTPMessage();

        httpMessage = readFirstLine(httpMessage);
        // If not valid first line, return null
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
            String[] parts = line.split(":", 2);
            if (parts.length >= 2) {
                httpMessage.setHeader(parts[0].trim(), parts[1].trim());
            }
        }

        return httpMessage;
    }

    private HTTPMessage readBody(HTTPMessage httpMessage) throws IOException {
        StringBuilder body = new StringBuilder();
    
        if (httpMessage == null) {
            throw new IllegalArgumentException("httpMessage cannot be null");
        }
        
        String contentLengthStr = httpMessage.getHeader("Content-Length");
        int contentLength = 0;
    
        if (contentLengthStr != null) {
            try {
                contentLength = Integer.parseInt(contentLengthStr);
            } catch (NumberFormatException e) {
                // Handle invalid Content-Length header
                throw new IllegalArgumentException("Invalid Content-Length header value");
            }
        }
    
        if (contentLength > 0) {
            // Read fixed size data
            char[] bodyChars = new char[contentLength];
            int bytesRead = 0;
            
            while (bytesRead < contentLength) {
                int result = in.read(bodyChars, bytesRead, contentLength - bytesRead);
                if (result == -1) {
                    // End of stream reached; handle it
                    break;
                }
                bytesRead += result;
            }
            
            if (bytesRead < contentLength) {
                // Log or handle: data is truncated
            }
    
            body.append(bodyChars, 0, bytesRead);  // Append only the bytes that were actually read
            httpMessage.setBody(body.toString());
        } else {
            httpMessage.setBody("");
        }
    
        return httpMessage;
    }
    
    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public void manuallyClose() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
