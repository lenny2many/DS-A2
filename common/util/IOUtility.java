package common.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class IOUtility {
    public static void saveToFile(Object obj, String filename) {
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(obj);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static Object loadFromFile(String filename) {
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public static void deleteFile(String filename) {
        try {
            Files.deleteIfExists(Paths.get(filename));
            System.out.println("Successfully deleted file: " + filename);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + filename);
            e.printStackTrace();
        }
    }

    public static String readTxtFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    public static boolean createFileIfNotExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                return true;
            } catch (IOException e) {
                System.err.println("Error creating file: " + filePath);
                e.printStackTrace();
                return false;
            }
        } else{
            System.out.println("File already exists: " + filePath);
            return false;
        }
    }
}
