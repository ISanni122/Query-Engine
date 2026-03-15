package engine.query;

import engine.core.Row;

/**
 * Represents a WHERE condition: column OPERATOR value
 * Supports: =, !=, >, <, >=, <=, LIKE
 *
 * Example: WHERE city = 'Toronto'
 *          WHERE price > 50
 *          WHERE name LIKE 'Watch'
 */
public class WhereClause {
    private final String column;
    private final String operator;
    private final String value;

    public WhereClause(String column, String operator, String value) {
        this.column = column.trim();
        this.operator = operator.trim().toUpperCase();
        this.value = value.trim().replace("'", ""); // strip quotes
    }

    public boolean matches(Row row) {
        String cellValue = row.get(column);
        if (cellValue == null) return false;

        switch (operator) {
            case "=":
                return cellValue.equalsIgnoreCase(value);
            case "!=":
                return !cellValue.equalsIgnoreCase(value);
            case "LIKE":
                return cellValue.toLowerCase().contains(value.toLowerCase());
            case ">": case "<": case ">=": case "<=":
                return compareNumerically(cellValue);
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }
    }

    private boolean compareNumerically(String cellValue) {
        try {
            double cell = Double.parseDouble(cellValue);
            double target = Double.parseDouble(value);
            switch (operator) {
                case ">":  return cell > target;
                case "<":  return cell < target;
                case ">=": return cell >= target;
                case "<=": return cell <= target;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Cannot compare non-numeric value '" + cellValue + "' with operator " + operator);
        }
        return false;
    }

    public String getColumn() { return column; }
    public String getOperator() { return operator; }
    public String getValue() { return value; }
}
