package aggregation_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import aggregation_server.Server;

public class AggregationServer extends Server {

    public static void main(String[] args) {
        AggregationServer server = new AggregationServer();
        server.serverStart();
    }
}