package engine.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The in-memory database. Stores all loaded tables by name.
 */
public class Database {
    private final Map<String, Table> tables;

    public Database() {
        this.tables = new HashMap<>();
    }

    public void addTable(Table table) {
        tables.put(table.getName().toLowerCase(), table);
    }

    public Table getTable(String name) {
        Table t = tables.get(name.toLowerCase());
        if (t == null) {
            throw new RuntimeException("Table not found: " + name);
        }
        return t;
    }

    public boolean hasTable(String name) {
        return tables.containsKey(name.toLowerCase());
    }

    public Set<String> getTableNames() {
        return tables.keySet();
    }

    public void printAll() {
        System.out.println("=== Database Tables ===");
        for (Table t : tables.values()) {
            t.printSchema();
            System.out.println();
        }
    }
}
