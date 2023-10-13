package util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling various IO operations such as saving and loading objects,
 * deleting files, reading text files, and creating files.
 */
public class IOUtility {
    private static final Logger LOGGER = Logger.getLogger(IOUtility.class.getName());


    public IOUtility() {}

    /**
     * Saves an object to a file.
     *
     * @param obj      The object to be saved.
     * @param filename The name of the file.
     * @throws IOException if an I/O error occurs.
     */
    public void saveToFile(Object obj, String filename) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(obj);
        } catch (IOException i) {
            LOGGER.log(Level.SEVERE, "Error saving to file: " + filename, i);
            throw i;
        }
    }

    /**
     * Loads an object from a file.
     *
     * @param filename The name of the file from which to load the object.
     * @return The object loaded from the file.
     * @throws IOException            if an I/O error occurs.
     * @throws FileNotFoundException if the class of the serialized object cannot be found.
     */
    public Object loadFromFile(String filename) throws IOException, FileNotFoundException, ClassNotFoundException {
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return in.readObject();
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException io) {
            throw io;
        } catch (ClassNotFoundException cnfe) {
            throw cnfe;
        }
    }

    /**
     * Deletes a file if it exists.
     *
     * @param filename The name of the file to delete.
     * @throws IOException if an I/O error occurs.
     */
    public void deleteFile(String filename) throws IOException {
        try {
            Files.deleteIfExists(Paths.get(filename));
            LOGGER.log(Level.INFO, "Successfully deleted file: " + filename);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error deleting file: " + filename, e);
            throw e;
        }
    }

    /**
     * Reads a text file and returns its content as a string.
     *
     * @param filePath The path to the text file.
     * @return A string containing the content of the text file.
     * @throws IOException if an I/O error occurs.
     */
    public String readTxtFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading text file: " + filePath, e);
            throw e;
        }
        return content.toString();
    }

    /**
     * Creates a new file if it does not already exist.
     *
     * @param filePath The path to the file.
     * @return true if the file was created, false if it already exists.
     * @throws IOException if an I/O error occurs.
     */
    public boolean createFileIfNotExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                LOGGER.log(Level.INFO, "Created new file: " + filePath);
                return true;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error creating file: " + filePath, e);
                throw e;
            }
        } else {
            LOGGER.log(Level.INFO, "File already exists: " + filePath);
            return false;
        }
    }
}
