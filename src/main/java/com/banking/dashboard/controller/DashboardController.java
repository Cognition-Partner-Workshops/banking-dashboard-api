package com.banking.dashboard.controller;

import com.banking.dashboard.dto.ApiResponse;
import com.banking.dashboard.entity.BranchPerformance;
import com.banking.dashboard.entity.CreditCardUtilization;
import com.banking.dashboard.entity.CustomerPortfolio;
import com.banking.dashboard.entity.LoanRiskSummary;
import com.banking.dashboard.entity.MonthlyTransactionVolume;
import com.banking.dashboard.entity.TopCustomerAum;
import com.banking.dashboard.service.DashboardService;
import com.banking.dashboard.service.ViewRefreshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Banking analytics dashboard endpoints")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final ViewRefreshService viewRefreshService;

    public DashboardController(DashboardService dashboardService,
                               ViewRefreshService viewRefreshService) {
        this.dashboardService = dashboardService;
        this.viewRefreshService = viewRefreshService;
    }

    @GetMapping("/customer-portfolio")
    @Operation(summary = "Get customer portfolio data",
               description = "Returns all rows from mv_customer_portfolio. Optionally filter by tier (PLATINUM, GOLD, SILVER, BRONZE).")
    public ResponseEntity<ApiResponse<List<CustomerPortfolio>>> getCustomerPortfolio(
            @Parameter(description = "Customer tier filter (PLATINUM, GOLD, SILVER, BRONZE)")
            @RequestParam(required = false) String tier) {
        List<CustomerPortfolio> data = dashboardService.getCustomerPortfolios(tier);
        return ResponseEntity.ok(ApiResponse.success(data, data.size()));
    }

    @GetMapping("/branch-performance")
    @Operation(summary = "Get branch performance data",
               description = "Returns all rows from mv_branch_performance. Optionally filter by minimum deposits.")
    public ResponseEntity<ApiResponse<List<BranchPerformance>>> getBranchPerformance(
            @Parameter(description = "Minimum total deposits filter")
            @RequestParam(name = "min_deposits", required = false) BigDecimal minDeposits) {
        List<BranchPerformance> data = dashboardService.getBranchPerformance(minDeposits);
        return ResponseEntity.ok(ApiResponse.success(data, data.size()));
    }

    @GetMapping("/monthly-transactions")
    @Operation(summary = "Get monthly transaction volume",
               description = "Returns all rows from mv_monthly_transaction_volume.")
    public ResponseEntity<ApiResponse<List<MonthlyTransactionVolume>>> getMonthlyTransactions() {
        List<MonthlyTransactionVolume> data = dashboardService.getMonthlyTransactionVolume();
        return ResponseEntity.ok(ApiResponse.success(data, data.size()));
    }

    @GetMapping("/loan-risk")
    @Operation(summary = "Get loan risk summary",
               description = "Returns all rows from mv_loan_risk_summary.")
    public ResponseEntity<ApiResponse<List<LoanRiskSummary>>> getLoanRisk() {
        List<LoanRiskSummary> data = dashboardService.getLoanRiskSummary();
        return ResponseEntity.ok(ApiResponse.success(data, data.size()));
    }

    @GetMapping("/credit-card-utilization")
    @Operation(summary = "Get credit card utilization",
               description = "Returns all rows from mv_credit_card_utilization.")
    public ResponseEntity<ApiResponse<List<CreditCardUtilization>>> getCreditCardUtilization() {
        List<CreditCardUtilization> data = dashboardService.getCreditCardUtilization();
        return ResponseEntity.ok(ApiResponse.success(data, data.size()));
    }

    @GetMapping("/top-customers")
    @Operation(summary = "Get top customers by AUM",
               description = "Returns all rows from mv_top_customers_aum. Optionally filter by tier.")
    public ResponseEntity<ApiResponse<List<TopCustomerAum>>> getTopCustomers(
            @Parameter(description = "Customer tier filter")
            @RequestParam(required = false) String tier) {
        List<TopCustomerAum> data = dashboardService.getTopCustomers(tier);
        return ResponseEntity.ok(ApiResponse.success(data, data.size()));
    }

    @PostMapping("/refresh-views")
    @Operation(summary = "Refresh all materialized views",
               description = "Calls banking.refresh_all_views() to refresh all materialized views.")
    public ResponseEntity<ApiResponse<Void>> refreshViews() {
        try {
            viewRefreshService.refreshAllViews();
            return ResponseEntity.ok(ApiResponse.success("All materialized views refreshed successfully"));
        } catch (Exception e) {
            log.error("Failed to refresh materialized views", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to refresh views"));
        }
    }
}
