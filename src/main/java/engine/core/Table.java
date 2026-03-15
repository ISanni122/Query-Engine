package engine.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an in-memory table with a name, column headers, and rows.
 */
public class Table {
    private final String name;
    private final List<String> columns;
    private final List<Row> rows;

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    public void addRow(Row row) {
        rows.add(row);
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return new ArrayList<>(rows);
    }

    public int size() {
        return rows.size();
    }

    public void printSchema() {
        System.out.println("Table: " + name);
        System.out.println("Columns: " + columns);
        System.out.println("Rows: " + rows.size());
    }
}
