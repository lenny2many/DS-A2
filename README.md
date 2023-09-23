# Distributed Systems Project #2

This repository contains assignment 2 consisting of several components including an Aggregation Server (AS), Content Server (CS), and a GET client. 

## Getting Started

### Prerequisites

- Java version 8

### Building and Running

To build and run the project, use the provided `Makefile`. Below is a detailed guide on how to make the best use of it:

### Compilation:
All compilation will first create a bin directory in the parent folder to store all of the `.class` files.
1. **Compile All Components**: To compile all components (Aggregation Server, Content Server, GET Client, and common utilities) at once:
    ```
    make
    ```
    Or specifically:
    ```
    make all
    ```
2. **Compile Specific Components**:
- **Aggregation Server**:
  ```
  $ make compile_as
  ```
- **Content Server**:
  ```
  $ make compile_cs
  ```
- **GET Client**:
  ```
  $ make compile_client
  ```
- **Common Utilities**:
  ```
  $ make compile_common
  ```


### Execution:

After successful compilation, you can run individual components as running all components will not provide a useful output in the terminal.  
  
**Important Note:** The makefile only uses `--default` for the application startup and if you want to use any custom options, you have to refer to the help section of each program individually using the `--help` option.
- **Run Aggregation Server**:
    Ensure that the aggregation server gets run first as this is a requirement for the both the GET client and the content server.
    ```
    $ make run_as
    ```
Run these next two components in either order and as many times you would like as long as the aggregation server is up.
- **Run Content Server**
    ```
    $ make run_cs
    ```
- **Run GET Client**
    ```
    $make run_client
    ```


## Project Structure

### **AS**: 
Contains the Aggregation Server component. For more details, refer to [AS/README.md](./AS/README.md).
  
### **CS**: 
Contains the Content Server component. For more details, refer to [CS/README.md](./CS/README.md).
  
### **GET**: 
Contains the GET client component. For more details, refer to [GET/README.md](./GET/README.md).
  
### **common**: 
Houses the common modules and utilities shared across the various components.
  
### **lib**: 
Contains external libraries and dependencies.