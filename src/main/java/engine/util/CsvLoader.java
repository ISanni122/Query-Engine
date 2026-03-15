package engine.util;

import engine.core.Row;
import engine.core.Table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads CSV files into Table objects.
 * First row is treated as the header (column names).
 */
public class CsvLoader {

    public static Table load(String filePath, String tableName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            // First line = column headers
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + filePath);
            }

            List<String> columns = Arrays.asList(headerLine.split(","));
            Table table = new Table(tableName, columns);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1); // -1 keeps trailing empty strings

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    String value = (i < values.length) ? values[i].trim() : "";
                    rowData.put(columns.get(i).trim(), value);
                }

                table.addRow(new Row(rowData));
            }

            System.out.println("Loaded " + table.size() + " rows into table '" + tableName + "'");
            return table;
        }
    }
}
