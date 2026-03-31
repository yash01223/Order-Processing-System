TRUNCATE TABLE orders CASCADE;

-- Ensure the is_verified column exists before updating
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE;

-- Mark existing users as verified so they are not locked out
UPDATE users SET is_verified = true;
