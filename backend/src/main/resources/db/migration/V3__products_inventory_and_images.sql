CREATE TABLE products (
    id UUID PRIMARY KEY,
    slug VARCHAR(100) NOT NULL,
    name VARCHAR(120) NOT NULL,
    short_name VARCHAR(80) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    search_text VARCHAR(1200) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    category_id UUID NOT NULL,
    flavor VARCHAR(20),
    tags VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES product_categories(id),
    CONSTRAINT ck_products_slug_lowercase CHECK (slug = LOWER(slug)),
    CONSTRAINT ck_products_price_positive CHECK (price > 0),
    CONSTRAINT ck_products_currency_mxn CHECK (currency = 'MXN'),
    CONSTRAINT ck_products_flavor CHECK (flavor IS NULL OR flavor IN ('DULCE', 'ACIDO', 'PICANTE'))
);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_search_text ON products(search_text);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_flavor ON products(flavor);

CREATE TABLE product_inventory (
    product_id UUID PRIMARY KEY,
    available INTEGER NOT NULL,
    minimum_stock INTEGER NOT NULL,
    version BIGINT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT ck_inventory_available_nonnegative CHECK (available >= 0),
    CONSTRAINT ck_inventory_minimum_nonnegative CHECK (minimum_stock >= 0),
    CONSTRAINT ck_inventory_version_nonnegative CHECK (version >= 0)
);

CREATE INDEX idx_inventory_available ON product_inventory(available);
CREATE INDEX idx_inventory_minimum ON product_inventory(minimum_stock);

CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    content BYTEA NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_images_product UNIQUE (product_id),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT ck_product_images_type CHECK (content_type LIKE 'image/%')
);
