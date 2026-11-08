package com.practice.java_refresher.controller;

import com.practice.java_refresher.dto.*;
import com.practice.java_refresher.exception.InsufficientStockException;
import com.practice.java_refresher.service.AuthService;
import com.practice.java_refresher.service.CartService;
import com.practice.java_refresher.service.OrderService;
import com.practice.java_refresher.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebViewController {

    private final ProductService productService;
    private final AuthService authService;
    private final CartService cartService;
    private final OrderService orderService;
    private final org.springframework.batch.core.launch.JobOperator jobOperator;
    private final org.springframework.batch.core.job.Job productPipelineJob;
    private final com.practice.java_refresher.batch.util.CsvDataGenerator csvDataGenerator;

    public WebViewController(ProductService productService,
                             AuthService authService,
                             CartService cartService,
                             OrderService orderService,
                             org.springframework.batch.core.launch.JobOperator jobOperator,
                             org.springframework.batch.core.job.Job productPipelineJob,
                             com.practice.java_refresher.batch.util.CsvDataGenerator csvDataGenerator) {
        this.productService = productService;
        this.authService = authService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.jobOperator = jobOperator;
        this.productPipelineJob = productPipelineJob;
        this.csvDataGenerator = csvDataGenerator;
    }

    // --- HOME & CATALOG ---

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        Page<ProductResponseDto> featured = productService.getAllProducts(null, null, PageRequest.of(0, 6));
        model.addAttribute("products", featured.getContent());
        model.addAttribute("totalProducts", featured.getTotalElements());
        model.addAttribute("cart", cartService.getCart(session));
        return "index";
    }

    @GetMapping("/web/products")
    public String listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model,
            HttpSession session) {
        Page<ProductResponseDto> productPage = productService.getAllProducts(category, search, PageRequest.of(page, size));
        model.addAttribute("productPage", productPage);
        model.addAttribute("category", category);
        model.addAttribute("search", search);
        model.addAttribute("cart", cartService.getCart(session));
        return "products/list";
    }

    @GetMapping("/web/products/{id}")
    public String productDetails(@PathVariable Long id, Model model, HttpSession session) {
        ProductResponseDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("cart", cartService.getCart(session));
        return "products/details";
    }

    @GetMapping("/web/products/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newProductForm(Model model, HttpSession session) {
        model.addAttribute("productForm", new ProductRequestDto());
        model.addAttribute("cart", cartService.getCart(session));
        return "products/form";
    }

    @PostMapping("/web/products")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveProduct(
            @Valid @ModelAttribute("productForm") ProductRequestDto productForm,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cartService.getCart(session));
            return "products/form";
        }
        productService.createProduct(productForm);
        return "redirect:/web/products";
    }

    // --- AUTHENTICATION & REGISTRATION ---

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterRequestDto());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(
            @Valid @ModelAttribute("registerForm") RegisterRequestDto registerForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            authService.register(registerForm);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }

    // --- SHOPPING CART ---

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        CartDto cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        cartService.addToCart(session, productId, quantity);
        redirectAttributes.addFlashAttribute("cartSuccess", "Item added to cart!");
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCartQuantity(@RequestParam Long productId,
                                     @RequestParam int quantity,
                                     HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId, HttpSession session) {
        cartService.removeFromCart(session, productId);
        return "redirect:/cart";
    }

    // --- CHECKOUT & ORDERS ---

    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String checkoutView(Model model, HttpSession session) {
        CartDto cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        return "checkout";
    }

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String processCheckout(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            OrderResponseDto placedOrder = cartService.checkout(session);
            redirectAttributes.addFlashAttribute("orderSuccess", true);
            return "redirect:/web/orders/" + placedOrder.getOrderNumber();
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("stockError", ex.getMessage());
            return "redirect:/cart";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/web/orders")
    @PreAuthorize("isAuthenticated()")
    public String listUserOrders(Model model, HttpSession session) {
        List<OrderResponseDto> orders = orderService.getCurrentUserOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("cart", cartService.getCart(session));
        return "orders/list";
    }

    @GetMapping("/web/orders/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    public String viewOrderReceipt(@PathVariable String orderNumber, Model model, HttpSession session) {
        OrderResponseDto order = orderService.getOrderByOrderNumber(orderNumber);
        model.addAttribute("order", order);
        model.addAttribute("cart", cartService.getCart(session));
        return "orders/details";
    }

    // --- SPRING BATCH DEMO ---

        @GetMapping("/web/batch")
    public String batchDashboard(Model model, HttpSession session) {
        model.addAttribute("cart", cartService.getCart(session));
        java.io.File csvFile = new java.io.File("data/products_10k.csv");
        if (!csvFile.exists()) {
            csvFile = new java.io.File("data/products_large.csv");
        }
        java.io.File jsonFile = new java.io.File("data/exported_products.json");

        model.addAttribute("csvExists", csvFile.exists());
        model.addAttribute("csvSizeKb", csvFile.exists() ? csvFile.length() / 1024 : 0);
        model.addAttribute("jsonExists", jsonFile.exists());
        model.addAttribute("jsonSizeKb", jsonFile.exists() ? jsonFile.length() / 1024 : 0);
        return "batch";
    }

    @PostMapping("/web/batch/run")
    public String runBatchDemo(@RequestParam(defaultValue = "data/products_10k.csv") String inputCsvPath,
                               RedirectAttributes redirectAttributes) {
        try {
            java.io.File csvFile = new java.io.File(inputCsvPath);
            if (!csvFile.exists()) {
                csvDataGenerator.generateLargeCsv(inputCsvPath, 2000);
            }

            org.springframework.batch.core.job.parameters.JobParameters params = new org.springframework.batch.core.job.parameters.JobParametersBuilder()
                    .addString("inputCsvPath", inputCsvPath)
                    .addString("outputJsonPath", "data/exported_products.json")
                    .addLong("startedAt", System.currentTimeMillis())
                    .toJobParameters();

            org.springframework.batch.core.job.JobExecution execution = jobOperator.start(productPipelineJob, params);

            redirectAttributes.addFlashAttribute("batchSuccess", true);
            redirectAttributes.addFlashAttribute("jobId", execution.getId());
            redirectAttributes.addFlashAttribute("status", execution.getStatus().toString());
            return "redirect:/web/batch";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("batchError", ex.getMessage());
            return "redirect:/web/batch";
        }
    }
}
