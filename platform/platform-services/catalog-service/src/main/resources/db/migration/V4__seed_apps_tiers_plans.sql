-- Seed data per doc §1's pricing table. Annual only (Phase 2 decision) - prices are
-- 10x the monthly list price per §15.5.2, already GST-exclusive; gst_rate_bps=1800
-- means 18%. price_paise is integer rupees*100, never a double.

INSERT INTO apps (key, name, description) VALUES
    ('restaurant', 'Swami Restaurant', 'Menu, tables, orders, KOT, billing, reservations, staff shifts'),
    ('coffee-shop', 'Coffee Shop', 'Quick-serve menu, modifiers, queue tokens, loyalty stamps'),
    ('bar-restro', 'Bar & Restro', 'Bar inventory, happy-hour pricing, tab management, cocktail recipes'),
    ('hotel', 'Hotel Management', 'Rooms, rate plans, availability, bookings, check-in/out, housekeeping');

INSERT INTO tiers (app_id, key, name, sort_order)
SELECT id, 'standard', 'Standard', 0 FROM apps WHERE key IN ('restaurant', 'coffee-shop', 'bar-restro');

INSERT INTO tiers (app_id, key, name, sort_order)
SELECT id, 'large', 'Large', 0 FROM apps WHERE key = 'hotel';

INSERT INTO plans (tier_id, plan_key, billing_interval, price_paise, gst_rate_bps)
SELECT t.id, a.key || '-annual', 'ANNUAL', 5000000, 1800
FROM tiers t JOIN apps a ON a.id = t.app_id
WHERE a.key IN ('restaurant', 'coffee-shop', 'bar-restro');

INSERT INTO plans (tier_id, plan_key, billing_interval, price_paise, gst_rate_bps)
SELECT t.id, a.key || '-annual', 'ANNUAL', 10000000, 1800
FROM tiers t JOIN apps a ON a.id = t.app_id
WHERE a.key = 'hotel';
