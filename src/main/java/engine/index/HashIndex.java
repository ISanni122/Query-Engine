package engine.index;

import engine.core.Row;
import engine.core.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A hash index on a single column for O(1) equality lookups.
 *
 * Internally: Map<columnValue, List<Row>>
 *
 * This is similar to how real databases build hash indexes to
 * avoid full table scans on equality conditions (WHERE col = 'x').
 *
 * (Week 3): Extend this to support range queries
 *      by building a sorted structure alongside the hash map.
 */
public class HashIndex {
    private final String columnName;
    private final Map<String, List<Row>> index;

    public HashIndex(Table table, String columnName) {
        this.columnName = columnName;
        this.index = new HashMap<>();
        build(table);
    }

    private void build(Table table) {
        for (Row row : table.getRows()) {
            String key = row.get(columnName);
            if (key != null) {
                index.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
            }
        }
        System.out.println("Built hash index on column '" + columnName + "' with " + index.size() + " distinct values.");
    }

    /**
     * O(1) lookup by exact value.
     */
    public List<Row> lookup(String value) {
        return index.getOrDefault(value, new ArrayList<>());
    }

    public String getColumnName() {
        return columnName;
    }

    public int distinctValues() {
        return index.size();
    }
}
