# Aggregation Server (AS)

The Aggregation Server serves as the middleware between a client requesting weather data and the content servers that host weather data. It follows REST (Representational State Transfer) architectural standards to ensure a stateless, client-server communication which is cacheable and follows a layered system architecture.

Content servers communicate with the Aggregation Server through HTTP PUT requests to send the data they have stored, while clients obtain weather data through HTTP GET requests.

The Aggregation Server adheres to the REST standards through the commands it offers, ensuring a predictable and easy-to-understand interface for interacting with the server. Here are the commands provided by the Aggregation Server, demonstrating the RESTful interface:
  

- GET Request:
  ```
  GET /weather/{STATION_ID} HTTP/1.1
  ```

  This command allows clients to retrieve the current weather data for a specific weather station by providing the station's ID.

- PUT Request:
  ```
  PUT /weather/{CONTENT_SERVER_ID} HTTP/1.1
  ```
  This command allows content servers to send updated weather data to the Aggregation Server by providing their server ID.

The aforementioned commands adhere to the REST standards by being stateless; each command from the client to server must contain all the information the server needs to fulfill the request. Moreover, these commands operate over HTTP, utilizing standard HTTP methods, which allows the system to leverage existing web infrastructure and makes the commands easily understandable to developers. Through these commands, the Aggregation Server establishes a clean, standardized communication protocol among clients and content servers, encapsulating the complexity of data aggregation and providing an easy-to-use interface for data retrieval and update.

## Directory Structure

- **src**: Contains the source code for the Aggregation Server.
  - **main**: Contains the main server application.
  - **resources**: Houses data and help files.
- **test**: Contains unit tests for the Aggregation Server.

## Usage

To run the Aggregation Server (AS), use the following command:

```
java AggregationServer [PORT]
```

**Options:**

- **PORT:** The port number for the server to listen on.
- **--default, -d:** Use this option to have the server listen on the default port {4567}.
- **--help, -h:** Use this option to display the help message.
  
**Examples:**
- To start the server on port 4567, use the following command:
  ```
  java AggregationServer 4567
  ```
- To start the server on the default port, use the following command:
  ```
  java AggregationServer -d
  ```

**Defaults:**
- **PORT:** 4567

Ensure you've compiled the AS component before running it. You can compile it using the Makefile in the parent directory as follows:

```
$make compile_as
```
After compilation, navigate to the directory containing the compiled .class files (usually the bin directory), then use the above java command to start the server.
  
Or simply use the make command in parent directory to run `--default`:
```
$make run_as
```
