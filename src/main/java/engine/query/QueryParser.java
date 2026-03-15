package engine.query;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses SQL-like query strings into a QueryPlan.
 *
 * Supported syntax:
 *   SELECT col1, col2 FROM table
 *   SELECT * FROM table WHERE col = 'value'
 *   SELECT * FROM table WHERE col > 50
 *   SELECT * FROM table ORDER BY col ASC|DESC
 *   SELECT * FROM table WHERE col = 'x' ORDER BY col2 DESC LIMIT 10
 */
public class QueryParser {

    public static QueryPlan parse(String query) {
        // Normalize whitespace
        String q = query.trim().replaceAll("\\s+", " ");
        String upper = q.toUpperCase();

        if (!upper.startsWith("SELECT")) {
            throw new RuntimeException("Only SELECT queries are supported.");
        }

        QueryPlan plan = new QueryPlan();

        // --- Extract LIMIT ---
        int limitIdx = upper.indexOf(" LIMIT ");
        if (limitIdx != -1) {
            String limitStr = q.substring(limitIdx + 7).trim();
            plan.setLimit(Integer.parseInt(limitStr.split(" ")[0]));
            q = q.substring(0, limitIdx);
            upper = q.toUpperCase();
        }

        // --- Extract ORDER BY ---
        int orderIdx = upper.indexOf(" ORDER BY ");
        if (orderIdx != -1) {
            String orderPart = q.substring(orderIdx + 10).trim();
            String[] orderTokens = orderPart.split(" ");
            plan.setOrderByColumn(orderTokens[0]);
            if (orderTokens.length > 1 && orderTokens[1].equalsIgnoreCase("DESC")) {
                plan.setOrderDesc(true);
            }
            q = q.substring(0, orderIdx);
            upper = q.toUpperCase();
        }

        // --- Extract WHERE ---
        int whereIdx = upper.indexOf(" WHERE ");
        if (whereIdx != -1) {
            String wherePart = q.substring(whereIdx + 7).trim();
            plan.setWhereClause(parseWhere(wherePart));
            q = q.substring(0, whereIdx);
            upper = q.toUpperCase();
        }

        // --- Extract FROM ---
        int fromIdx = upper.indexOf(" FROM ");
        if (fromIdx == -1) throw new RuntimeException("Missing FROM clause.");
        String tableName = q.substring(fromIdx + 6).trim();
        plan.setTableName(tableName);

        // --- Extract SELECT columns ---
        String selectPart = q.substring(6, fromIdx).trim();
        if (selectPart.equals("*")) {
            plan.setSelectColumns(List.of("*"));
        } else {
            List<String> cols = Arrays.stream(selectPart.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
            plan.setSelectColumns(cols);
        }

        return plan;
    }

    private static WhereClause parseWhere(String wherePart) {
        // Try each operator (longest first to avoid partial matches)
        String[] operators = {">=", "<=", "!=", ">", "<", "=", "LIKE"};
        for (String op : operators) {
            int idx = wherePart.toUpperCase().indexOf(op);
            if (idx != -1) {
                String col = wherePart.substring(0, idx).trim();
                String val = wherePart.substring(idx + op.length()).trim();
                return new WhereClause(col, op, val);
            }
        }
        throw new RuntimeException("Could not parse WHERE clause: " + wherePart);
    }
}
