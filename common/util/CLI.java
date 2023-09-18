package common.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CLI {
    private String HELP_MESSAGE;

    public CLI(String helpMessage) {
        this.HELP_MESSAGE = helpMessage;
    }

    public static CLI initialiseCLI(String helpFilePath) {
    try {
        return new CLI(IOUtility.readTxtFile(helpFilePath));
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

    public Map<String, String> parseCLIArguments(String[] args) {
        Map<String, String> argMap = new HashMap<>();
        // Placeholder logic: assumes arguments are in format: --key value
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                // if --help or --default, ignore all other arguments
                if (args[i].substring(2).equals("help") || args[i].substring(2).equals("default")) {
                    argMap.put(args[i].substring(2), "");
                    break;
                }
                String key = args[i].substring(2);
                String value = args[i + 1];
                argMap.put(key, value);
            }
        }

        if (argMap.containsKey("help") || argMap.containsKey("h")) {
            System.out.println(HELP_MESSAGE);
            return null;
        }

        if (argMap.containsKey("default") || argMap.containsKey("d")) {
            argMap.put("port", "4567");
        }
        
        if (argMap.isEmpty()) {
            System.out.println("No arguments provided. Use --help to see available options");
            return null;
        }
        
        return argMap;
    }
}
