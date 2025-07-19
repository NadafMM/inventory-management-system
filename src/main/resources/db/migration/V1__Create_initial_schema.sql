-- Initial schema for Inventory Management System
-- Using path enumeration pattern for hierarchical categories
-- Implementing soft delete strategy with deleted_at timestamps

-- Categories table with path enumeration for hierarchy
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    path VARCHAR(1000) NOT NULL, -- Path enumeration: /1/2/3/
    parent_id BIGINT,
    level INTEGER NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata TEXT, -- JSON metadata for extensibility
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT chk_categories_level CHECK (level >= 0),
    CONSTRAINT chk_categories_sort_order CHECK (sort_order >= 0)
);

-- Products table
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    brand VARCHAR(100),
    manufacturer VARCHAR(100),
    weight DECIMAL(10,3),
    dimensions VARCHAR(50), -- Format: LxWxH
    color VARCHAR(50),
    material VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata TEXT, -- JSON metadata for extensibility
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_products_weight CHECK (weight >= 0)
);

-- SKUs (Stock Keeping Units) table
CREATE TABLE skus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_code VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    variant_name VARCHAR(100), -- e.g., "Small Red", "Large Blue"
    size VARCHAR(20),
    color VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    cost DECIMAL(10,2),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reorder_point INTEGER NOT NULL DEFAULT 0,
    reorder_quantity INTEGER NOT NULL DEFAULT 0,
    barcode VARCHAR(100),
    location VARCHAR(100), -- Warehouse location
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata TEXT, -- JSON metadata for extensibility
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_skus_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_skus_price CHECK (price >= 0),
    CONSTRAINT chk_skus_cost CHECK (cost >= 0),
    CONSTRAINT chk_skus_stock_quantity CHECK (stock_quantity >= 0),
    CONSTRAINT chk_skus_reserved_quantity CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_skus_reorder_point CHECK (reorder_point >= 0),
    CONSTRAINT chk_skus_reorder_quantity CHECK (reorder_quantity >= 0)
);

-- Inventory transactions table for audit trail
CREATE TABLE inventory_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- IN, OUT, ADJUSTMENT, RESERVED, RELEASED
    quantity INTEGER NOT NULL,
    reference_id VARCHAR(100), -- Order ID, adjustment ID, etc.
    reference_type VARCHAR(50), -- ORDER, ADJUSTMENT, TRANSFER, etc.
    reason VARCHAR(255),
    performed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_inventory_transactions_sku FOREIGN KEY (sku_id) REFERENCES skus(id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_transactions_type CHECK (transaction_type IN ('IN', 'OUT', 'ADJUSTMENT', 'RESERVED', 'RELEASED'))
);

-- Indexes for performance optimization

-- Categories indexes
CREATE INDEX idx_categories_path ON categories(path);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_level ON categories(level);
CREATE INDEX idx_categories_active ON categories(is_active);
CREATE INDEX idx_categories_deleted_at ON categories(deleted_at);
CREATE INDEX idx_categories_name ON categories(name);

-- Products indexes
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_products_deleted_at ON products(deleted_at);
CREATE INDEX idx_products_created_at ON products(created_at);

-- SKUs indexes
CREATE INDEX idx_skus_product_id ON skus(product_id);
CREATE INDEX idx_skus_sku_code ON skus(sku_code);
CREATE INDEX idx_skus_barcode ON skus(barcode);
CREATE INDEX idx_skus_active ON skus(is_active);
CREATE INDEX idx_skus_deleted_at ON skus(deleted_at);
CREATE INDEX idx_skus_stock_quantity ON skus(stock_quantity);
CREATE INDEX idx_skus_available_quantity ON skus(available_quantity);
CREATE INDEX idx_skus_reorder_point ON skus(reorder_point);
CREATE INDEX idx_skus_location ON skus(location);

-- Inventory transactions indexes
CREATE INDEX idx_inventory_transactions_sku_id ON inventory_transactions(sku_id);
CREATE INDEX idx_inventory_transactions_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inventory_transactions_reference ON inventory_transactions(reference_id, reference_type);
CREATE INDEX idx_inventory_transactions_created_at ON inventory_transactions(created_at);

-- Composite indexes for common queries
CREATE INDEX idx_categories_active_level ON categories(is_active, level);
CREATE INDEX idx_products_category_active ON products(category_id, is_active);
CREATE INDEX idx_skus_product_active ON skus(product_id, is_active);
CREATE INDEX idx_skus_low_stock ON skus(stock_quantity, reorder_point, is_active);

-- Note: Triggers for updated_at timestamps and category path maintenance
-- are handled at the application level due to H2 compatibility.
-- The application uses JPA @PreUpdate and @PrePersist annotations for timestamp management
-- and service-level logic for category path maintenance. 