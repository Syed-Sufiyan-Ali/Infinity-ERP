-- =============================================================
-- Nova Factory ERP — Seed Data
-- Run AFTER schema.sql
-- NOTE: Password hashes are fixed automatically on first app startup.
--       Placeholder hash below is intentional — DatabaseSeeder corrects it.
-- =============================================================

USE factory_management;

-- =============================================================
-- ROLES
-- =============================================================
INSERT IGNORE INTO roles (name, description) VALUES
('ADMIN',      'Full system access'),
('MANAGER',    'All modules read/write'),
('INVENTORY',  'Inventory management'),
('PRODUCTION', 'Manufacturing and production'),
('SALES',      'Sales, customers, invoices'),
('HR',         'Employees, attendance, payroll');

-- =============================================================
-- USERS
-- Passwords use a placeholder. DatabaseSeeder auto-fixes to:
--   admin    -> admin123
--   manager  -> manager123
-- =============================================================
INSERT IGNORE INTO users (username, password, full_name, email, role_id) VALUES
('admin',   'PLACEHOLDER_WILL_BE_FIXED', 'System Administrator', 'admin@novafactory.com',   1),
('manager', 'PLACEHOLDER_WILL_BE_FIXED', 'Factory Manager',      'manager@novafactory.com', 2);

-- =============================================================
-- DEPARTMENTS
-- =============================================================
INSERT IGNORE INTO departments (code, name, description) VALUES
('PROD',  'Production',      'Manufacturing and assembly'),
('INV',   'Inventory',       'Raw materials and finished goods'),
('SALES', 'Sales',           'Customer sales and orders'),
('HR',    'Human Resources', 'Employee management'),
('ADMIN', 'Administration',  'General administration'),
('QC',    'Quality Control', 'Quality assurance');

-- =============================================================
-- EMPLOYEES
-- =============================================================
INSERT IGNORE INTO employees (employee_code, full_name, father_name, phone,
    department_id, designation, joining_date, employment_type, basic_salary, status)
SELECT 'EMP-001','Ahmed Ali','Muhammad Ali','0300-1234567',
    (SELECT id FROM departments WHERE code='PROD'),
    'Production Supervisor','2024-01-15','PERMANENT',45000.00,'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code='EMP-001');

INSERT IGNORE INTO employees (employee_code, full_name, father_name, phone,
    department_id, designation, joining_date, employment_type, basic_salary, status)
SELECT 'EMP-002','Bilal Hassan','Hassan Ahmad','0301-2345678',
    (SELECT id FROM departments WHERE code='PROD'),
    'Machine Operator','2024-02-01','PERMANENT',30000.00,'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code='EMP-002');

INSERT IGNORE INTO employees (employee_code, full_name, father_name, phone,
    department_id, designation, joining_date, employment_type, basic_salary, status)
SELECT 'EMP-003','Sara Khan','Tariq Khan','0302-3456789',
    (SELECT id FROM departments WHERE code='INV'),
    'Inventory Officer','2024-03-10','PERMANENT',35000.00,'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code='EMP-003');

INSERT IGNORE INTO employees (employee_code, full_name, father_name, phone,
    department_id, designation, joining_date, employment_type, basic_salary, status)
SELECT 'EMP-004','Usman Raza','Abdul Raza','0303-4567890',
    (SELECT id FROM departments WHERE code='SALES'),
    'Sales Executive','2024-04-01','PERMANENT',40000.00,'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code='EMP-004');

INSERT IGNORE INTO employees (employee_code, full_name, father_name, phone,
    department_id, designation, joining_date, employment_type, basic_salary, status)
SELECT 'EMP-005','Fatima Malik','Malik Aslam','0304-5678901',
    (SELECT id FROM departments WHERE code='HR'),
    'HR Officer','2024-05-15','PERMANENT',38000.00,'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code='EMP-005');

-- =============================================================
-- SUPPLIERS
-- =============================================================
INSERT IGNORE INTO suppliers (code, name, contact_name, phone, email, city) VALUES
('SUP-001','ABC Electronics',   'Aamir Shah',   '021-11111111','aamir@abcelectronics.pk','Karachi'),
('SUP-002','XYZ Components',    'Zafar Iqbal',  '042-22222222','zafar@xyzcomponents.pk', 'Lahore'),
('SUP-003','Nova Packaging Co', 'Naveed Butt',  '051-33333333','naveed@novapack.pk',     'Islamabad'),
('SUP-004','Allied Materials',  'Asad Mehmood', '061-44444444','asad@alliedmat.pk',      'Multan');

-- =============================================================
-- RAW MATERIALS
-- =============================================================
INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-001','LED Chip 12W','Electronic','pcs',5000,1000,20000,15.00,'Rack A1',
    (SELECT id FROM suppliers WHERE code='SUP-001'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-001');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-002','PCB Board','Electronic','pcs',4800,1000,15000,25.00,'Rack A2',
    (SELECT id FROM suppliers WHERE code='SUP-001'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-002');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-003','LED Driver','Electronic','pcs',4500,1000,15000,35.00,'Rack A3',
    (SELECT id FROM suppliers WHERE code='SUP-001'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-003');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-004','Plastic Body','Mechanical','pcs',6000,2000,25000,8.00,'Rack B1',
    (SELECT id FROM suppliers WHERE code='SUP-002'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-004');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-005','Aluminium Heat Sink','Mechanical','pcs',5500,1000,20000,12.00,'Rack B2',
    (SELECT id FROM suppliers WHERE code='SUP-002'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-005');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-006','Capacitor 10uF','Electronic','pcs',15000,5000,50000,1.50,'Rack A4',
    (SELECT id FROM suppliers WHERE code='SUP-001'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-006');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-007','Resistor 100 Ohm','Electronic','pcs',20000,5000,80000,0.50,'Rack A5',
    (SELECT id FROM suppliers WHERE code='SUP-001'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-007');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-008','Wire 0.5mm','Electrical','mtr',3000,500,10000,5.00,'Rack C1',
    (SELECT id FROM suppliers WHERE code='SUP-002'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-008');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-009','Packaging Box','Packaging','pcs',8000,2000,30000,6.00,'Rack D1',
    (SELECT id FROM suppliers WHERE code='SUP-003'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-009');

INSERT IGNORE INTO raw_materials (code,name,category,unit,current_stock,min_stock,max_stock,
    purchase_price,location,supplier_id,status)
SELECT 'RM-010','E27 Base Socket','Mechanical','pcs',4000,1000,15000,10.00,'Rack B3',
    (SELECT id FROM suppliers WHERE code='SUP-002'),'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM raw_materials WHERE code='RM-010');

-- =============================================================
-- MATERIAL LOTS
-- =============================================================
INSERT IGNORE INTO material_lots (lot_number,material_id,supplier_id,original_qty,remaining_qty,
    received_date,purchase_price,status)
SELECT 'LOT-LED-2026-001',rm.id,s.id,5000,5000,'2026-07-01',15.00,'AVAILABLE'
FROM raw_materials rm, suppliers s WHERE rm.code='RM-001' AND s.code='SUP-001'
AND NOT EXISTS (SELECT 1 FROM material_lots WHERE lot_number='LOT-LED-2026-001');

INSERT IGNORE INTO material_lots (lot_number,material_id,supplier_id,original_qty,remaining_qty,
    received_date,purchase_price,status)
SELECT 'LOT-PCB-2026-001',rm.id,s.id,4800,4800,'2026-07-01',25.00,'AVAILABLE'
FROM raw_materials rm, suppliers s WHERE rm.code='RM-002' AND s.code='SUP-001'
AND NOT EXISTS (SELECT 1 FROM material_lots WHERE lot_number='LOT-PCB-2026-001');

INSERT IGNORE INTO material_lots (lot_number,material_id,supplier_id,original_qty,remaining_qty,
    received_date,purchase_price,status)
SELECT 'LOT-DRV-2026-001',rm.id,s.id,4500,4500,'2026-07-01',35.00,'AVAILABLE'
FROM raw_materials rm, suppliers s WHERE rm.code='RM-003' AND s.code='SUP-001'
AND NOT EXISTS (SELECT 1 FROM material_lots WHERE lot_number='LOT-DRV-2026-001');

INSERT IGNORE INTO material_lots (lot_number,material_id,supplier_id,original_qty,remaining_qty,
    received_date,purchase_price,status)
SELECT 'LOT-BOX-2026-001',rm.id,s.id,8000,8000,'2026-07-10',6.00,'AVAILABLE'
FROM raw_materials rm, suppliers s WHERE rm.code='RM-009' AND s.code='SUP-003'
AND NOT EXISTS (SELECT 1 FROM material_lots WHERE lot_number='LOT-BOX-2026-001');

-- =============================================================
-- PRODUCT CATEGORIES
-- =============================================================
INSERT IGNORE INTO product_categories (code, name, description) VALUES
('LED-BULB',  'LED Bulbs',       'LED light bulbs in various wattages'),
('LED-TUBE',  'LED Tube Lights', 'LED tube lights'),
('LED-PANEL', 'LED Panels',      'LED panel lights');

-- =============================================================
-- PRODUCTS
-- =============================================================
INSERT IGNORE INTO products (product_code,name,category_id,description,unit,
    selling_price,cost_price,current_stock,min_stock,status)
SELECT 'BULB-LED-12W','LED Bulb 12W',pc.id,
    'Energy-saving LED bulb 12 Watt E27 base','pcs',250.00,110.00,0,100,'ACTIVE'
FROM product_categories pc WHERE pc.code='LED-BULB'
AND NOT EXISTS (SELECT 1 FROM products WHERE product_code='BULB-LED-12W');

INSERT IGNORE INTO products (product_code,name,category_id,description,unit,
    selling_price,cost_price,current_stock,min_stock,status)
SELECT 'BULB-LED-18W','LED Bulb 18W',pc.id,
    'Energy-saving LED bulb 18 Watt E27 base','pcs',380.00,160.00,0,100,'ACTIVE'
FROM product_categories pc WHERE pc.code='LED-BULB'
AND NOT EXISTS (SELECT 1 FROM products WHERE product_code='BULB-LED-18W');

-- =============================================================
-- BILL OF MATERIALS — LED Bulb 12W
-- =============================================================
INSERT IGNORE INTO bills_of_materials (product_id, version, description, is_active)
SELECT p.id,'1.0','Standard BOM for LED Bulb 12W',1
FROM products p WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bills_of_materials WHERE product_id=p.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 1.0000, 'pcs'
FROM bills_of_materials b
JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-001'
WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 1.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-002' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 1.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-003' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 1.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-004' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 1.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-005' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 2.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-006' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 4.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-007' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 0.1500, 'mtr'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-008' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 1.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-009' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

INSERT IGNORE INTO bom_items (bom_id, material_id, quantity, unit)
SELECT b.id, rm.id, 1.0000, 'pcs'
FROM bills_of_materials b JOIN products p ON b.product_id=p.id
JOIN raw_materials rm ON rm.code='RM-010' WHERE p.product_code='BULB-LED-12W'
AND NOT EXISTS (SELECT 1 FROM bom_items WHERE bom_id=b.id AND material_id=rm.id);

-- =============================================================
-- CUSTOMERS
-- =============================================================
INSERT IGNORE INTO customers (customer_code,name,phone,email,address,city,
    opening_balance,current_balance,status)
VALUES
('CUST-001','Bright Lights Store','0300-9876543','info@brightlights.pk','123 Main Bazaar','Karachi',0,0,'ACTIVE'),
('CUST-002','Hafeez Electric',    '0301-8765432','hafeez@electric.pk',  '45 Urdu Bazaar', 'Lahore', 0,0,'ACTIVE'),
('CUST-003','Galaxy Traders',     '0302-7654321','galaxy@traders.pk',   '78 Saddar Market','Islamabad',0,0,'ACTIVE');

-- =============================================================
-- SAMPLE ATTENDANCE (today for all employees)
-- =============================================================
INSERT IGNORE INTO attendance (employee_id, date, check_in, check_out, status)
SELECT e.id, CURDATE(), '08:00:00', '17:00:00', 'PRESENT'
FROM employees e WHERE e.employee_code='EMP-001';

INSERT IGNORE INTO attendance (employee_id, date, check_in, check_out, status)
SELECT e.id, CURDATE(), '08:05:00', '17:00:00', 'PRESENT'
FROM employees e WHERE e.employee_code='EMP-002';

INSERT IGNORE INTO attendance (employee_id, date, check_in, check_out, status)
SELECT e.id, CURDATE(), '08:00:00', '17:00:00', 'PRESENT'
FROM employees e WHERE e.employee_code='EMP-003';

INSERT IGNORE INTO attendance (employee_id, date, check_in, check_out, status)
SELECT e.id, CURDATE(), '09:15:00', '17:00:00', 'LATE'
FROM employees e WHERE e.employee_code='EMP-004';

INSERT IGNORE INTO attendance (employee_id, date, status)
SELECT e.id, CURDATE(), 'ABSENT' FROM employees e WHERE e.employee_code='EMP-005';
