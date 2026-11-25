CREATE TABLE product_categories (
    id UUID PRIMARY KEY,
    slug VARCHAR(40) NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_categories_slug UNIQUE (slug),
    CONSTRAINT uk_product_categories_name UNIQUE (name),
    CONSTRAINT ck_product_categories_slug_lowercase CHECK (slug = LOWER(slug))
);

INSERT INTO product_categories (id, slug, name, created_at) VALUES
    ('10000000-0000-0000-0000-000000000001', 'botanas', 'Botanas', CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000002', 'dulces', 'Dulces', CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000003', 'paquetes', 'Paquetes', CURRENT_TIMESTAMP);
