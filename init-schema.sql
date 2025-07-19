-- Schema initialization for PostgreSQL/Supabase
-- Create tables in dependency order to avoid foreign key issues

-- Drop tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS user_interest CASCADE;
DROP TABLE IF EXISTS user_settings CASCADE;
DROP TABLE IF EXISTS user_audio CASCADE;
DROP TABLE IF EXISTS user_verify_picture CASCADE;
DROP TABLE IF EXISTS user_profile_picture CASCADE;
DROP TABLE IF EXISTS message CASCADE;
DROP TABLE IF EXISTS conversation CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS interest CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS gender CASCADE;
DROP TABLE IF EXISTS captcha CASCADE;

-- Create captcha table
CREATE TABLE IF NOT EXISTS captcha (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(255) NOT NULL,
    hashcode VARCHAR(255),
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create gender table
CREATE TABLE IF NOT EXISTS gender (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(255) NOT NULL
);

-- Create user_role table
CREATE TABLE IF NOT EXISTS user_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create interest table
CREATE TABLE IF NOT EXISTS interest (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(255) NOT NULL
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(255),
    description TEXT,
    prefered_min_age INTEGER,
    prefered_max_age INTEGER,
    prefered_gender_id BIGINT,
    location_latitude DOUBLE PRECISION,
    location_longitude DOUBLE PRECISION,
    intention_id BIGINT,
    min_age INTEGER,
    max_age INTEGER,
    distance INTEGER,
    total_donations DOUBLE PRECISION DEFAULT 0,
    show_zodiac BOOLEAN DEFAULT FALSE,
    zodiac INTEGER,
    accent_color VARCHAR(7),
    ui_design INTEGER DEFAULT 1,
    privacy INTEGER DEFAULT 1,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_date TIMESTAMP,
    confirmed BOOLEAN DEFAULT FALSE,
    disabled BOOLEAN DEFAULT FALSE,
    admin BOOLEAN DEFAULT FALSE,
    uuid VARCHAR(255),
    language VARCHAR(10),
    FOREIGN KEY (prefered_gender_id) REFERENCES gender(id)
);

-- Create conversation table
CREATE TABLE IF NOT EXISTS conversation (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create message table
CREATE TABLE IF NOT EXISTS message (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_from_id BIGINT NOT NULL,
    user_to_id BIGINT NOT NULL,
    text TEXT,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE,
    FOREIGN KEY (user_from_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_to_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_profile_picture table with TEXT instead of mediumtext
CREATE TABLE IF NOT EXISTS user_profile_picture (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    data TEXT,
    image TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_verify_picture table
CREATE TABLE IF NOT EXISTS user_verify_picture (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    data TEXT,
    image TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_audio table
CREATE TABLE IF NOT EXISTS user_audio (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    data TEXT,
    bin BYTEA,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_settings table
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_chat BOOLEAN DEFAULT TRUE,
    email_like BOOLEAN DEFAULT TRUE,
    push_chat BOOLEAN DEFAULT TRUE,
    push_like BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_image table
CREATE TABLE IF NOT EXISTS user_image (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT,
    bin BYTEA,
    bin_mime VARCHAR(255),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_interest table with correct syntax
CREATE TABLE IF NOT EXISTS user_interest (
    user_id BIGINT NOT NULL,
    interest_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, interest_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES interest(id) ON DELETE CASCADE
);

-- Insert some basic data
INSERT INTO gender (text) VALUES ('Male'), ('Female'), ('Other');
INSERT INTO user_role (name) VALUES ('USER'), ('ADMIN');
INSERT INTO interest (text) VALUES ('Music'), ('Sports'), ('Travel'), ('Movies'), ('Books'), ('Cooking'), ('Art'), ('Technology');
-- Add other necessary tables as needed
-- You can add more tables based on errors that appear during startup

-- Create necessary indexes for performance
CREATE INDEX IF NOT EXISTS "idx_user_email" ON "users" ("email");
CREATE INDEX IF NOT EXISTS "idx_user_uuid" ON "users" ("uuid");
