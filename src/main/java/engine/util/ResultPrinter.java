package engine.util;

import engine.core.Row;

import java.util.List;

/**
 * Pretty-prints query results as a formatted table in the console.
 */
public class ResultPrinter {

    public static void print(List<Row> rows, String queryLabel) {
        System.out.println("\n>>> " + queryLabel);

        if (rows.isEmpty()) {
            System.out.println("(no results)");
            return;
        }

        // Collect all column names from first row
        List<String> columns = List.copyOf(rows.get(0).getData().keySet());

        // Calculate column widths
        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            widths[i] = columns.get(i).length();
        }
        for (Row row : rows) {
            for (int i = 0; i < columns.size(); i++) {
                String val = row.get(columns.get(i));
                if (val != null) widths[i] = Math.max(widths[i], val.length());
            }
        }

        // Print header
        printDivider(widths);
        printRow(columns.toArray(new String[0]), widths);
        printDivider(widths);

        // Print rows
        for (Row row : rows) {
            String[] vals = new String[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                vals[i] = row.get(columns.get(i));
                if (vals[i] == null) vals[i] = "";
            }
            printRow(vals, widths);
        }

        printDivider(widths);
        System.out.println(rows.size() + " row(s) returned.");
    }

    private static void printDivider(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        System.out.println(sb);
    }

    private static void printRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.length; i++) {
            String val = values[i] == null ? "" : values[i];
            sb.append(" ").append(val);
            sb.append(" ".repeat(widths[i] - val.length()));
            sb.append(" |");
        }
        System.out.println(sb);
    }
}
