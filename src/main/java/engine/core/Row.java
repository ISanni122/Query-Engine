package engine.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single row of data in a table.
 * Internally stores column->value pairs as strings.
 */
public class Row {
    private final Map<String, String> data;

    public Row(Map<String, String> data) {
        this.data = new LinkedHashMap<>(data);
    }

    public String get(String column) {
        return data.get(column);
    }

    public Map<String, String> getData() {
        return data;
    }

    public boolean hasColumn(String column) {
        return data.containsKey(column);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("| ");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append(entry.getValue()).append(" | ");
        }
        return sb.toString();
    }
}
