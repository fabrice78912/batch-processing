package com.example.batch_processing.web;

import com.example.batch_processing.dto.AccountDailySummaryDTO;
import com.example.batch_processing.service.AccountDailyBalanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Classe AccountDailyBalanceControllerIT
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */

/**
 * Test d’intégration pour AccountDailyBalanceController
 */
@WebMvcTest(AccountDailyBalanceController.class)
class AccountDailyBalanceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountDailyBalanceService service;

    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    // ------------------- Scénarios 1 à 15 -------------------
    @Nested
    @DisplayName("Scénarios de test endpoint /daily-summary")
    class DailySummaryScenarios {

        // 1️⃣ Retour standard avec un compte
        @Test
        void testStandardResponse() throws Exception {
            AccountDailySummaryDTO dto = new AccountDailySummaryDTO(
                    1L, "Alice", BigDecimal.valueOf(100), yesterday,
                    BigDecimal.valueOf(3), BigDecimal.valueOf(300), true
            );

            Mockito.when(service.getDailySummary(eq(yesterday), isNull(), eq(1), eq(20)))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("Résumé journalier récupéré avec succès"))
                    .andExpect(jsonPath("$.data.accounts[0].accountId").value(1))
                    .andExpect(jsonPath("$.data.accounts[0].totalTransactions").value(3));
        }

        // 2️⃣ Page < 1 et size < 1
        @Test
        void testPageAndSizeNegative() throws Exception {
            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("page", "-1")
                            .param("size", "-5"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message")
                            .value("getDailySummary.page : page doit être > 0; getDailySummary.size : size doit être > 0"))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.exception").value("ConstraintViolationException"));
        }


        // 4️⃣ AccountId inexistant
        @Test
        void testNonExistingAccountId() throws Exception {
            Mockito.when(service.getDailySummary(eq(yesterday), eq(999L), eq(1), eq(20)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("accountId", "999")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts").isEmpty());
        }

        // 5️⃣ Page + size dépassant le nombre total de comptes
        @Test
        void testPaginationBeyondTotal() throws Exception {
            Mockito.when(service.getDailySummary(eq(yesterday), isNull(), eq(10), eq(100)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("page", "10")
                            .param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts").isEmpty());
        }

        // 6️⃣ Multiple comptes pour même date
        @Test
        void testMultipleAccountsSameDate() throws Exception {
            AccountDailySummaryDTO dto1 = new AccountDailySummaryDTO(1L, "Alice", BigDecimal.valueOf(100), yesterday,
                    BigDecimal.valueOf(2), BigDecimal.valueOf(200), true);
            AccountDailySummaryDTO dto2 = new AccountDailySummaryDTO(2L, "Bob", BigDecimal.valueOf(150), yesterday,
                    BigDecimal.valueOf(1), BigDecimal.valueOf(150), true);

            Mockito.when(service.getDailySummary(eq(yesterday), isNull(), eq(1), eq(20)))
                    .thenReturn(List.of(dto1, dto2));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts", hasSize(2)))
                    .andExpect(jsonPath("$.data.accounts[0].accountId").value(1))
                    .andExpect(jsonPath("$.data.accounts[1].accountId").value(2));
        }

        // 7️⃣ Multiple dates pour un même compte
        @Test
        void testMultipleDatesSameAccount() throws Exception {
            LocalDate balanceDate = LocalDate.now().minusDays(1); // correspond à la logique du contrôleur
            AccountDailySummaryDTO dto1 = new AccountDailySummaryDTO(
                    1L, "Alice", BigDecimal.valueOf(100), balanceDate.minusDays(1),
                    BigDecimal.valueOf(1), BigDecimal.valueOf(100), true);
            AccountDailySummaryDTO dto2 = new AccountDailySummaryDTO(
                    1L, "Alice", BigDecimal.valueOf(100), balanceDate,
                    BigDecimal.valueOf(2), BigDecimal.valueOf(200), true);

            // ⚡️ Utiliser le bon balanceDate dans le mock
            Mockito.when(service.getDailySummary(eq(balanceDate), eq(1L), eq(1), eq(20)))
                    .thenReturn(List.of(dto1, dto2));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("accountId", "1")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts", hasSize(2)))
                    .andExpect(jsonPath("$.data.accounts[0].balanceDate").value(balanceDate.minusDays(1).toString()))
                    .andExpect(jsonPath("$.data.accounts[1].balanceDate").value(balanceDate.toString()));
        }

        // 8️⃣ Transactions = 0
        @Test
        void testZeroTransactions() throws Exception {
            AccountDailySummaryDTO dto = new AccountDailySummaryDTO(3L, "Charlie", BigDecimal.valueOf(50), yesterday,
                    BigDecimal.ZERO, BigDecimal.ZERO, false);

            Mockito.when(service.getDailySummary(eq(yesterday), eq(3L), eq(1), eq(20)))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("accountId", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts[0].totalTransactions").value(0))
                    .andExpect(jsonPath("$.data.accounts[0].dailyTotal").value(0))
                    .andExpect(jsonPath("$.data.accounts[0].dailyProcessed").value(false));
        }

        // 9️⃣ Account avec solde négatif
        @Test
        void testNegativeBalance() throws Exception {
            AccountDailySummaryDTO dto = new AccountDailySummaryDTO(4L, "David", BigDecimal.valueOf(-50), yesterday,
                    BigDecimal.valueOf(2), BigDecimal.valueOf(150), true);

            Mockito.when(service.getDailySummary(eq(yesterday), eq(4L), eq(1), eq(20)))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("accountId", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts[0].currentBalance").value(-50));
        }
    }

        // 10️⃣ Transactions null ou missing
        @Test
        void testTransactionsNullOrMissing() throws Exception {
            AccountDailySummaryDTO dto = new AccountDailySummaryDTO(
                    2L, "Eve", BigDecimal.valueOf(50), yesterday,
                    BigDecimal.ZERO, BigDecimal.ZERO, false
            );

            Mockito.when(service.getDailySummary(eq(yesterday), isNull(), eq(1), eq(20)))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts[0].totalTransactions").value(0))
                    .andExpect(jsonPath("$.data.accounts[0].dailyTotal").value(0))
                    .andExpect(jsonPath("$.data.accounts[0].dailyProcessed").value(false));
        }

        // 11️⃣ Pagination avec accountId spécifique
        @Test
        void testPaginationWithAccountId() throws Exception {
            Mockito.when(service.getDailySummary(eq(yesterday), eq(5L), eq(2), eq(10)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("page", "2")
                            .param("size", "10")
                            .param("accountId", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts").isEmpty());
        }

        // 12️⃣ BalanceDate null
        @Test
        void testBalanceDateNull() throws Exception {

            AccountDailySummaryDTO dto = new AccountDailySummaryDTO(
                    1L, "Alice", BigDecimal.valueOf(100),
                    yesterday, // balanceDate = yesterday
                    BigDecimal.valueOf(3),
                    BigDecimal.valueOf(300),
                    true
            );

            Mockito.when(service.getDailySummary(eq(yesterday), isNull(), eq(1), eq(20)))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts[0].balanceDate").value(yesterday.toString()))
                    .andExpect(jsonPath("$.data.accounts[0].dailyTotal").value(300));
        }

        // 13️⃣ Date future
        @Test
        void testFutureDate() throws Exception {
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            Mockito.when(service.getDailySummary(eq(tomorrow), isNull(), eq(1), eq(20)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("date", tomorrow.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts").isEmpty());
        }

        // 14️⃣ Paramètres mixtes (page, size, accountId)
        @Test
        void testMixedParams() throws Exception {
            AccountDailySummaryDTO dto = new AccountDailySummaryDTO(
                    3L, "Dan", BigDecimal.valueOf(150), yesterday,
                    BigDecimal.valueOf(2), BigDecimal.valueOf(200), true
            );

            Mockito.when(service.getDailySummary(eq(yesterday), eq(3L), eq(2), eq(1)))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("accountId", "3")
                            .param("page", "2")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts[0].accountId").value(3));
        }

        // 15️⃣ Vérification pagination et SQL indirect via service mocké
        @Test
        void testPaginationAndFiltering() throws Exception {
            AccountDailySummaryDTO dto = new AccountDailySummaryDTO(
                    7L, "Fiona", BigDecimal.valueOf(250), yesterday,
                    BigDecimal.valueOf(5), BigDecimal.valueOf(500), true
            );

            Mockito.when(service.getDailySummary(eq(yesterday), eq(7L), eq(1), eq(10)))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/accounts/daily-summary")
                            .param("accountId", "7")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accounts[0].accountId").value(7))
                    .andExpect(jsonPath("$.data.accounts[0].totalTransactions").value(5))
                    .andExpect(jsonPath("$.data.accounts[0].dailyTotal").value(500));
        }
}
