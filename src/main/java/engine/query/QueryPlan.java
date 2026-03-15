package engine.query;

import java.util.List;

/**
 * Holds the parsed components of a query before execution.
 */
public class QueryPlan {
    private List<String> selectColumns;  // ["*"] or ["name", "price"]
    private String tableName;
    private WhereClause whereClause;     // nullable
    private String orderByColumn;        // nullable
    private boolean orderDesc;
    private int limit;                   // -1 = no limit

    public QueryPlan() {
        this.limit = -1;
        this.orderDesc = false;
    }

    // --- Getters ---
    public List<String> getSelectColumns() { return selectColumns; }
    public String getTableName() { return tableName; }
    public WhereClause getWhereClause() { return whereClause; }
    public String getOrderByColumn() { return orderByColumn; }
    public boolean isOrderDesc() { return orderDesc; }
    public int getLimit() { return limit; }

    // --- Setters ---
    public void setSelectColumns(List<String> selectColumns) { this.selectColumns = selectColumns; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public void setWhereClause(WhereClause whereClause) { this.whereClause = whereClause; }
    public void setOrderByColumn(String col) { this.orderByColumn = col; }
    public void setOrderDesc(boolean desc) { this.orderDesc = desc; }
    public void setLimit(int limit) { this.limit = limit; }

    @Override
    public String toString() {
        return String.format("QueryPlan{select=%s, from=%s, where=%s, orderBy=%s %s, limit=%d}",
            selectColumns, tableName,
            whereClause != null ? whereClause.getColumn() + whereClause.getOperator() + whereClause.getValue() : "none",
            orderByColumn != null ? orderByColumn : "none",
            orderDesc ? "DESC" : "ASC",
            limit);
    }
}
