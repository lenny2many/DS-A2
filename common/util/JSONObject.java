package common.util;

import java.util.Map;
import java.util.HashMap;

public class JSONObject {
    private final Map<String, String> keyValMap = new HashMap<>();

    public JSONObject() {}

    public JSONObject(String jsonString) {
        String[] keyValPairs = jsonString.split("\n");
        for (String keyValPair : keyValPairs) {
            String[] keyVal = keyValPair.split(":");
            keyValMap.put(keyVal[0].trim(), keyVal[1].trim());
        }
    }

    public void put(String key, String value) {
        keyValMap.put(key, value);
    }

    public String get(String key) {
        return keyValMap.get(key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
            sb.append("   ");
            sb.append('"'+entry.getKey()+'"');
            sb.append(": ");
            
            if (isNumeric(entry.getValue())) {
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

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}
