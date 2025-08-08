-- Initialize database for Electronic Signature Service
-- First check if database exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'signature_db') THEN
        CREATE DATABASE signature_db;
    END IF;
END
$$;

-- Create user if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'signature_user') THEN
        CREATE USER signature_user WITH PASSWORD 'signature_pass';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE signature_db TO signature_user;

-- Grant schema privileges (run this after connecting to signature_db in psql)
-- Note: The following command should be run after connecting to signature_db:
-- GRANT ALL ON SCHEMA public TO signature_user;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO signature_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO signature_user;
