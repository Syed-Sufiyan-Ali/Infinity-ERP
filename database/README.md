# Nova Factory ERP — Database Setup

## Prerequisites
- MySQL 8.0+ installed and running
- A MySQL user with CREATE DATABASE privilege

## Setup Steps

### 1. Create the database and tables
```bash
mysql -u root -p < schema.sql
```

### 2. Insert seed data
```bash
mysql -u root -p < seed.sql
```

### 3. Configure connection
Edit `src/main/resources/application.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/factory_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_PASSWORD_HERE
```

## Default Login Credentials
| Username | Password   | Role    |
|----------|------------|---------|
| admin    | admin123   | ADMIN   |
| manager  | manager123 | MANAGER |

> **Important:** The BCrypt hashes in seed.sql were generated at compile time.
> On first run, Main.java logs the correct hashes to console. If login fails,
> copy the logged hash and update seed.sql, then re-run it.

## Tables Created
- roles, users, audit_logs
- departments, employees, attendance, payroll
- suppliers, raw_materials, material_lots
- product_categories, products, product_serial_numbers
- inventory_transactions
- bills_of_materials, bom_items
- production_orders, production_batches, production_material_usage
- customers, sales_orders, sales_order_items, invoices, payments
