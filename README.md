# Mini Query Engine

A lightweight SQL-like query engine built in Java that operates on in-memory data loaded from CSV files. Built to demonstrate core database internals: query parsing, hash indexing, merge sort, and join algorithms.

## Features

- **SQL-like query parser** — supports `SELECT`, `FROM`, `WHERE`, `ORDER BY`, `LIMIT`
- **Hash index** — O(1) equality lookups, automatically used by the query planner when available
- **Merge sort** — O(n log n) sorting for `ORDER BY`
- **Nested loop join** — join two tables on a shared column
- **Query planner** — decides between index scan and full table scan at runtime
- **Interactive REPL** — type and run queries live in the terminal

## Supported Query Syntax

```sql
SELECT * FROM table
SELECT col1, col2 FROM table
SELECT * FROM table WHERE col = 'value'
SELECT * FROM table WHERE col > 100
SELECT * FROM table WHERE col LIKE 'keyword'
SELECT * FROM table ORDER BY col DESC
SELECT * FROM table WHERE col = 'x' ORDER BY col2 ASC LIMIT 10
```

**Supported operators:** `=`, `!=`, `>`, `<`, `>=`, `<=`, `LIKE`

## Project Structure

```
queryengine/
├── data/
│   ├── customers.csv
│   ├── products.csv
│   ├── orders.csv
│   └── order_items.csv
└── src/main/java/
    ├── Main.java
    └── engine/
        ├── core/
        │   ├── Row.java         # Single row of data
        │   ├── Table.java       # In-memory table
        │   └── Database.java    # Holds all tables
        ├── index/
        │   └── HashIndex.java   # O(1) column index
        ├── query/
        │   ├── QueryParser.java   # Tokenizes SQL strings
        │   ├── QueryPlan.java     # Parsed query representation
        │   ├── WhereClause.java   # Filter logic
        │   └── QueryExecutor.java # Runs plans: scan, sort, join, project
        └── util/
            ├── CsvLoader.java     # Loads CSVs into tables
            └── ResultPrinter.java # Pretty-prints results
```

## Getting Started

**Requirements:** Java 11+ and Git

**Clone the repo:**
```bash
git clone https://github.com/YOUR_USERNAME/query-engine.git
cd query-engine
```

**Compile (Windows):**
```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java).FullName
```

**Compile (Mac/Linux):**
```bash
find src -name "*.java" | xargs javac -d out
```

**Run:**
```bash
java -cp out Main
```

## Example Queries

```sql
-- Find all customers in Toronto (uses hash index)
SELECT * FROM customers WHERE city = 'Toronto'

-- Electronics sorted by price
SELECT * FROM products WHERE category = 'Electronics' ORDER BY price DESC

-- Top 5 highest value delivered orders
SELECT * FROM orders WHERE status = 'delivered' ORDER BY total_amount DESC LIMIT 5

-- Books with projected columns
SELECT name, category, price FROM products WHERE category = 'Books'

-- Customers over 50, sorted by age
SELECT * FROM customers WHERE age > 50 ORDER BY age DESC LIMIT 3
```

## How It Works

When a query runs, the executor follows this pipeline:

```
SQL string → Parser → QueryPlan → Executor → Results
                                      |
                          ┌───────────┴───────────┐
                     Hash Index?              Full Scan
                     (equality =)           (everything else)
                          │                      │
                          └──────────┬───────────┘
                                  Filter (WHERE)
                                  Sort (ORDER BY) ← merge sort
                                  Project (SELECT cols)
                                  Limit
```

The query planner automatically uses a hash index when:
- The `WHERE` clause uses `=`
- An index has been built on that column

Otherwise it falls back to a full table scan.

## Roadmap

- [ ] Hash join (replace nested loop join for better performance)
- [ ] Range index support on `HashIndex` for `>`, `<` queries
- [ ] `GROUP BY` and aggregate functions (`COUNT`, `SUM`, `AVG`)
- [ ] Multi-condition `WHERE` with `AND` / `OR`
- [ ] `INSERT` and `UPDATE` support

## Dataset

The `data/` folder contains a synthetic e-commerce dataset:

| Table | Rows | Description |
|---|---|---|
| customers | 100 | Customer profiles with city, age, join date |
| products | 64 | Products across 8 categories with price and rating |
| orders | 300 | Orders with status and total amount |
| order_items | 758 | Line items linking orders to products |

## Tech Stack

- Java 21
- No external dependencies — pure standard library
