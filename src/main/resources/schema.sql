-- Reset the schema
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS pets CASCADE;
DROP TABLE IF EXISTS pets_tags CASCADE;
DROP TABLE IF EXISTS pet_photos CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS order_status CASCADE;

CREATE TYPE order_status AS ENUM ('available', 'pending', 'sold');

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    quantity INTEGER DEFAULT 0 CHECK (quantity >= 0),
    ship_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status order_status,
    complete BOOLEAN DEFAULT FALSE
);

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30)
);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    status order_status,
    category_id BIGINT, -- Type Category

    -- tags [tag] list of tags
    -- photourls [string] list of photo urls

    -- Foreign Key
    CONSTRAINT fk_pets_category FOREIGN KEY (category_id)
        REFERENCES category(id) ON DELETE SET NULL
);

-- Junction Table for pet tags
CREATE TABLE pets_tags(
    pet_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (pet_id, tag_id),
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Add foreign key to orders
ALTER TABLE orders
ADD CONSTRAINT fk_orders_pets FOREIGN KEY (pet_id)
    REFERENCES pets(id) ON DELETE CASCADE;

CREATE TABLE pet_photos(
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT,
    url TEXT NOT NULL,

    -- Foreign key
    CONSTRAINT fk_petphotos_pets FOREIGN KEY (pet_id)
        REFERENCES pets(id) ON DELETE SET NULL
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    phone VARCHAR(50),
    user_status INTEGER
);