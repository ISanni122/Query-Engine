import engine.core.Database;
import engine.core.Row;
import engine.core.Table;
import engine.index.HashIndex;
import engine.query.QueryExecutor;
import engine.query.QueryParser;
import engine.query.QueryPlan;
import engine.util.CsvLoader;
import engine.util.ResultPrinter;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Mini Query Engine.
 *
 * Run this to see demo queries, then try the interactive REPL at the bottom.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        // -------------------------------------------------------
        // 1. LOAD DATA
        // -------------------------------------------------------
        System.out.println("=== Loading tables ===");
        Database db = new Database();

        Table customers  = CsvLoader.load("data/customers.csv",   "customers");
        Table products   = CsvLoader.load("data/products.csv",    "products");
        Table orders     = CsvLoader.load("data/orders.csv",      "orders");
        Table orderItems = CsvLoader.load("data/order_items.csv", "order_items");

        db.addTable(customers);
        db.addTable(products);
        db.addTable(orders);
        db.addTable(orderItems);

        // -------------------------------------------------------
        // 2. BUILD INDEXES
        // -------------------------------------------------------
        System.out.println("\n=== Building indexes ===");
        QueryExecutor executor = new QueryExecutor(db);

        executor.addIndex("customers",   new HashIndex(customers,  "city"));
        executor.addIndex("products",    new HashIndex(products,   "category"));
        executor.addIndex("orders",      new HashIndex(orders,     "status"));
        executor.addIndex("order_items", new HashIndex(orderItems, "order_id"));

        // -------------------------------------------------------
        // 3. DEMO QUERIES
        // -------------------------------------------------------
        System.out.println("\n=== Demo Queries ===");

        runQuery(executor, "SELECT * FROM customers WHERE city = 'Toronto'",
                "Customers in Toronto (uses hash index)");

        runQuery(executor, "SELECT * FROM products WHERE category = 'Electronics' ORDER BY price DESC",
                "Electronics sorted by price descending");

        runQuery(executor, "SELECT * FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 5",
                "Top 5 highest delivered orders");

        runQuery(executor, "SELECT * FROM products WHERE price > 100 ORDER BY rating DESC",
                "Products over $100 sorted by rating");

        runQuery(executor, "SELECT name, category, price FROM products WHERE category = 'Books'",
                "Book names and prices (projected columns)");

        // -------------------------------------------------------
        // 4. JOIN DEMO
        // -------------------------------------------------------
        System.out.println("\n=== JOIN Demo ===");
        System.out.println(">>> Joining customers + orders (nested loop join)");

        List<Row> allCustomers  = db.getTable("customers").getRows();
        List<Row> allOrders     = db.getTable("orders").getRows();
        List<Row> joined = executor.nestedLoopJoin(allCustomers, allOrders, "customer_id", "customer_id");

        // Show first 5 results
        ResultPrinter.print(joined.subList(0, Math.min(5, joined.size())),
                "customers JOIN orders ON customer_id (first 5 rows)");

        // -------------------------------------------------------
        // 5. INTERACTIVE REPL — type your own queries!
        // -------------------------------------------------------
        System.out.println("\n=== Interactive Query REPL ===");
        System.out.println("Tables: customers, products, orders, order_items");
        System.out.println("Type a SELECT query or 'exit' to quit.\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("query> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;
            if (input.isEmpty()) continue;

            try {
                QueryPlan plan = QueryParser.parse(input);
                List<Row> results = executor.execute(plan);
                ResultPrinter.print(results, input);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Goodbye!");
    }

    private static void runQuery(QueryExecutor executor, String sql, String label) {
        try {
            QueryPlan plan = QueryParser.parse(sql);
            List<Row> results = executor.execute(plan);
            ResultPrinter.print(results, label + "\n    SQL: " + sql);
        } catch (Exception e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }
}
