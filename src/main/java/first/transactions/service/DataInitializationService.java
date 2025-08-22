package first.transactions.service;

import first.transactions.model.*;
import first.transactions.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class DataInitializationService implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final InvestmentRepository investmentRepository;
    private final TransferRepository transferRepository;
    private final PortfolioService portfolioService;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializationService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            InvestmentRepository investmentRepository,
            TransferRepository transferRepository,
            PortfolioService portfolioService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.investmentRepository = investmentRepository;
        this.transferRepository = transferRepository;
        this.portfolioService = portfolioService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if database is empty
            System.out.println("🚀 Initializing database with sample data...");

            // Step 1: Create Users
            createSampleUsers();

            // Step 2: Create Companies
            createSampleCompanies();
            
            // Step 3: Create Investments
            createSampleInvestments();
            
            // Step 4: Create Transfers
//            createSampleTransfers();
            
            // Step 5: Initialize Portfolios (will be auto-created by service)
            initializePortfolios();
            
            System.out.println("✅ Database initialized successfully!");
            printSampleData();

    }
    
    private void createSampleUsers() {
        System.out.println("👥 Creating sample users...");
        
        // Super Admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@stockmarket.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.SUPER_ADMIN);
        admin.setBalance(10000.0);
        userRepository.save(admin);
        
        // Investors
        User investor1 = new User();
        investor1.setUsername("john_investor");
        investor1.setEmail("john@example.com");
        investor1.setPassword(passwordEncoder.encode("password123"));
        investor1.setRole(UserRole.INVESTOR);
        investor1.setBalance(15000.0);
        userRepository.save(investor1);
        
        User investor2 = new User();
        investor2.setUsername("sarah_trader");
        investor2.setEmail("sarah@example.com");
        investor2.setPassword(passwordEncoder.encode("password123"));
        investor2.setRole(UserRole.INVESTOR);
        investor2.setBalance(8500.0);
        userRepository.save(investor2);
        
        User investor3 = new User();
        investor3.setUsername("mike_portfolio");
        investor3.setEmail("mike@example.com");
        investor3.setPassword(passwordEncoder.encode("password123"));
        investor3.setRole(UserRole.INVESTOR);
        investor3.setBalance(22000.0);
        userRepository.save(investor3);
        
        // Company Representatives
        User company1 = new User();
        company1.setUsername("apple_rep");
        company1.setEmail("rep@apple.com");
        company1.setPassword(passwordEncoder.encode("company123"));
        company1.setRole(UserRole.COMPANY);
        company1.setBalance(50000.0);
        userRepository.save(company1);
        
        User company2 = new User();
        company2.setUsername("tesla_rep");
        company2.setEmail("rep@tesla.com");
        company2.setPassword(passwordEncoder.encode("company123"));
        company2.setRole(UserRole.COMPANY);
        company2.setBalance(75000.0);
        userRepository.save(company2);
    }
    
    private void createSampleCompanies() {
        System.out.println("🏢 Creating sample companies...");
        
        // Apple Inc.
        Company apple = new Company();
        apple.setName("Apple Inc.");
        apple.setTickerSymbol("AAPL");
        apple.setLastStockPrice(175.50);
        apple.setTotalShares(1000000L);
        apple.setAvailableShares(750000L);
        companyRepository.save(apple);
        
        // Tesla Inc.
        Company tesla = new Company();
        tesla.setName("Tesla Inc.");
        tesla.setTickerSymbol("TSLA");
        tesla.setLastStockPrice(245.80);
        tesla.setTotalShares(500000L);
        tesla.setAvailableShares(380000L);
        companyRepository.save(tesla);
        
        // Microsoft Corporation
        Company microsoft = new Company();
        microsoft.setName("Microsoft Corporation");
        microsoft.setTickerSymbol("MSFT");
        microsoft.setLastStockPrice(415.25);
        microsoft.setTotalShares(800000L);
        microsoft.setAvailableShares(620000L);
        companyRepository.save(microsoft);
        
        // Google (Alphabet)
        Company google = new Company();
        google.setName("Alphabet Inc.");
        google.setTickerSymbol("GOOGL");
        google.setLastStockPrice(138.75);
        google.setTotalShares(600000L);
        google.setAvailableShares(445000L);
        companyRepository.save(google);
        
        // Amazon
        Company amazon = new Company();
        amazon.setName("Amazon.com Inc.");
        amazon.setTickerSymbol("AMZN");
        amazon.setLastStockPrice(156.90);
        amazon.setTotalShares(700000L);
        amazon.setAvailableShares(520000L);
        companyRepository.save(amazon);
    }
    
    private void createSampleInvestments() {
        System.out.println("📈 Creating sample investments...");
        
        // Get users and companies
        User john = userRepository.findByUsername("john_investor").orElseThrow();
        User sarah = userRepository.findByUsername("sarah_trader").orElseThrow();
        User mike = userRepository.findByUsername("mike_portfolio").orElseThrow();
        
        // John's investments (diversified portfolio)
        createInvestment(john, "AAPL", 10.0, 1750.0);  // 10 shares at $175
        createInvestment(john, "TSLA", 5.0, 1229.0);   // 5 shares at $245.8
        createInvestment(john, "MSFT", 3.0, 1245.75);  // 3 shares at $415.25
        
        // Sarah's investments (focused on tech)
        createInvestment(sarah, "GOOGL", 15.0, 2081.25); // 15 shares at $138.75
        createInvestment(sarah, "AMZN", 8.0, 1255.2);    // 8 shares at $156.90
        
        // Mike's investments (heavy Tesla investor)
        createInvestment(mike, "TSLA", 25.0, 6145.0);    // 25 shares at $245.8
        createInvestment(mike, "AAPL", 20.0, 3510.0);    // 20 shares at $175.50
        createInvestment(mike, "MSFT", 5.0, 2076.25);    // 5 shares at $415.25
        createInvestment(mike, "GOOGL", 10.0, 1387.5);   // 10 shares at $138.75
        
        // Additional smaller investments for variety
        createInvestment(john, "AMZN", 3.0, 470.7);      // 3 shares at $156.90
        createInvestment(sarah, "AAPL", 8.0, 1404.0);    // 8 shares at $175.50
    }
    
    private void createInvestment(User user, String ticker, Double shares, Double amountUsd) {
        Investment investment = new Investment();
        investment.setUserId(user.getId());
        investment.setTickerSymbol(ticker);
        investment.setSharesPurchased(shares);
        investment.setAmountUsd(amountUsd);
        investment.setPurchasedAt(LocalDateTime.now().minusDays((long)(Math.random() * 30))); // Random date within last 30 days
        investmentRepository.save(investment);
        
        // Update user balance
        user.setBalance(user.getBalance() - amountUsd);
        userRepository.save(user);
        
        // Update company available shares
        Company company = companyRepository.findByTickerSymbol(ticker);
        if (company != null) {
            company.setAvailableShares(company.getAvailableShares() - shares.longValue());
            companyRepository.save(company);
        }
    }
    
    private void createSampleTransfers() {
        System.out.println("💸 Creating sample transfers...");
        
        User john = userRepository.findByUsername("john_investor").orElseThrow();
        User sarah = userRepository.findByUsername("sarah_trader").orElseThrow();
        User mike = userRepository.findByUsername("mike_portfolio").orElseThrow();
        
        // Some transfer history
        createTransfer(john, "Monthly Investment Fund", -2000.0);
        createTransfer(john, "Dividend Payment", +150.0);
        createTransfer(sarah, "Portfolio Rebalancing", -500.0);
        createTransfer(sarah, "Profit Taking", +300.0);
        createTransfer(mike, "Large Investment", -5000.0);
        createTransfer(mike, "Tesla Bonus Investment", -2000.0);
    }
    
    private void createTransfer(User user, String description, Double amount) {
        Transfers transfer = new Transfers();
        transfer.setAmount(Math.abs(amount));
        transfer.setRecipient(description);
        transfer.setUser(user);
        transfer.setCreatedAt(LocalDateTime.now().minusDays((long)(Math.random() * 60))); // Random date within last 60 days
        transferRepository.save(transfer);
    }
    
    private void initializePortfolios() {
        System.out.println("💼 Initializing portfolios...");
        
        // Get all users and create/update their portfolios
        userRepository.findAll().forEach(user -> {
            if (user.getRole() == UserRole.INVESTOR) {
                try {
                    portfolioService.recalculatePortfolio(user.getUsername());
                    System.out.println("📊 Portfolio created for: " + user.getUsername());
                } catch (Exception e) {
                    System.out.println("⚠️ Could not create portfolio for: " + user.getUsername());
                }
            }
        });
    }
    
    private void printSampleData() {
        System.out.println("\n🎯 SAMPLE DATA SUMMARY:");
        System.out.println("=======================");
        
        System.out.println("\n👥 USERS:");
        System.out.println("• admin / admin123 (SUPER_ADMIN)");
        System.out.println("• john_investor / password123 (INVESTOR) - Diversified portfolio");
        System.out.println("• sarah_trader / password123 (INVESTOR) - Tech focused");
        System.out.println("• mike_portfolio / password123 (INVESTOR) - Heavy Tesla investor");
        System.out.println("• apple_rep / company123 (COMPANY)");
        System.out.println("• tesla_rep / company123 (COMPANY)");
        
        System.out.println("\n🏢 COMPANIES:");
        System.out.println("• AAPL - Apple Inc. ($175.50)");
        System.out.println("• TSLA - Tesla Inc. ($245.80)");
        System.out.println("• MSFT - Microsoft Corporation ($415.25)");
        System.out.println("• GOOGL - Alphabet Inc. ($138.75)");
        System.out.println("• AMZN - Amazon.com Inc. ($156.90)");
        
        System.out.println("\n📊 TEST ENDPOINTS:");
        System.out.println("• POST /auth/login - Login with any user above");
        System.out.println("• GET /portfolio/my-portfolio - View your portfolio");
        System.out.println("• GET /investments/portfolio - View your investments");
        System.out.println("• GET /users (admin only) - View all users");
        System.out.println("• GET /portfolio/all (admin only) - View all portfolios");
        
        System.out.println("\n🚀 Ready for testing!");
    }
}
