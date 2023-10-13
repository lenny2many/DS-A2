package util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Serializable;
import java.util.HashMap;

public class JSONObject implements Serializable {
    private final Map<String, String> keyValMap = new HashMap<>();

    public JSONObject() {}

    public JSONObject(String inputString) throws Exception {
        try {
            if (inputString.contains("{")) {
                parseJsonFormat(inputString);
            } else {
                parseSimpleListFormat(inputString);
            }
        } catch (Exception e) {
            throw e;
        }
        
    }

    private void parseSimpleListFormat(String simpleListString) throws Exception {
        try {
            String[] keyValPairs = simpleListString.split("\n");
            for (String keyValPair : keyValPairs) {
                String[] keyVal = keyValPair.split(":");
                keyValMap.put(keyVal[0].trim(), keyVal[1].trim());
            }
        } catch (Exception e) {
            throw e;
        }
        
    }

    private void parseJsonFormat(String jsonString) {
        try {
            Pattern pattern = Pattern.compile("\"(.*?)\"\\s*:\\s*(\".*?\"|[-+]?[0-9]*\\.?[0-9]+)");
            Matcher matcher = pattern.matcher(jsonString);
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2).replaceAll("\"", "");
                keyValMap.put(key, value);
            }
        } catch (Exception e) {
            throw e;
        }
    }


    public void put(String key, String value) {
        keyValMap.put(key, value);
    }

    public String get(String key) {
        return keyValMap.get(key);
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
            sb.append("   ");
            sb.append('"').append(entry.getKey()).append('"');
            sb.append(": ");
            
            if (Math.isNumeric(entry.getValue())) {
                sb.append(entry.getValue());
            } else {
                sb.append('"').append(entry.getValue()).append('"');
            }

            sb.append(",\n");
        }
        sb.delete(sb.length()-2, sb.length());
        sb.append("\n}");
        return sb.toString();
    }

    public String toSimpleListString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }
}
