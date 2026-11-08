package com.practice.java_refresher.config;

import com.practice.java_refresher.model.Order;
import com.practice.java_refresher.model.OrderItem;
import com.practice.java_refresher.model.OrderStatus;
import com.practice.java_refresher.model.Product;
import com.practice.java_refresher.repository.OrderRepository;
import com.practice.java_refresher.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final com.practice.java_refresher.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DatabaseSeeder(ProductRepository productRepository,
                          OrderRepository orderRepository,
                          com.practice.java_refresher.repository.UserRepository userRepository,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding sample e-commerce data...");

        seedUsers();
        seedProducts();
        seedOrders();

        log.info("Database seeding check complete. Current totals: {} users, {} products, {} orders.",
                userRepository.count(), productRepository.count(), orderRepository.count());
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("user")) {
            userRepository.save(com.practice.java_refresher.model.User.builder()
                    .username("user")
                    .email("user@example.com")
                    .fullName("Standard Customer")
                    .password(passwordEncoder.encode("user123"))
                    .roles(java.util.Set.of("ROLE_USER"))
                    .build());
            log.info("Seeded default test user: user / user123");
        }

        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(com.practice.java_refresher.model.User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .fullName("System Administrator")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(java.util.Set.of("ROLE_ADMIN", "ROLE_USER"))
                    .build());
            log.info("Seeded default admin user: admin / admin123");
        }
    }

    private void seedProducts() {
        List<Product> catalog = List.of(
                // --- LAPTOPS & COMPUTERS ---
                Product.builder()
                        .name("MacBook Pro 16\" M3 Max")
                        .category("Laptops")
                        .description("Apple M3 Max chip with 16-core CPU, 40-core GPU, 36GB Unified Memory, and 1TB SSD storage.")
                        .price(new BigDecimal("3499.00"))
                        .stockQuantity(15)
                        .build(),
                Product.builder()
                        .name("MacBook Air 15\" M3")
                        .category("Laptops")
                        .description("Thin and lightweight Apple M3 laptop with 16GB RAM, 512GB SSD, Liquid Retina display, Midnight finish.")
                        .price(new BigDecimal("1499.00"))
                        .stockQuantity(28)
                        .build(),
                Product.builder()
                        .name("Dell XPS 15 OLED")
                        .category("Laptops")
                        .description("15.6-inch 3.5K OLED InfinityEdge touch display, Intel Core i9, 32GB RAM, 1TB NVMe SSD, NVIDIA RTX 4070.")
                        .price(new BigDecimal("2399.50"))
                        .stockQuantity(20)
                        .build(),
                Product.builder()
                        .name("Lenovo ThinkPad X1 Carbon Gen 12")
                        .category("Laptops")
                        .description("Ultralight business laptop with Intel Core Ultra 7, 32GB LPDDR5X, 1TB SSD, 14\" 2.8K OLED 120Hz display.")
                        .price(new BigDecimal("1899.00"))
                        .stockQuantity(22)
                        .build(),
                Product.builder()
                        .name("ASUS ROG Zephyrus G16")
                        .category("Laptops")
                        .description("Ultra-slim gaming laptop with Intel Core Ultra 9, RTX 4080, 32GB RAM, 2TB SSD, 240Hz OLED Nebula display.")
                        .price(new BigDecimal("2699.99"))
                        .stockQuantity(14)
                        .build(),
                Product.builder()
                        .name("Apple Mac mini M4 Pro")
                        .category("Computers")
                        .description("Compact desktop with M4 Pro chip, 24GB Unified Memory, 512GB SSD, Thunderbolt 5 ports.")
                        .price(new BigDecimal("1399.00"))
                        .stockQuantity(30)
                        .build(),

                // --- SMARTPHONES & TABLETS ---
                Product.builder()
                        .name("iPhone 16 Pro 256GB")
                        .category("Mobile")
                        .description("Grade 5 titanium design, A18 Pro chip, 48MP Fusion camera with 5x Telephoto, 6.3\" Super Retina XDR.")
                        .price(new BigDecimal("1099.00"))
                        .stockQuantity(40)
                        .build(),
                Product.builder()
                        .name("Samsung Galaxy S25 Ultra 512GB")
                        .category("Mobile")
                        .description("Snapdragon 8 Elite, 200MP camera system, built-in S Pen, Titanium frame, 6.8\" Dynamic AMOLED 2X.")
                        .price(new BigDecimal("1299.99"))
                        .stockQuantity(35)
                        .build(),
                Product.builder()
                        .name("Google Pixel 9 Pro 256GB")
                        .category("Mobile")
                        .description("Google Tensor G4, pro triple camera system with Super Res Zoom up to 30x, Gemini AI built-in, Obsidian.")
                        .price(new BigDecimal("999.00"))
                        .stockQuantity(25)
                        .build(),
                Product.builder()
                        .name("iPad Pro 13\" M4 OLED")
                        .category("Tablets")
                        .description("Ultra Retina XDR OLED display, Apple M4 chip, 256GB storage, LiDAR Scanner, Space Black.")
                        .price(new BigDecimal("1299.00"))
                        .stockQuantity(24)
                        .build(),
                Product.builder()
                        .name("iPad Air 11\" M2")
                        .category("Tablets")
                        .description("Apple M2 chip, 128GB storage, 11\" Liquid Retina display, Touch ID, Starlight.")
                        .price(new BigDecimal("599.00"))
                        .stockQuantity(32)
                        .build(),

                // --- AUDIO & HEADPHONES ---
                Product.builder()
                        .name("Sony WH-1000XM5 Wireless Headphones")
                        .category("Audio")
                        .description("Industry-leading active noise canceling headphones with dual processors and 8 microphones.")
                        .price(new BigDecimal("399.99"))
                        .stockQuantity(45)
                        .build(),
                Product.builder()
                        .name("Bose QuietComfort Ultra")
                        .category("Audio")
                        .description("Spatial audio, world-class noise cancellation, and all-day comfort with up to 24 hours battery life.")
                        .price(new BigDecimal("429.00"))
                        .stockQuantity(30)
                        .build(),
                Product.builder()
                        .name("Apple AirPods Pro 2 (USB-C)")
                        .category("Audio")
                        .description("Active Noise Cancellation with Transparency mode, Personalized Spatial Audio, MagSafe Case (USB-C).")
                        .price(new BigDecimal("249.00"))
                        .stockQuantity(70)
                        .build(),
                Product.builder()
                        .name("Sennheiser Momentum 4 Wireless")
                        .category("Audio")
                        .description("Audiophile-inspired 42mm transducer system with exceptional 60-hour battery life and adaptive ANC.")
                        .price(new BigDecimal("349.95"))
                        .stockQuantity(25)
                        .build(),
                Product.builder()
                        .name("Shure SM7B Vocal Dynamic Microphone")
                        .category("Audio")
                        .description("Legendary studio dynamic microphone with flat, wide-range frequency response for speech and music.")
                        .price(new BigDecimal("399.00"))
                        .stockQuantity(18)
                        .build(),
                Product.builder()
                        .name("Audio-Technica ATH-M50x Professional Monitor Headphones")
                        .category("Audio")
                        .description("Critically acclaimed studio monitor headphones with 45mm large-aperture drivers and 90° swiveling earcups.")
                        .price(new BigDecimal("149.00"))
                        .stockQuantity(40)
                        .build(),

                // --- PERIPHERALS & ACCESSORIES ---
                Product.builder()
                        .name("Keychron Q1 Pro Mechanical Keyboard")
                        .category("Peripherals")
                        .description("75% layout wireless custom mechanical keyboard, fully aluminum body, hot-swappable switches, QMK/VIA support.")
                        .price(new BigDecimal("199.99"))
                        .stockQuantity(35)
                        .build(),
                Product.builder()
                        .name("NuPhy Air75 V2 Low-Profile Wireless Keyboard")
                        .category("Peripherals")
                        .description("Ultra-thin 75% mechanical keyboard with PBT keycaps, RGB backlighting, 2.4G/Bluetooth/USB-C connection.")
                        .price(new BigDecimal("129.95"))
                        .stockQuantity(25)
                        .build(),
                Product.builder()
                        .name("Logitech MX Master 3S Mouse")
                        .category("Peripherals")
                        .description("Performance wireless mouse with 8K DPI track-on-glass sensor and Quiet Clicks.")
                        .price(new BigDecimal("99.99"))
                        .stockQuantity(60)
                        .build(),
                Product.builder()
                        .name("Apple Magic Trackpad")
                        .category("Peripherals")
                        .description("Wireless and rechargeable trackpad with full Force Touch gestures and edge-to-edge glass surface.")
                        .price(new BigDecimal("149.00"))
                        .stockQuantity(30)
                        .build(),
                Product.builder()
                        .name("Razer DeathAdder V3 Pro Wireless Gaming Mouse")
                        .category("Peripherals")
                        .description("Ultra-lightweight 63g ergonomic esports mouse with Focus Pro 30K Optical Sensor.")
                        .price(new BigDecimal("149.99"))
                        .stockQuantity(25)
                        .build(),
                Product.builder()
                        .name("Elgato Stream Deck MK.2")
                        .category("Peripherals")
                        .description("Studio controller with 15 customizable LCD keys for triggering actions in apps and streaming setups.")
                        .price(new BigDecimal("149.99"))
                        .stockQuantity(20)
                        .build(),

                // --- MONITORS & DISPLAYS ---
                Product.builder()
                        .name("LG UltraFine 32\" 4K Ergo Monitor")
                        .category("Monitors")
                        .description("31.5\" UHD (3840 x 2160) IPS Display with Ergo Stand (Extend/Retract, Swivel, Pivot, Height, Tilt).")
                        .price(new BigDecimal("649.99"))
                        .stockQuantity(18)
                        .build(),
                Product.builder()
                        .name("Dell UltraSharp 34\" Curved USB-C Hub Monitor")
                        .category("Monitors")
                        .description("WQHD (3440 x 1440) IPS Black technology with 2000:1 contrast ratio, 90W power delivery, built-in KVM.")
                        .price(new BigDecimal("899.99"))
                        .stockQuantity(15)
                        .build(),
                Product.builder()
                        .name("ASUS ProArt Display 27\" 4K HDR")
                        .category("Monitors")
                        .description("Factory pre-calibrated Delta E < 2 for color accuracy, 100% sRGB, 100% Rec. 709, USB-C 65W PD.")
                        .price(new BigDecimal("499.00"))
                        .stockQuantity(16)
                        .build(),
                Product.builder()
                        .name("Samsung Odyssey OLED G9 49\" Curved Gaming Monitor")
                        .category("Monitors")
                        .description("Dual QHD (5120x1440) 1800R curved OLED display, 240Hz refresh rate, 0.03ms response time, Neo Quantum Processor.")
                        .price(new BigDecimal("1599.99"))
                        .stockQuantity(8)
                        .build(),

                // --- OFFICE & DESK SETUP ---
                Product.builder()
                        .name("Herman Miller Aeron Ergonomic Chair")
                        .category("Office Furniture")
                        .description("Pellicle 8Z suspension seat and back with PostureFit SL adjustable sacral and lumbar support.")
                        .price(new BigDecimal("1295.00"))
                        .stockQuantity(8)
                        .build(),
                Product.builder()
                        .name("Steelcase Gesture Office Chair")
                        .category("Office Furniture")
                        .description("Designed for modern tech postures with 360-degree adjustable arms and Core Equalizer lumbar support.")
                        .price(new BigDecimal("1380.00"))
                        .stockQuantity(10)
                        .build(),
                Product.builder()
                        .name("Fully Jarvis Bamboo Standing Desk")
                        .category("Office Furniture")
                        .description("Dual-motor electric height-adjustable standing desk with sustainable bamboo desktop.")
                        .price(new BigDecimal("799.00"))
                        .stockQuantity(12)
                        .build(),
                Product.builder()
                        .name("BenQ ScreenBar Halo Monitor Light")
                        .category("Accessories")
                        .description("Wireless controller, back-ambient light, non-glare asymmetric optical design, automatic dimming sensor.")
                        .price(new BigDecimal("179.00"))
                        .stockQuantity(35)
                        .build(),
                Product.builder()
                        .name("CalDigit TS4 Thunderbolt 4 Dock")
                        .category("Accessories")
                        .description("18 ports of extreme connectivity including 98W Power Delivery, 2.5GbE LAN, 5x USB-A, 3x USB-C, DisplayPort 1.4.")
                        .price(new BigDecimal("399.95"))
                        .stockQuantity(22)
                        .build(),
                Product.builder()
                        .name("Anker Prime 20,000mAh Power Bank (200W)")
                        .category("Accessories")
                        .description("Multi-device fast charging power bank with digital smart display and 200W total output.")
                        .price(new BigDecimal("129.99"))
                        .stockQuantity(50)
                        .build()
        );

        int newlySaved = 0;
        for (Product product : catalog) {
            if (!productRepository.existsByName(product.getName())) {
                productRepository.save(product);
                newlySaved++;
            }
        }
        if (newlySaved > 0) {
            log.info("Inserted {} new products into catalog.", newlySaved);
        }
    }

    private void seedOrders() {
        createSampleOrderIfNotExists("ORD-2026-A101", "user", "user@example.com", OrderStatus.DELIVERED,
                List.of(new SeedItem("Keychron Q1 Pro Mechanical Keyboard", 1),
                        new SeedItem("Logitech MX Master 3S Mouse", 1)));

        createSampleOrderIfNotExists("ORD-2026-B202", "user", "user@example.com", OrderStatus.SHIPPED,
                List.of(new SeedItem("Sony WH-1000XM5 Wireless Headphones", 1)));

        createSampleOrderIfNotExists("ORD-2026-C303", "admin", "admin@example.com", OrderStatus.PENDING,
                List.of(new SeedItem("MacBook Pro 16\" M3 Max", 1),
                        new SeedItem("CalDigit TS4 Thunderbolt 4 Dock", 1)));

        createSampleOrderIfNotExists("ORD-2026-D404", "user", "user@example.com", OrderStatus.PROCESSING,
                List.of(new SeedItem("iPad Pro 13\" M4 OLED", 1),
                        new SeedItem("Apple AirPods Pro 2 (USB-C)", 1)));

        createSampleOrderIfNotExists("ORD-2026-E505", "user", "user@example.com", OrderStatus.DELIVERED,
                List.of(new SeedItem("LG UltraFine 32\" 4K Ergo Monitor", 1),
                        new SeedItem("BenQ ScreenBar Halo Monitor Light", 1)));

        createSampleOrderIfNotExists("ORD-2026-F606", "admin", "admin@example.com", OrderStatus.DELIVERED,
                List.of(new SeedItem("Dell UltraSharp 34\" Curved USB-C Hub Monitor", 1),
                        new SeedItem("Keychron Q1 Pro Mechanical Keyboard", 2)));

        createSampleOrderIfNotExists("ORD-2026-G707", "user", "user@example.com", OrderStatus.CANCELLED,
                List.of(new SeedItem("Samsung Odyssey OLED G9 49\" Curved Gaming Monitor", 1)));

        createSampleOrderIfNotExists("ORD-2026-H808", "user", "user@example.com", OrderStatus.CONFIRMED,
                List.of(new SeedItem("Anker Prime 20,000mAh Power Bank (200W)", 2),
                        new SeedItem("Apple Magic Trackpad", 1)));

        createSampleOrderIfNotExists("ORD-2026-I909", "admin", "admin@example.com", OrderStatus.SHIPPED,
                List.of(new SeedItem("Herman Miller Aeron Ergonomic Chair", 1)));

        createSampleOrderIfNotExists("ORD-2026-J010", "user", "user@example.com", OrderStatus.PENDING,
                List.of(new SeedItem("iPhone 16 Pro 256GB", 1),
                        new SeedItem("Apple AirPods Pro 2 (USB-C)", 1)));
    }

    private void createSampleOrderIfNotExists(String orderNumber, String username, String email,
                                             OrderStatus status, List<SeedItem> seedItems) {
        if (orderRepository.findByOrderNumber(orderNumber).isPresent()) {
            return;
        }

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(username)
                .username(username)
                .userEmail(email)
                .status(status)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (SeedItem seedItem : seedItems) {
            Optional<Product> prodOpt = productRepository.findByName(seedItem.productName);
            if (prodOpt.isEmpty()) {
                continue;
            }
            Product prod = prodOpt.get();
            BigDecimal subtotal = prod.getPrice().multiply(BigDecimal.valueOf(seedItem.quantity));
            total = total.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(prod)
                    .productName(prod.getName())
                    .unitPrice(prod.getPrice())
                    .quantity(seedItem.quantity)
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);
    }

    private static class SeedItem {
        final String productName;
        final int quantity;

        SeedItem(String productName, int quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }
    }
}
