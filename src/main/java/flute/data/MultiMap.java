package flute.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiMap {
    private HashMap<String, List<String>> multiMap;

    public MultiMap() {
        multiMap = new HashMap<>();
    }

    public HashMap<String, List<String>> getValue() {
        return multiMap;
    }

    public void put(String key, String value) {
        List<String> keyValue = multiMap.get(key);
        if (keyValue == null) {
            keyValue = new ArrayList<>();
            keyValue.add(value);
            multiMap.put(key, keyValue);
        } else {
            if (!keyValue.contains(value)) {
                keyValue.add(value);
            }
        }
    }

    public List<String> get(String key) {
        return multiMap.get(key);
    }
}
