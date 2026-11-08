-- Full initial seed script for manual execution in psql or database clients
-- (Note: Spring Boot automatically seeds these via DatabaseSeeder.java on startup)

INSERT INTO products (name, description, price, stock_quantity, category, created_at, updated_at)
VALUES 
-- Laptops & Computers
('MacBook Pro 16" M3 Max', 'Apple M3 Max chip with 16-core CPU, 40-core GPU, 36GB RAM, 1TB SSD', 3499.00, 15, 'Laptops', NOW(), NOW()),
('MacBook Air 15" M3', 'Thin and lightweight Apple M3 laptop with 16GB RAM, 512GB SSD, Liquid Retina', 1499.00, 28, 'Laptops', NOW(), NOW()),
('Dell XPS 15 OLED', '15.6" 3.5K OLED touch display, Intel Core i9, 32GB RAM, 1TB SSD, RTX 4070', 2399.50, 20, 'Laptops', NOW(), NOW()),
('Lenovo ThinkPad X1 Carbon Gen 12', 'Ultralight business laptop with Intel Core Ultra 7, 32GB RAM, 14" 2.8K OLED', 1899.00, 22, 'Laptops', NOW(), NOW()),
('ASUS ROG Zephyrus G16', 'Ultra-slim gaming laptop with Intel Core Ultra 9, RTX 4080, 240Hz OLED', 2699.99, 14, 'Laptops', NOW(), NOW()),
('Apple Mac mini M4 Pro', 'Compact desktop with M4 Pro chip, 24GB RAM, 512GB SSD, Thunderbolt 5', 1399.00, 30, 'Computers', NOW(), NOW()),

-- Mobile & Tablets
('iPhone 16 Pro 256GB', 'Grade 5 titanium design, A18 Pro chip, 48MP Fusion camera with 5x Telephoto', 1099.00, 40, 'Mobile', NOW(), NOW()),
('Samsung Galaxy S25 Ultra 512GB', 'Snapdragon 8 Elite, 200MP camera system, built-in S Pen, Titanium frame', 1299.99, 35, 'Mobile', NOW(), NOW()),
('Google Pixel 9 Pro 256GB', 'Google Tensor G4, pro triple camera system, Gemini AI built-in', 999.00, 25, 'Mobile', NOW(), NOW()),
('iPad Pro 13" M4 OLED', 'Ultra Retina XDR OLED display, Apple M4 chip, 256GB storage, LiDAR', 1299.00, 24, 'Tablets', NOW(), NOW()),
('iPad Air 11" M2', 'Apple M2 chip, 128GB storage, 11" Liquid Retina display, Touch ID', 599.00, 32, 'Tablets', NOW(), NOW()),

-- Audio
('Sony WH-1000XM5 Wireless Headphones', 'Industry-leading noise canceling headphones with 8 microphones', 399.99, 45, 'Audio', NOW(), NOW()),
('Bose QuietComfort Ultra', 'Spatial audio, world-class noise cancellation, 24-hour battery', 429.00, 30, 'Audio', NOW(), NOW()),
('Apple AirPods Pro 2 (USB-C)', 'Active Noise Cancellation with Transparency mode, Personalized Spatial Audio', 249.00, 70, 'Audio', NOW(), NOW()),
('Sennheiser Momentum 4 Wireless', 'Audiophile-inspired 42mm transducer system with 60-hour battery', 349.95, 25, 'Audio', NOW(), NOW()),
('Shure SM7B Vocal Dynamic Microphone', 'Legendary studio dynamic microphone with flat frequency response', 399.00, 18, 'Audio', NOW(), NOW()),
('Audio-Technica ATH-M50x Professional Monitor Headphones', 'Critically acclaimed studio monitor headphones with 45mm drivers', 149.00, 40, 'Audio', NOW(), NOW()),

-- Peripherals
('Keychron Q1 Pro Mechanical Keyboard', '75% layout wireless custom mechanical keyboard, aluminum body', 199.99, 35, 'Peripherals', NOW(), NOW()),
('NuPhy Air75 V2 Low-Profile Wireless Keyboard', 'Ultra-thin 75% mechanical keyboard with PBT keycaps, RGB', 129.95, 25, 'Peripherals', NOW(), NOW()),
('Logitech MX Master 3S Mouse', 'Performance wireless mouse with 8K DPI sensor and Quiet Clicks', 99.99, 60, 'Peripherals', NOW(), NOW()),
('Apple Magic Trackpad', 'Wireless and rechargeable trackpad with full Force Touch gestures', 149.00, 30, 'Peripherals', NOW(), NOW()),
('Razer DeathAdder V3 Pro Wireless Gaming Mouse', 'Ultra-lightweight 63g ergonomic esports mouse with 30K sensor', 149.99, 25, 'Peripherals', NOW(), NOW()),
('Elgato Stream Deck MK.2', 'Studio controller with 15 customizable LCD keys', 149.99, 20, 'Peripherals', NOW(), NOW()),

-- Monitors
('LG UltraFine 32" 4K Ergo Monitor', '31.5" UHD IPS Display with Ergo Arm Stand', 649.99, 18, 'Monitors', NOW(), NOW()),
('Dell UltraSharp 34" Curved USB-C Hub Monitor', 'WQHD (3440 x 1440) IPS Black with built-in KVM and 90W PD', 899.99, 15, 'Monitors', NOW(), NOW()),
('ASUS ProArt Display 27" 4K HDR', 'Factory pre-calibrated Delta E < 2 for color accuracy, 100% sRGB', 499.00, 16, 'Monitors', NOW(), NOW()),
('Samsung Odyssey OLED G9 49" Curved Gaming Monitor', 'Dual QHD (5120x1440) 1800R curved OLED, 240Hz, 0.03ms', 1599.99, 8, 'Monitors', NOW(), NOW()),

-- Office Furniture & Accessories
('Herman Miller Aeron Ergonomic Chair', 'Pellicle 8Z suspension seat and back with PostureFit SL', 1295.00, 8, 'Office Furniture', NOW(), NOW()),
('Steelcase Gesture Office Chair', 'Designed for modern tech postures with 360-degree adjustable arms', 1380.00, 10, 'Office Furniture', NOW(), NOW()),
('Fully Jarvis Bamboo Standing Desk', 'Dual-motor electric height-adjustable desk with bamboo top', 799.00, 12, 'Office Furniture', NOW(), NOW()),
('BenQ ScreenBar Halo Monitor Light', 'Wireless controller, back-ambient light, non-glare optics', 179.00, 35, 'Accessories', NOW(), NOW()),
('CalDigit TS4 Thunderbolt 4 Dock', '18 ports of extreme connectivity with 98W PD and 2.5GbE', 399.95, 22, 'Accessories', NOW(), NOW()),
('Anker Prime 20,000mAh Power Bank (200W)', 'Multi-device fast charging power bank with digital smart display', 129.99, 50, 'Accessories', NOW(), NOW())
ON CONFLICT DO NOTHING;
