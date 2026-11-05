package com.banking.dashboard.controller;

import com.banking.dashboard.entity.BranchPerformance;
import com.banking.dashboard.entity.CreditCardUtilization;
import com.banking.dashboard.entity.CustomerPortfolio;
import com.banking.dashboard.entity.LoanRiskSummary;
import com.banking.dashboard.entity.MonthlyTransactionVolume;
import com.banking.dashboard.entity.TopCustomerAum;
import com.banking.dashboard.service.DashboardService;
import com.banking.dashboard.service.ViewRefreshService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private ViewRefreshService viewRefreshService;

    @Test
    void getCustomerPortfolio_returnsAllData() throws Exception {
        CustomerPortfolio cp = new CustomerPortfolio();
        cp.setCustomerId(1);
        cp.setCustomerName("John Doe");
        cp.setEmail("john@example.com");
        cp.setTotalDepositBalance(new BigDecimal("50000"));

        when(dashboardService.getCustomerPortfolios(null)).thenReturn(List.of(cp));

        mockMvc.perform(get("/api/dashboard/customer-portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].customerName", is("John Doe")))
                .andExpect(jsonPath("$.count", is(1)));
    }

    @Test
    void getCustomerPortfolio_withTierFilter() throws Exception {
        when(dashboardService.getCustomerPortfolios("GOLD")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/dashboard/customer-portfolio").param("tier", "GOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.count", is(0)));

        verify(dashboardService).getCustomerPortfolios("GOLD");
    }

    @Test
    void getBranchPerformance_returnsAllData() throws Exception {
        BranchPerformance bp = new BranchPerformance();
        bp.setBranchId(1);
        bp.setBranchName("Main Branch");
        bp.setTotalDeposits(new BigDecimal("1000000"));

        when(dashboardService.getBranchPerformance(null)).thenReturn(List.of(bp));

        mockMvc.perform(get("/api/dashboard/branch-performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].branchName", is("Main Branch")));
    }

    @Test
    void getBranchPerformance_withMinDepositsFilter() throws Exception {
        when(dashboardService.getBranchPerformance(new BigDecimal("100000")))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/dashboard/branch-performance").param("min_deposits", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(dashboardService).getBranchPerformance(new BigDecimal("100000"));
    }

    @Test
    void getMonthlyTransactions_returnsData() throws Exception {
        when(dashboardService.getMonthlyTransactionVolume()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/dashboard/monthly-transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void getLoanRisk_returnsData() throws Exception {
        LoanRiskSummary lr = new LoanRiskSummary();
        lr.setLoanType("MORTGAGE");
        lr.setStatus("ACTIVE");
        lr.setLoanCount(50L);

        when(dashboardService.getLoanRiskSummary()).thenReturn(List.of(lr));

        mockMvc.perform(get("/api/dashboard/loan-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].loanType", is("MORTGAGE")));
    }

    @Test
    void getCreditCardUtilization_returnsData() throws Exception {
        CreditCardUtilization ccu = new CreditCardUtilization();
        ccu.setCardType("VISA");
        ccu.setCardCount(100L);

        when(dashboardService.getCreditCardUtilization()).thenReturn(List.of(ccu));

        mockMvc.perform(get("/api/dashboard/credit-card-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].cardType", is("VISA")));
    }

    @Test
    void getTopCustomers_returnsAllData() throws Exception {
        TopCustomerAum tc = new TopCustomerAum();
        tc.setCustomerId(1);
        tc.setCustomerName("Jane Doe");
        tc.setTier("PLATINUM");

        when(dashboardService.getTopCustomers(null)).thenReturn(List.of(tc));

        mockMvc.perform(get("/api/dashboard/top-customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].tier", is("PLATINUM")));
    }

    @Test
    void getTopCustomers_withTierFilter() throws Exception {
        when(dashboardService.getTopCustomers("GOLD")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/dashboard/top-customers").param("tier", "GOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(dashboardService).getTopCustomers("GOLD");
    }

    @Test
    void refreshViews_success() throws Exception {
        doNothing().when(viewRefreshService).refreshAllViews();

        mockMvc.perform(post("/api/dashboard/refresh-views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("All materialized views refreshed successfully")));
    }

    @Test
    void refreshViews_failure() throws Exception {
        doThrow(new RuntimeException("Database error")).when(viewRefreshService).refreshAllViews();

        mockMvc.perform(post("/api/dashboard/refresh-views"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Failed to refresh views")));
    }

    @Test
    void unexpectedException_returnsGenericMessageWithoutDetails() throws Exception {
        when(dashboardService.getMonthlyTransactionVolume())
                .thenThrow(new RuntimeException("relation banking.mv_secret does not exist"));

        mockMvc.perform(get("/api/dashboard/monthly-transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")));
    }
}
