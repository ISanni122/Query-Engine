package engine.query;

import engine.core.Database;
import engine.core.Row;
import engine.core.Table;
import engine.index.HashIndex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Executes a QueryPlan against the Database.
 *
 * Execution pipeline:
 *   1. Scan (full table scan OR hash index lookup)
 *   2. Filter (WHERE clause)
 *   3. Sort (ORDER BY) — uses merge sort
 *   4. Project (SELECT columns)
 *   5. Limit
 */
public class QueryExecutor {
    private final Database database;
    private final Map<String, HashIndex> indexes;

    public QueryExecutor(Database database) {
        this.database = database;
        this.indexes = new HashMap<>();
    }

    /**
     * Register a hash index so the executor can use it for fast lookups.
     */
    public void addIndex(String tableName, HashIndex index) {
        String key = tableName.toLowerCase() + "." + index.getColumnName().toLowerCase();
        indexes.put(key, index);
        System.out.println("Registered index: " + key);
    }

    public List<Row> execute(QueryPlan plan) {
        Table table = database.getTable(plan.getTableName());
        WhereClause where = plan.getWhereClause();

        // --- Step 1 & 2: Scan + Filter ---
        List<Row> rows = scanAndFilter(table, where, plan.getTableName());

        // --- Step 3: Sort ---
        if (plan.getOrderByColumn() != null) {
            rows = mergeSort(rows, plan.getOrderByColumn(), plan.isOrderDesc());
        }

        // --- Step 4: Project columns ---
        rows = project(rows, plan.getSelectColumns(), table.getColumns());

        // --- Step 5: Limit ---
        if (plan.getLimit() > 0 && rows.size() > plan.getLimit()) {
            rows = rows.subList(0, plan.getLimit());
        }

        return rows;
    }

    private List<Row> scanAndFilter(Table table, WhereClause where, String tableName) {
        // Can we use a hash index?
        if (where != null && where.getOperator().equals("=")) {
            String indexKey = tableName.toLowerCase() + "." + where.getColumn().toLowerCase();
            if (indexes.containsKey(indexKey)) {
                System.out.println("[Planner] Using hash index on " + where.getColumn());
                return indexes.get(indexKey).lookup(where.getValue());
            }
        }

        // Full table scan
        System.out.println("[Planner] Full table scan on " + table.getName());
        List<Row> result = new ArrayList<>();
        for (Row row : table.getRows()) {
            if (where == null || where.matches(row)) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Merge sort implementation — O(n log n).
     * This is what databases use for ORDER BY on large datasets.
     *
     * (Week 2): Understand this and try implementing it yourself!
     */
    private List<Row> mergeSort(List<Row> rows, String column, boolean desc) {
        if (rows.size() <= 1) return rows;

        int mid = rows.size() / 2;
        List<Row> left  = mergeSort(new ArrayList<>(rows.subList(0, mid)), column, desc);
        List<Row> right = mergeSort(new ArrayList<>(rows.subList(mid, rows.size())), column, desc);

        return merge(left, right, column, desc);
    }

    private List<Row> merge(List<Row> left, List<Row> right, String column, boolean desc) {
        List<Row> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            int cmp = compare(left.get(i), right.get(j), column);
            if (desc) cmp = -cmp;

            if (cmp <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }

        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    private int compare(Row a, Row b, String column) {
        String va = a.get(column);
        String vb = b.get(column);
        if (va == null) return -1;
        if (vb == null) return 1;

        // Try numeric comparison first
        try {
            double da = Double.parseDouble(va);
            double db = Double.parseDouble(vb);
            return Double.compare(da, db);
        } catch (NumberFormatException e) {
            return va.compareToIgnoreCase(vb);
        }
    }

    private List<Row> project(List<Row> rows, List<String> selectCols, List<String> tableCols) {
        if (selectCols.contains("*")) return rows;

        return rows.stream().map(row -> {
            Map<String, String> projected = new LinkedHashMap<>();
            for (String col : selectCols) {
                projected.put(col, row.get(col));
            }
            return new Row(projected);
        }).collect(Collectors.toList());
    }

    /**
     * Performs a nested loop JOIN between two tables on matching column values.
     *
     * (Week 4): Replace this with a hash join for better performance.
     *
     * @param left     left table rows
     * @param right    right table rows
     * @param leftCol  join column from left table
     * @param rightCol join column from right table
     */
    public List<Row> nestedLoopJoin(List<Row> left, List<Row> right, String leftCol, String rightCol) {
        List<Row> result = new ArrayList<>();
        for (Row leftRow : left) {
            for (Row rightRow : right) {
                String lv = leftRow.get(leftCol);
                String rv = rightRow.get(rightCol);
                if (lv != null && lv.equals(rv)) {
                    // Merge both rows into one
                    Map<String, String> merged = new LinkedHashMap<>(leftRow.getData());
                    merged.putAll(rightRow.getData());
                    result.add(new Row(merged));
                }
            }
        }
        return result;
    }
}
