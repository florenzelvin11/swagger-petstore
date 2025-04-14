-- Reset the schema
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS pets CASCADE;
DROP TABLE IF EXISTS pets_tags CASCADE;
DROP TABLE IF EXISTS pet_photos CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS order_status CASCADE;
DROP TYPE IF EXISTS pet_status CASCADE;

CREATE TYPE pet_status AS ENUM ('available', 'pending', 'sold');
CREATE TYPE order_status AS ENUM ('placed', 'approved', 'delivered');

CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    quantity INTEGER DEFAULT 0 CHECK (quantity >= 0),
    ship_date TIMESTAMPTZ DEFAULT NOW(),
    status order_status,
    complete BOOLEAN DEFAULT FALSE
);

CREATE TABLE category (
    id BIGINT PRIMARY KEY,
    name VARCHAR(30)
);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE pets (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    status pet_status NOT NULL DEFAULT 'available',
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
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    phone VARCHAR(50),
    user_status INTEGER
);

---------------------------------
-- STORE PROCEDURES AND VIEW ----
---------------------------------

DROP TYPE IF EXISTS tag_name CASCADE;

CREATE TYPE tag_name AS (
    id BIGINT,
    name VARCHAR(50)
);

-- PETS PROCEDURES --

DROP PROCEDURE IF EXISTS insert_pet;
CREATE OR REPLACE PROCEDURE insert_pet(
    pet_id BIGINT,
    pet_name VARCHAR,
    category_id BIGINT,
    category_name VARCHAR,
    photo_urls TEXT[],
    tags tag_name[],
    status pet_status
)
AS '
DECLARE
    tag_id BIGINT;
    url TEXT;
    tag_rec tag_name;
BEGIN
    -- Insert or update category
    INSERT INTO category (id, name)
    VALUES (category_id, category_name)
    ON CONFLICT DO NOTHING;

    -- Insert
    INSERT INTO pets (id, name, status, category_id)
    VALUES (pet_id, pet_name, status, category_id);

    -- Insert photo URLs
    FOREACH url IN ARRAY photo_urls LOOP
        INSERT INTO pet_photos (pet_id, url)
        VALUES (pet_id, url);
    END LOOP;

    -- Insert tags and junctions
    FOREACH tag_rec IN ARRAY tags LOOP
        IF tag_rec.id IS NOT NULL THEN
            tag_id := tag_rec.id;
        ELSE
            SELECT id INTO tag_id FROM tags WHERE name = tag_rec.name;
            IF tag_id IS NULL THEN
                INSERT INTO tags (name) VALUES (tag_rec.name)
                RETURNING id INTO tag_id;
            END IF;
        END IF;

        INSERT INTO pets_tags (pet_id, tag_id)
        VALUES (pet_id, tag_id)
        ON CONFLICT DO NOTHING;
    END LOOP;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS update_pet;
CREATE OR REPLACE PROCEDURE update_pet(
    p_id BIGINT,
    p_name VARCHAR,
    cat_id BIGINT,
    cat_name VARCHAR,
    photo_urls TEXT[],
    tags tag_name[],
    p_status pet_status
)
AS '
BEGIN
    -- Check if pet exists
    IF NOT EXISTS (SELECT 1 FROM pets WHERE id = p_id) THEN
        RAISE EXCEPTION $$Pet with Id % does not exist$$, p_id
            USING ERRCODE = $$P0001$$;
    END IF;

    -- If category does not exist then add the category or update it
    INSERT INTO category (id, name)
    VALUES (cat_id, cat_name)
    ON CONFLICT (id) DO UPDATE
    SET name = EXCLUDED.name;

    -- Update pet attribute
    UPDATE pets
    SET
        name = p_name,
        category_id = cat_id,
        status = p_status
    WHERE id = p_id;

    -- New Photo Urls
    -- DELETE not needed urls
    DELETE FROM pet_photos
    WHERE pet_id = p_id
    AND url NOT IN (SELECT unnest(photo_urls));

    -- Insert new URLs
    INSERT INTO pet_photos (pet_id, url)
    SELECT p_id, new_url
    FROM unnest(photo_urls) AS new_url
    ON CONFLICT DO NOTHING;

    -- New Tags
    -- Delete any that are not in the new list
    DELETE FROM pets_tags
    WHERE pet_id = p_id
    AND tag_id NOT IN (
        SELECT (tag).id
        FROM unnest(tags) AS tag
    );

    -- Insert new ones
    INSERT INTO pets_tags (pet_id, tag_id)
    SELECT p_id, (tag).id
    FROM unnest(tags) AS tag
    ON CONFLICT (pet_id, tag_id) DO NOTHING;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS get_pet_by_id;
CREATE OR REPLACE PROCEDURE get_pet_by_id(
    IN p_id BIGINT,
    OUT result REFCURSOR
)
AS '
BEGIN
    OPEN result FOR
        SELECT
            p.id AS pet_id,
            p.name AS pet_name,
            p.category_id AS category_id,
            cat.name AS category_name,
            p.status AS pet_status,
            ARRAY_AGG(DISTINCT photo.url) AS pet_urls,
            ARRAY_AGG(DISTINCT ROW(tag.id, tag.name)::tag_name) AS pet_tag
        FROM pets p
        LEFT JOIN category cat ON cat.id = p.category_id
        LEFT JOIN pet_photos photo ON photo.pet_id = p.id
        LEFT JOIN pets_tags ptags ON ptags.pet_id = p.id
        LEFT JOIN tags tag ON tag.id = ptags.tag_id
        WHERE p.id = p_id
        GROUP BY p.id, p.name, p.category_id, cat.name, p.status;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS get_pet_by_tag_name;
CREATE OR REPLACE PROCEDURE get_pet_by_tag_name(
    IN t_name TEXT[],
    OUT result REFCURSOR
)
AS '
BEGIN
    OPEN result FOR
        WITH matched_pets AS (
            SELECT DISTINCT p.id
            FROM pets p
            JOIN pets_tags p_tags ON p_tags.pet_id = p.id
            JOIN tags t ON t.id = p_tags.tag_id
            WHERE t.name = ANY(t_name)
        )
        SELECT
            p.id AS pet_id,
            p.name AS pet_name,
            p.category_id AS category_id,
            c.name AS category_name,
            p.status AS pet_status,
            ARRAY_AGG(DISTINCT photo.url) AS pet_urls,
            ARRAY_AGG(DISTINCT ROW(t.id, t.name)::tag_name) AS pet_tag
        FROM pets p
        JOIN matched_pets mp ON mp.id = p.id         -- only matched pets
        JOIN category c ON c.id = p.category_id
        JOIN pet_photos photo ON photo.pet_id = p.id
        JOIN pets_tags pt ON pt.pet_id = p.id
        JOIN tags t ON t.id = pt.tag_id            -- join all tags (not filtered!)
        GROUP BY p.id, p.name, p.category_id, c.name, p.status;

END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS get_pet_by_status;
CREATE OR REPLACE PROCEDURE get_pet_by_status(
    IN s_name pet_status[],
    OUT result REFCURSOR
)
AS '
BEGIN
    OPEN result FOR
        SELECT
            p.id AS pet_id,
            p.name AS pet_name,
            p.category_id AS category_id,
            c.name AS category_name,
            p.status AS pet_status,
            ARRAY_AGG(DISTINCT photo.url) AS pet_urls,
            ARRAY_AGG(DISTINCT ROW(t.id, t.name)::tag_name) AS pet_tag
        FROM pets p
        JOIN category c ON c.id = p.category_id
        JOIN pet_photos photo ON photo.pet_id = p.id
        JOIN pets_tags pt ON pt.pet_id = p.id
        JOIN tags t ON t.id = pt.tag_id
        WHERE p.status = ANY(s_name)
        GROUP BY p.id, p.name, p.category_id, c.name, p.status;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS delete_pet_by_id;
CREATE OR REPLACE PROCEDURE delete_pet_by_id(
    IN p_id BIGINT
)
AS '
BEGIN
   -- Check if pet exists
       IF NOT EXISTS (SELECT 1 FROM pets WHERE id = p_id) THEN
           RAISE EXCEPTION $$Pet with Id % does not exist$$, p_id
               USING ERRCODE = $$P0001$$;
       END IF;

   -- Delete the pet row
    DELETE FROM pets
    WHERE id = p_id;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS update_pet_with_form;
CREATE OR REPLACE PROCEDURE update_pet_with_form(
    IN p_id BIGINT,
    IN p_name VARCHAR,
    IN p_status pet_status
)
AS '
BEGIN
    -- Check if pet exists
    IF NOT EXISTS (SELECT 1 FROM pets WHERE id = p_id) THEN
        RAISE EXCEPTION $$Pet with Id % does not exist$$, p_id
            USING ERRCODE = $$P0001$$;
    END IF;

    -- Update Pet Attribute
    UPDATE pets
    SET
        name = p_name,
        status = p_status
    WHERE id = p_id;

END;
' LANGUAGE plpgsql;

-- USER PROCEDURE --

DROP PROCEDURE IF EXISTS insert_user;
CREATE OR REPLACE PROCEDURE insert_user(
    IN u_id BIGINT,
    IN u_username VARCHAR,
    IN u_firstname VARCHAR,
    IN u_lastname VARCHAR,
    IN u_email VARCHAR,
    IN u_password VARCHAR,
    IN u_phone VARCHAR,
    IN u_userStatus INTEGER
)
AS '
BEGIN
    -- Insert to users
    INSERT INTO users (id, username, firstname, lastname, email, password_hash, phone, user_status)
    VALUES (u_id, u_username, u_firstname, u_lastname, u_email, u_password, u_phone, u_userStatus);
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS get_user_by_username;
CREATE OR REPLACE PROCEDURE get_user_by_username(
    IN u_username VARCHAR,
    OUT result REFCURSOR
)
AS '
BEGIN
    OPEN result FOR
    SELECT
     u.id AS user_id,
     u.username AS user_username,
     u.firstname AS user_firstname,
     u.lastname AS user_lastname,
     u.email AS user_email,
     u.password_hash AS user_password,
     u.phone AS user_phone,
     u.user_status AS user_status
    FROM users u
    WHERE u.username = u_username;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS update_user;
CREATE OR REPLACE PROCEDURE update_user(
    IN u_id BIGINT,
    IN u_username VARCHAR,
    IN u_firstname VARCHAR,
    IN u_lastname VARCHAR,
    IN u_email VARCHAR,
    IN u_password VARCHAR,
    IN u_phone VARCHAR,
    IN u_userStatus INTEGER
)
AS '
BEGIN
    -- Update user
    INSERT INTO users (id, username, firstname, lastname, email, password_hash, phone, user_status)
    VALUES (u_id, u_username, u_firstname, u_lastname, u_email, u_password, u_phone, u_userStatus)
    ON CONFLICT (id) DO UPDATE
    SET
        username = EXCLUDED.username,
        firstname = EXCLUDED.firstname,
        lastname = EXCLUDED.lastname,
        email = EXCLUDED.email,
        password_hash = EXCLUDED.password_hash,
        phone = EXCLUDED.phone,
        user_status = EXCLUDED.user_status;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS delete_user;
CREATE OR REPLACE PROCEDURE delete_user(IN u_username VARCHAR)
AS '
BEGIN
    -- Check if user exists
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = u_username) THEN
        RAISE EXCEPTION $$User with username % does not exists$$, u_username
            USING ERRCODE = $$U0001$$;
    END IF;

    -- Delete User
    DELETE FROM users
    WHERE username = u_username;
END;
' LANGUAGE plpgsql;

-- STORE PROCEDURE --

DROP PROCEDURE IF EXISTS get_inventory;
CREATE OR REPLACE PROCEDURE get_inventory(OUT result REFCURSOR)
AS '
BEGIN
   OPEN result FOR
   SELECT
    p.status AS pet_status,
    COUNT(*) AS pet_status_count
   FROM pets p
   GROUP BY p.status;
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS add_order;
CREATE OR REPLACE PROCEDURE add_order(
    IN o_id BIGINT,
    IN o_pet_id BIGINT,
    IN o_quantity INTEGER,
    IN o_ship_date TIMESTAMPTZ,
    IN o_status order_status,
    IN o_complete BOOLEAN
)
AS '
BEGIN
    -- Add new order but throws error if order id already exists
    INSERT INTO orders (id, pet_id, quantity, ship_date, status, complete)
    VALUES (o_id, o_pet_id, o_quantity, o_ship_date, o_status, o_complete);
END;
' LANGUAGE plpgsql;

DROP PROCEDURE IF EXISTS get_order;
CREATE OR REPLACE PROCEDURE get_order(
    IN o_id BIGINT,
    OUT result REFCURSOR
)
AS '
BEGIN
    OPEN result FOR
    SELECT
        o.id AS order_id,
        o.pet_id AS order_pet_id,
        o.quantity AS order_quantity,
        o.ship_date AS order_ship_date,
        o.status AS order_status,
        o.complete AS order_complete
    FROM orders o
    WHERE o.id = o_id;
END;
' LANGUAGE plpgsql;