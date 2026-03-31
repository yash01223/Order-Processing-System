-- One-time cleanup: clear stale orders so status_updated_at column starts fresh.
-- IMPORTANT: Remove or comment out this file after the first successful startup,
-- otherwise it will wipe all orders on every restart.
TRUNCATE TABLE orders CASCADE;
