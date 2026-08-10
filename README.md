# Nova Factory ERP

A professional desktop Factory Management / ERP application built with Java 21, JavaFX 21, and MySQL.

---

## Requirements

| Tool       | Version     |
|------------|-------------|
| Java JDK   | 21+         |
| Maven      | 3.8+        |
| MySQL      | 8.0+        |

---

## Setup (Step by Step)

### 1. Install Java 21
Download from https://adoptium.net and add to PATH.
Verify: `java -version`

### 2. Install Maven
Download from https://maven.apache.org and add to PATH.
Verify: `mvn -version`

### 3. Install MySQL
Download from https://dev.mysql.com/downloads/mysql/
Verify: `mysql --version`

### 4. Create Database
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

### 5. Configure Database
Edit `src/main/resources/application.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/factory_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### 6. Run the Application
```bash
mvn clean javafx:run
```

---

## Default Login

| Username | Password   | Role  |
|----------|------------|-------|
| admin    | admin123   | ADMIN |

> On first run the console prints BCrypt hashes. If login fails, update
> seed.sql with those hashes and re-run the seed script.

---

## Architecture

```
com.nova.factoryerp/
├── Main.java                  Application entry point
├── config/AppConfig.java      Properties loader
├── database/DatabaseConnection.java  Connection pool
├── models/                    Plain Java model classes
├── dao/
│   ├── interfaces/            DAO contracts
│   └── impl/                  JDBC implementations
├── services/                  (Business logic — Phase 3+)
├── controllers/               JavaFX controllers
│   └── inventory/             Module-specific controllers
├── security/AuthService.java  BCrypt login / logout
└── utils/                     Helpers (Alert, Number, Date, Session)
```

---

## Modules

| Phase | Modules                                                  | Status      |
|-------|----------------------------------------------------------|-------------|
| 1     | Login, Main Layout, Dashboard, Sidebar, Roles            | ✅ Complete  |
| 2     | Raw Materials, Lots, Products, Suppliers, Transactions   | ✅ Complete  |
| 3     | BOM, Production Orders, Batches, Serial Numbers          | 🚧 Next      |
| 4     | Customers, Sales Orders, Invoices, Payments              | 🚧 Next      |
| 5     | Employees, Departments, Attendance, Payroll              | 🚧 Next      |
| 6     | Dashboard Charts, Reports, UI Polish                     | 🚧 Next      |

---

## Troubleshooting

**Cannot connect to MySQL**
- Verify MySQL service is running: `net start MySQL80` (Windows) or `sudo systemctl start mysql` (Linux)
- Check username/password in `application.properties`
- Ensure `factory_management` database exists

**Login fails after seeding**
- Check console output on startup — it prints correct BCrypt hashes
- Update password column in `users` table with the correct hash

**JavaFX not found**
- Ensure you run via `mvn clean javafx:run`, not `java -jar`
