CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pizzas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    is_available BOOLEAN DEFAULT true,
    cooking_time_minutes INTEGER DEFAULT 15 CHECK (cooking_time_minutes > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pizza_sizes (
    id BIGSERIAL PRIMARY KEY,
    pizza_id BIGINT NOT NULL REFERENCES pizzas(id) ON DELETE CASCADE,
    size_name VARCHAR(20) NOT NULL, -- '25см', '30см', '35см'
    diameter_cm INTEGER NOT NULL,
    weight_grams INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    is_available BOOLEAN DEFAULT true,
    UNIQUE(pizza_id, size_name)
);

CREATE TABLE IF NOT EXISTS ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pizza_ingredients (
    pizza_id BIGINT NOT NULL REFERENCES pizzas(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
    PRIMARY KEY(pizza_id, ingredient_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- подумать ещё
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERING', 'COMPLETED', 'CANCELLED')) DEFAULT 'PENDING',
    total_price DECIMAL(10,2) NOT NULL CHECK(total_price  > 0),
    delivery_address JSONB NOT NULL,
    customer_notes TEXT,
    delivery_type VARCHAR(20) CHECK (delivery_type IN ('DELIVERY', 'PICKUP', 'IN_STORE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    pizza_id BIGINT NOT NULL REFERENCES pizzas(id) ON DELETE RESTRICT,
    pizza_size_id BIGINT NOT NULL REFERENCES pizza_sizes(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sub_total DECIMAL(10,2) GENERATED ALWAYS AS (unit_price * quantity) STORED,
    UNIQUE(order_id, pizza_id, pizza_size_id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_id VARCHAR(255),
    pizza_id BIGINT NOT NULL REFERENCES pizzas(id) ON DELETE CASCADE,
    pizza_size_id BIGINT NOT NULL REFERENCES pizza_sizes(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, session_id, pizza_id, pizza_size_id),
    CONSTRAINT cart_owner_check CHECK (
            (user_id IS NOT NULL AND session_id IS NULL) OR
            (user_id IS NULL AND session_id IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

     UNIQUE(order_id, user_id) -- возможно дать возможность много комментов писать
);