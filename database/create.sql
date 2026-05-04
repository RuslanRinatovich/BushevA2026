-- =====================================================
-- 1. Создание базы данных (выполнять отдельно, если БД ещё не создана)
-- =====================================================
-- CREATE DATABASE furniture_factory
--     WITH
--     OWNER = postgres
--     ENCODING = 'UTF8'
--     CONNECTION LIMIT = -1;

-- =====================================================
-- 2. Создание таблиц
-- =====================================================

-- 2.1 Пользователи
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- DIRECTOR, MASTER, STOREKEEPER
    full_name VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.2 Категории продукции (новая отдельная сущность)
CREATE TABLE IF NOT EXISTS product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.3 Материалы (сырьё)
CREATE TABLE IF NOT EXISTS materials (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    current_balance NUMERIC(12,3) DEFAULT 0,
    min_balance NUMERIC(12,3) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.4 Готовая продукция (номенклатура)
CREATE TABLE IF NOT EXISTS finished_products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category_id BIGINT REFERENCES product_categories(id) ON DELETE SET NULL,
    unit VARCHAR(20) DEFAULT 'шт',
    selling_price NUMERIC(12,2) NOT NULL,
    cost_price NUMERIC(12,2) DEFAULT 0,
    current_balance NUMERIC(12,3) DEFAULT 0,
    image BYTEA,                           -- изображение товара (бинарные данные)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.5 Заказы на производство
CREATE TABLE IF NOT EXISTS production_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL REFERENCES finished_products(id) ON DELETE RESTRICT,
    planned_quantity NUMERIC(10,2) NOT NULL,
    actual_quantity NUMERIC(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'planned', -- planned, in_progress, completed, cancelled
    planned_date DATE NOT NULL,
    completed_date DATE,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('planned', 'in_progress', 'completed', 'cancelled'))
);

-- 2.6 Нормы расхода материалов
CREATE TABLE IF NOT EXISTS consumption_rates (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES finished_products(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
    quantity NUMERIC(12,3) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, material_id)
);

-- 2.7 Фактический расход материалов по заказам
CREATE TABLE IF NOT EXISTS material_consumption (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES production_orders(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
    planned_quantity NUMERIC(12,3) NOT NULL,
    actual_quantity NUMERIC(12,3),
    written_off BOOLEAN DEFAULT false,
    written_off_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.8 Клиенты
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    inn VARCHAR(12),
    phone VARCHAR(20),
    address TEXT,
    contact_person VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.9 Отгрузка готовой продукции
CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    shipment_number VARCHAR(50) NOT NULL UNIQUE,
    client_id BIGINT REFERENCES clients(id) ON DELETE SET NULL,
    shipment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    product_id BIGINT NOT NULL REFERENCES finished_products(id) ON DELETE RESTRICT,
    quantity NUMERIC(12,2) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    total_amount NUMERIC(15,2),
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. Индексы для производительности
-- =====================================================
CREATE INDEX idx_materials_sku ON materials(sku);
CREATE INDEX idx_materials_name ON materials(name);
CREATE INDEX idx_products_sku ON finished_products(sku);
CREATE INDEX idx_products_category ON finished_products(category_id);
CREATE INDEX idx_categories_name ON product_categories(name);
CREATE INDEX idx_orders_product ON production_orders(product_id);
CREATE INDEX idx_orders_status ON production_orders(status);
CREATE INDEX idx_orders_planned_date ON production_orders(planned_date);
CREATE INDEX idx_consumption_product ON consumption_rates(product_id);
CREATE INDEX idx_consumption_material ON consumption_rates(material_id);
CREATE INDEX idx_consumption_order ON material_consumption(order_id);
CREATE INDEX idx_consumption_material_cons ON material_consumption(material_id);
CREATE INDEX idx_shipments_client ON shipments(client_id);
CREATE INDEX idx_shipments_date ON shipments(shipment_date);
CREATE INDEX idx_shipments_product ON shipments(product_id);
CREATE INDEX idx_clients_name ON clients(name);

-- =====================================================
-- 4. Тестовые данные
-- =====================================================

-- 4.1 Очистка старых данных (для повторного запуска)
TRUNCATE TABLE shipments CASCADE;
TRUNCATE TABLE material_consumption CASCADE;
TRUNCATE TABLE production_orders CASCADE;
TRUNCATE TABLE consumption_rates CASCADE;
TRUNCATE TABLE finished_products CASCADE;
TRUNCATE TABLE product_categories CASCADE;
TRUNCATE TABLE materials CASCADE;
TRUNCATE TABLE clients CASCADE;
TRUNCATE TABLE users CASCADE;

-- Сброс последовательностей
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE product_categories_id_seq RESTART WITH 1;
ALTER SEQUENCE materials_id_seq RESTART WITH 1;
ALTER SEQUENCE finished_products_id_seq RESTART WITH 1;
ALTER SEQUENCE production_orders_id_seq RESTART WITH 1;
ALTER SEQUENCE consumption_rates_id_seq RESTART WITH 1;
ALTER SEQUENCE material_consumption_id_seq RESTART WITH 1;
ALTER SEQUENCE clients_id_seq RESTART WITH 1;
ALTER SEQUENCE shipments_id_seq RESTART WITH 1;

-- 4.2 Пользователи (пароли зашифрованы BCrypt: admin123, master123, keeper123)
INSERT INTO users (username, password, role, full_name, enabled) VALUES
('admin', '$2a$12$d0ktYStFw5HflkopoxUane1FRbCNZFspzJFt4h3Upw/RXT0wJtX7i', 'DIRECTOR', 'Директор', true),
('master', '$2a$12$wX1EJjFPMIZpYaUoDVs6Q.QuxT/4K958GrTHtVmqDz/pLxXpf4Rae', 'MASTER', 'Мастер цеха', true),
('keeper', '$2a$12$WYPvwH81M0DiD0VQektfg.0xO9QyuDpfnUXhjvKOPQOHJGo3gGFki', 'STOREKEEPER', 'Кладовщик', true);

-- 4.3 Категории продукции
INSERT INTO product_categories (name, description, sort_order) VALUES
('Корпусная мебель', 'Столы, шкафы, комоды, тумбы', 1),
('Стулья', 'Стулья, табуреты, банкетки', 2),
('Мягкая мебель', 'Диваны, кресла, пуфы', 3),
('Офисная мебель', 'Офисные столы, кресла, шкафы', 4);

-- 4.4 Материалы
INSERT INTO materials (sku, name, unit, price, current_balance, min_balance) VALUES
('ДСП-16', 'ДСП 16 мм (лист 2750х1830 мм)', 'лист', 1800.00, 150, 20),
('ДСП-18', 'ДСП 18 мм (лист 2750х1830 мм)', 'лист', 2100.00, 100, 15),
('КРОМКА-22', 'Кромка ПВХ 22 мм', 'м.пог', 25.00, 5000, 500),
('КРОМКА-40', 'Кромка ПВХ 40 мм', 'м.пог', 35.00, 3000, 300),
('ФАНЕРА-10', 'Фанера 10 мм (лист 1525х1525 мм)', 'лист', 1200.00, 80, 10),
('ЛДСП-25', 'ЛДСП 25 мм (лист 2800х2070 мм)', 'лист', 3200.00, 60, 8),
('ШУРУПЫ', 'Шурупы саморезы 3.5х16 мм', 'упак (500 шт)', 150.00, 200, 30),
('ПЕТЛИ', 'Мебельные петли', 'пара', 80.00, 500, 50),
('НАПРАВЛЯЮЩИЕ', 'Направляющие для ящиков', 'комплект', 250.00, 150, 20),
('КЛЕЙ', 'Клей столярный', 'кг', 180.00, 100, 15);

-- 4.5 Готовая продукция (с категориями)
INSERT INTO finished_products (sku, name, category_id, selling_price, current_balance) VALUES
('СТОЛ-001', 'Стол обеденный "Классика"', (SELECT id FROM product_categories WHERE name = 'Корпусная мебель'), 8500.00, 25),
('СТУЛ-002', 'Стул "Венский"', (SELECT id FROM product_categories WHERE name = 'Стулья'), 3200.00, 50),
('ШКАФ-003', 'Шкаф-купе "Прованс"', (SELECT id FROM product_categories WHERE name = 'Корпусная мебель'), 18500.00, 12),
('КОМОД-004', 'Комод "Модерн"', (SELECT id FROM product_categories WHERE name = 'Корпусная мебель'), 12500.00, 8),
('ТУМБА-005', 'Тумба прикроватная', (SELECT id FROM product_categories WHERE name = 'Корпусная мебель'), 4500.00, 30),
('СТОЛ-006', 'Стол письменный "Ученик"', (SELECT id FROM product_categories WHERE name = 'Корпусная мебель'), 6800.00, 18),
('КРЕСЛО-007', 'Кресло офисное', (SELECT id FROM product_categories WHERE name = 'Офисная мебель'), 8500.00, 10),
('ДИВАН-008', 'Диван угловой "Комфорт"', (SELECT id FROM product_categories WHERE name = 'Мягкая мебель'), 35000.00, 5);

-- 4.6 Клиенты
INSERT INTO clients (name, inn, phone, contact_person, address) VALUES
('ООО "Мебельный Мир"', '771234567890', '+7 (495) 123-45-67', 'Иванов И.И.', 'г. Москва, ул. Тверская, д.10'),
('ИП Петров А.А.', '772345678901', '+7 (495) 234-56-78', 'Петров А.А.', 'г. Москва, ул. Арбат, д.5'),
('ООО "Домашний Уют"', '773456789012', '+7 (495) 345-67-89', 'Сидорова Е.В.', 'г. Москва, пр. Мира, д.20'),
('Мебельная фабрика "Комфорт"', '774567890123', '+7 (812) 456-78-90', 'Козлов П.П.', 'г. Санкт-Петербург, Невский пр., д.100'),
('ИП Смирнов В.В.', '775678901234', '+7 (495) 567-89-01', 'Смирнов В.В.', 'г. Москва, Ленинградский пр., д.30');

-- 4.7 Нормы расхода материалов
INSERT INTO consumption_rates (product_id, material_id, quantity) VALUES
-- Стол обеденный "Классика"
((SELECT id FROM finished_products WHERE sku = 'СТОЛ-001'), (SELECT id FROM materials WHERE sku = 'ДСП-16'), 2.5),
((SELECT id FROM finished_products WHERE sku = 'СТОЛ-001'), (SELECT id FROM materials WHERE sku = 'КРОМКА-22'), 12.0),
((SELECT id FROM finished_products WHERE sku = 'СТОЛ-001'), (SELECT id FROM materials WHERE sku = 'ШУРУПЫ'), 2.0),
-- Стул "Венский"
((SELECT id FROM finished_products WHERE sku = 'СТУЛ-002'), (SELECT id FROM materials WHERE sku = 'ДСП-16'), 0.8),
((SELECT id FROM finished_products WHERE sku = 'СТУЛ-002'), (SELECT id FROM materials WHERE sku = 'КРОМКА-22'), 4.0),
((SELECT id FROM finished_products WHERE sku = 'СТУЛ-002'), (SELECT id FROM materials WHERE sku = 'ШУРУПЫ'), 0.5),
-- Шкаф-купе "Прованс"
((SELECT id FROM finished_products WHERE sku = 'ШКАФ-003'), (SELECT id FROM materials WHERE sku = 'ЛДСП-25'), 6.0),
((SELECT id FROM finished_products WHERE sku = 'ШКАФ-003'), (SELECT id FROM materials WHERE sku = 'КРОМКА-40'), 30.0),
((SELECT id FROM finished_products WHERE sku = 'ШКАФ-003'), (SELECT id FROM materials WHERE sku = 'НАПРАВЛЯЮЩИЕ'), 3.0),
-- Комод "Модерн"
((SELECT id FROM finished_products WHERE sku = 'КОМОД-004'), (SELECT id FROM materials WHERE sku = 'ДСП-18'), 4.0),
((SELECT id FROM finished_products WHERE sku = 'КОМОД-004'), (SELECT id FROM materials WHERE sku = 'КРОМКА-40'), 18.0),
-- Тумба прикроватная
((SELECT id FROM finished_products WHERE sku = 'ТУМБА-005'), (SELECT id FROM materials WHERE sku = 'ДСП-16'), 1.5),
((SELECT id FROM finished_products WHERE sku = 'ТУМБА-005'), (SELECT id FROM materials WHERE sku = 'КРОМКА-22'), 6.0),
-- Стол письменный "Ученик"
((SELECT id FROM finished_products WHERE sku = 'СТОЛ-006'), (SELECT id FROM materials WHERE sku = 'ДСП-16'), 2.2),
((SELECT id FROM finished_products WHERE sku = 'СТОЛ-006'), (SELECT id FROM materials WHERE sku = 'КРОМКА-22'), 10.0);

-- 4.8 Заказы на производство
INSERT INTO production_orders (order_number, product_id, planned_quantity, status, planned_date, created_by) VALUES
('ПЗ-001', (SELECT id FROM finished_products WHERE sku = 'СТОЛ-001'), 10, 'completed', '2025-01-15', 2),
('ПЗ-002', (SELECT id FROM finished_products WHERE sku = 'СТУЛ-002'), 30, 'completed', '2025-01-20', 2),
('ПЗ-003', (SELECT id FROM finished_products WHERE sku = 'ШКАФ-003'), 5, 'in_progress', '2025-02-01', 2),
('ПЗ-004', (SELECT id FROM finished_products WHERE sku = 'КОМОД-004'), 4, 'planned', '2025-02-15', 2),
('ПЗ-005', (SELECT id FROM finished_products WHERE sku = 'ТУМБА-005'), 15, 'completed', '2025-01-25', 2),
('ПЗ-006', (SELECT id FROM finished_products WHERE sku = 'СТОЛ-006'), 8, 'planned', '2025-02-20', 2);

-- 4.9 Фактический расход материалов (списания)
-- Для ПЗ-001 (Стол обеденный)
INSERT INTO material_consumption (order_id, material_id, planned_quantity, actual_quantity, written_off, written_off_at)
SELECT
    (SELECT id FROM production_orders WHERE order_number = 'ПЗ-001'),
    cr.material_id,
    cr.quantity * 10,
    cr.quantity * 10,
    true,
    CURRENT_TIMESTAMP
FROM consumption_rates cr
WHERE cr.product_id = (SELECT id FROM finished_products WHERE sku = 'СТОЛ-001');

-- Для ПЗ-002 (Стул)
INSERT INTO material_consumption (order_id, material_id, planned_quantity, actual_quantity, written_off, written_off_at)
SELECT
    (SELECT id FROM production_orders WHERE order_number = 'ПЗ-002'),
    cr.material_id,
    cr.quantity * 30,
    cr.quantity * 30,
    true,
    CURRENT_TIMESTAMP
FROM consumption_rates cr
WHERE cr.product_id = (SELECT id FROM finished_products WHERE sku = 'СТУЛ-002');

-- Для ПЗ-005 (Тумба)
INSERT INTO material_consumption (order_id, material_id, planned_quantity, actual_quantity, written_off, written_off_at)
SELECT
    (SELECT id FROM production_orders WHERE order_number = 'ПЗ-005'),
    cr.material_id,
    cr.quantity * 15,
    cr.quantity * 15,
    true,
    CURRENT_TIMESTAMP
FROM consumption_rates cr
WHERE cr.product_id = (SELECT id FROM finished_products WHERE sku = 'ТУМБА-005');

-- 4.10 Обновление фактического количества продукции для завершённых заказов
UPDATE production_orders SET actual_quantity = planned_quantity, completed_date = CURRENT_DATE
WHERE order_number IN ('ПЗ-001', 'ПЗ-002', 'ПЗ-005');

-- 4.11 Обновление текущих остатков материалов после списаний
UPDATE materials SET current_balance = current_balance - (
    SELECT COALESCE(SUM(mc.actual_quantity), 0)
    FROM material_consumption mc
    WHERE mc.material_id = materials.id AND mc.written_off = true
);

-- 4.12 Обновление остатков готовой продукции
UPDATE finished_products SET current_balance = current_balance + (
    SELECT COALESCE(SUM(po.actual_quantity), 0)
    FROM production_orders po
    WHERE po.product_id = finished_products.id AND po.status = 'completed'
);

-- 4.13 Отгрузка готовой продукции
INSERT INTO shipments (shipment_number, client_id, shipment_date, product_id, quantity, price, total_amount, created_by) VALUES
('ОГ-001', 1, '2025-01-20', (SELECT id FROM finished_products WHERE sku = 'СТОЛ-001'), 5, 8500.00, 42500.00, 3),
('ОГ-002', 2, '2025-01-25', (SELECT id FROM finished_products WHERE sku = 'СТУЛ-002'), 20, 3200.00, 64000.00, 3),
('ОГ-003', 1, '2025-02-01', (SELECT id FROM finished_products WHERE sku = 'СТОЛ-001'), 3, 8500.00, 25500.00, 3),
('ОГ-004', 3, '2025-02-05', (SELECT id FROM finished_products WHERE sku = 'ТУМБА-005'), 10, 4500.00, 45000.00, 3);

-- 4.14 Обновление остатков готовой продукции после отгрузки
UPDATE finished_products SET current_balance = current_balance - (
    SELECT COALESCE(SUM(s.quantity), 0)
    FROM shipments s
    WHERE s.product_id = finished_products.id
);

-- =====================================================
-- 5. Сброс последовательностей
-- =====================================================
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('product_categories_id_seq', (SELECT MAX(id) FROM product_categories));
SELECT setval('materials_id_seq', (SELECT MAX(id) FROM materials));
SELECT setval('finished_products_id_seq', (SELECT MAX(id) FROM finished_products));
SELECT setval('production_orders_id_seq', (SELECT MAX(id) FROM production_orders));
SELECT setval('consumption_rates_id_seq', (SELECT MAX(id) FROM consumption_rates));
SELECT setval('material_consumption_id_seq', (SELECT MAX(id) FROM material_consumption));
SELECT setval('clients_id_seq', (SELECT MAX(id) FROM clients));
SELECT setval('shipments_id_seq', (SELECT MAX(id) FROM shipments));

-- =====================================================
-- 6. Проверочные запросы
-- =====================================================
SELECT 'База данных инициализирована успешно!' as status;
SELECT COUNT(*) as users_count FROM users;
SELECT COUNT(*) as categories_count FROM product_categories;
SELECT COUNT(*) as materials_count FROM materials;
SELECT COUNT(*) as products_count FROM finished_products;
SELECT COUNT(*) as orders_count FROM production_orders;
SELECT COUNT(*) as clients_count FROM clients;