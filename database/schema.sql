-- =============================================================
-- Nova Factory ERP — Database Schema
-- Database: factory_management
-- =============================================================

CREATE DATABASE IF NOT EXISTS factory_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE factory_management;

-- =============================================================
-- ROLES & USERS
-- =============================================================

CREATE TABLE IF NOT EXISTS roles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed',
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100),
    role_id      INT NOT NULL,
    is_active    TINYINT(1) DEFAULT 1,
    last_login   TIMESTAMP NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

-- =============================================================
-- AUDIT / ACTIVITY LOG
-- =============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT,
    username    VARCHAR(50),
    action      VARCHAR(100) NOT NULL,
    module      VARCHAR(50),
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- HR — DEPARTMENTS & EMPLOYEES
-- =============================================================

CREATE TABLE IF NOT EXISTS departments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active   TINYINT(1) DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS employees (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    employee_code    VARCHAR(20) NOT NULL UNIQUE,
    full_name        VARCHAR(100) NOT NULL,
    father_name      VARCHAR(100),
    cnic             VARCHAR(20),
    phone            VARCHAR(20),
    email            VARCHAR(100),
    address          TEXT,
    department_id    INT,
    designation      VARCHAR(100),
    joining_date     DATE,
    employment_type  ENUM('PERMANENT','CONTRACT','DAILY_WAGE','PART_TIME') DEFAULT 'PERMANENT',
    basic_salary     DECIMAL(12,2) DEFAULT 0.00,
    status           ENUM('ACTIVE','INACTIVE','TERMINATED','RESIGNED') DEFAULT 'ACTIVE',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- ATTENDANCE
-- =============================================================

CREATE TABLE IF NOT EXISTS attendance (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    employee_id  INT NOT NULL,
    date         DATE NOT NULL,
    check_in     TIME,
    check_out    TIME,
    status       ENUM('PRESENT','ABSENT','LATE','LEAVE','HALF_DAY') NOT NULL DEFAULT 'PRESENT',
    remarks      VARCHAR(255),
    recorded_by  INT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_attendance (employee_id, date),
    CONSTRAINT fk_att_emp FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_att_user FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- PAYROLL
-- =============================================================

CREATE TABLE IF NOT EXISTS payroll (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    employee_id       INT NOT NULL,
    period_month      INT NOT NULL COMMENT '1-12',
    period_year       INT NOT NULL,
    basic_salary      DECIMAL(12,2) DEFAULT 0.00,
    allowances        DECIMAL(12,2) DEFAULT 0.00,
    overtime          DECIMAL(12,2) DEFAULT 0.00,
    bonus             DECIMAL(12,2) DEFAULT 0.00,
    deductions        DECIMAL(12,2) DEFAULT 0.00,
    absence_deduction DECIMAL(12,2) DEFAULT 0.00,
    late_deduction    DECIMAL(12,2) DEFAULT 0.00,
    net_salary        DECIMAL(12,2) DEFAULT 0.00,
    working_days      INT DEFAULT 0,
    present_days      INT DEFAULT 0,
    absent_days       INT DEFAULT 0,
    late_days         INT DEFAULT 0,
    status            ENUM('DRAFT','CALCULATED','APPROVED','PAID') DEFAULT 'DRAFT',
    notes             TEXT,
    processed_by      INT,
    processed_at      TIMESTAMP NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payroll (employee_id, period_month, period_year),
    CONSTRAINT fk_payroll_emp FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_payroll_user FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- SUPPLIERS
-- =============================================================

CREATE TABLE IF NOT EXISTS suppliers (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(20) NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    contact_name VARCHAR(100),
    phone        VARCHAR(20),
    email        VARCHAR(100),
    address      TEXT,
    city         VARCHAR(50),
    is_active    TINYINT(1) DEFAULT 1,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =============================================================
-- INVENTORY — RAW MATERIALS
-- =============================================================

CREATE TABLE IF NOT EXISTS raw_materials (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(30) NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    category       VARCHAR(50),
    unit           VARCHAR(20) NOT NULL DEFAULT 'pcs',
    current_stock  DECIMAL(12,3) DEFAULT 0.000,
    min_stock      DECIMAL(12,3) DEFAULT 0.000,
    max_stock      DECIMAL(12,3) DEFAULT 0.000,
    purchase_price DECIMAL(12,2) DEFAULT 0.00,
    location       VARCHAR(100),
    supplier_id    INT,
    status         ENUM('ACTIVE','INACTIVE','DISCONTINUED') DEFAULT 'ACTIVE',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rm_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- MATERIAL LOTS / BATCHES
-- =============================================================

CREATE TABLE IF NOT EXISTS material_lots (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    lot_number     VARCHAR(50) NOT NULL UNIQUE,
    material_id    INT NOT NULL,
    supplier_id    INT,
    original_qty   DECIMAL(12,3) NOT NULL,
    remaining_qty  DECIMAL(12,3) NOT NULL,
    received_date  DATE NOT NULL,
    expiry_date    DATE,
    purchase_price DECIMAL(12,2) DEFAULT 0.00,
    status         ENUM('AVAILABLE','PARTIALLY_USED','EXHAUSTED','QUARANTINE','REJECTED') DEFAULT 'AVAILABLE',
    notes          TEXT,
    created_by     INT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lot_material FOREIGN KEY (material_id) REFERENCES raw_materials(id),
    CONSTRAINT fk_lot_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    CONSTRAINT fk_lot_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- PRODUCT CATEGORIES & PRODUCTS
-- =============================================================

CREATE TABLE IF NOT EXISTS product_categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active   TINYINT(1) DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    product_code  VARCHAR(30) NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    category_id   INT,
    description   TEXT,
    unit          VARCHAR(20) DEFAULT 'pcs',
    selling_price DECIMAL(12,2) DEFAULT 0.00,
    cost_price    DECIMAL(12,2) DEFAULT 0.00,
    current_stock DECIMAL(12,3) DEFAULT 0.000,
    min_stock     DECIMAL(12,3) DEFAULT 0.000,
    status        ENUM('ACTIVE','INACTIVE','DISCONTINUED') DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- INVENTORY TRANSACTIONS
-- =============================================================

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_type ENUM('PURCHASE','PRODUCTION_IN','PRODUCTION_OUT','SALE','SALE_RETURN',
                          'ADJUSTMENT_IN','ADJUSTMENT_OUT','DAMAGE','TRANSFER') NOT NULL,
    item_type        ENUM('RAW_MATERIAL','FINISHED_PRODUCT') NOT NULL,
    item_id          INT NOT NULL,
    lot_id           INT,
    quantity         DECIMAL(12,3) NOT NULL,
    unit_price       DECIMAL(12,2) DEFAULT 0.00,
    reference_type   VARCHAR(50),
    reference_id     BIGINT,
    notes            TEXT,
    performed_by     INT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_user FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- BILL OF MATERIALS
-- =============================================================

CREATE TABLE IF NOT EXISTS bills_of_materials (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    product_id  INT NOT NULL UNIQUE,
    version     VARCHAR(20) DEFAULT '1.0',
    description TEXT,
    is_active   TINYINT(1) DEFAULT 1,
    created_by  INT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bom_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_bom_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bom_items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    bom_id      INT NOT NULL,
    material_id INT NOT NULL,
    quantity    DECIMAL(12,4) NOT NULL,
    unit        VARCHAR(20),
    notes       VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bom_material (bom_id, material_id),
    CONSTRAINT fk_bomitem_bom FOREIGN KEY (bom_id) REFERENCES bills_of_materials(id) ON DELETE CASCADE,
    CONSTRAINT fk_bomitem_material FOREIGN KEY (material_id) REFERENCES raw_materials(id)
) ENGINE=InnoDB;

-- =============================================================
-- PRODUCTION ORDERS & BATCHES
-- =============================================================

CREATE TABLE IF NOT EXISTS production_orders (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    product_id   INT NOT NULL,
    quantity     INT NOT NULL,
    planned_date DATE,
    status       ENUM('DRAFT','IN_PROGRESS','COMPLETED','CANCELLED','ON_HOLD') DEFAULT 'DRAFT',
    notes        TEXT,
    created_by   INT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_po_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS production_batches (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    batch_number        VARCHAR(30) NOT NULL UNIQUE,
    production_order_id INT,
    product_id          INT NOT NULL,
    quantity_planned    INT NOT NULL,
    quantity_produced   INT DEFAULT 0,
    production_date     DATE,
    status              ENUM('IN_PROGRESS','COMPLETED','FAILED','CANCELLED') DEFAULT 'IN_PROGRESS',
    operator_id         INT,
    notes               TEXT,
    created_by          INT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_order FOREIGN KEY (production_order_id) REFERENCES production_orders(id) ON DELETE SET NULL,
    CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_batch_operator FOREIGN KEY (operator_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT fk_batch_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS production_material_usage (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    batch_id      INT NOT NULL,
    material_id   INT NOT NULL,
    lot_id        INT,
    quantity_used DECIMAL(12,4) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pmu_batch FOREIGN KEY (batch_id) REFERENCES production_batches(id),
    CONSTRAINT fk_pmu_material FOREIGN KEY (material_id) REFERENCES raw_materials(id),
    CONSTRAINT fk_pmu_lot FOREIGN KEY (lot_id) REFERENCES material_lots(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- PRODUCT SERIAL NUMBERS (after batches table)
-- =============================================================

CREATE TABLE IF NOT EXISTS product_serial_numbers (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number      VARCHAR(100) NOT NULL UNIQUE,
    product_id         INT NOT NULL,
    batch_id           INT,
    manufactured_date  DATE,
    status             ENUM('IN_STOCK','SOLD','DAMAGED','RETURNED') DEFAULT 'IN_STOCK',
    sale_order_item_id BIGINT,
    notes              VARCHAR(255),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_serial_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_serial_batch FOREIGN KEY (batch_id) REFERENCES production_batches(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- CUSTOMERS
-- =============================================================

CREATE TABLE IF NOT EXISTS customers (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    customer_code   VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    address         TEXT,
    city            VARCHAR(50),
    opening_balance DECIMAL(12,2) DEFAULT 0.00,
    current_balance DECIMAL(12,2) DEFAULT 0.00,
    status          ENUM('ACTIVE','INACTIVE','BLOCKED') DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =============================================================
-- SALES ORDERS
-- =============================================================

CREATE TABLE IF NOT EXISTS sales_orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    customer_id  INT NOT NULL,
    order_date   DATE NOT NULL,
    subtotal     DECIMAL(12,2) DEFAULT 0.00,
    discount     DECIMAL(12,2) DEFAULT 0.00,
    tax          DECIMAL(12,2) DEFAULT 0.00,
    grand_total  DECIMAL(12,2) DEFAULT 0.00,
    paid_amount  DECIMAL(12,2) DEFAULT 0.00,
    remaining    DECIMAL(12,2) DEFAULT 0.00,
    status       ENUM('DRAFT','CONFIRMED','DELIVERED','CANCELLED','RETURNED') DEFAULT 'DRAFT',
    notes        TEXT,
    created_by   INT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_so_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales_order_items (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    sales_order_id BIGINT NOT NULL,
    product_id     INT NOT NULL,
    quantity       INT NOT NULL,
    unit_price     DECIMAL(12,2) NOT NULL,
    discount       DECIMAL(12,2) DEFAULT 0.00,
    line_total     DECIMAL(12,2) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_soi_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_soi_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

ALTER TABLE product_serial_numbers
    ADD CONSTRAINT fk_serial_soi
        FOREIGN KEY (sale_order_item_id) REFERENCES sales_order_items(id) ON DELETE SET NULL;

-- =============================================================
-- INVOICES & PAYMENTS
-- =============================================================

CREATE TABLE IF NOT EXISTS invoices (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    sales_order_id BIGINT NOT NULL UNIQUE,
    invoice_date   DATE NOT NULL,
    due_date       DATE,
    grand_total    DECIMAL(12,2) NOT NULL,
    paid_amount    DECIMAL(12,2) DEFAULT 0.00,
    remaining      DECIMAL(12,2) DEFAULT 0.00,
    status         ENUM('UNPAID','PARTIAL','PAID','OVERDUE','CANCELLED') DEFAULT 'UNPAID',
    notes          TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_number VARCHAR(30) NOT NULL UNIQUE,
    invoice_id     BIGINT NOT NULL,
    customer_id    INT NOT NULL,
    payment_date   DATE NOT NULL,
    amount         DECIMAL(12,2) NOT NULL,
    payment_method ENUM('CASH','BANK_TRANSFER','CHEQUE','ONLINE') DEFAULT 'CASH',
    reference      VARCHAR(100),
    notes          TEXT,
    received_by    INT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_pay_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_pay_user FOREIGN KEY (received_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =============================================================
-- INDEXES
-- =============================================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_employees_code ON employees(employee_code);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_attendance_emp ON attendance(employee_id, date);
CREATE INDEX idx_rm_code ON raw_materials(code);
CREATE INDEX idx_rm_stock ON raw_materials(current_stock);
CREATE INDEX idx_lots_number ON material_lots(lot_number);
CREATE INDEX idx_products_code ON products(product_code);
CREATE INDEX idx_inv_tx_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inv_tx_item ON inventory_transactions(item_type, item_id);
CREATE INDEX idx_prod_orders_status ON production_orders(status);
CREATE INDEX idx_prod_batches_number ON production_batches(batch_number);
CREATE INDEX idx_serial_number ON product_serial_numbers(serial_number);
CREATE INDEX idx_serial_status ON product_serial_numbers(status);
CREATE INDEX idx_so_number ON sales_orders(order_number);
CREATE INDEX idx_so_customer ON sales_orders(customer_id);
CREATE INDEX idx_invoice_number ON invoices(invoice_number);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
