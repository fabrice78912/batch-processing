package com.example.batch_processing.repo;

import com.example.batch_processing.dto.AccountDailySummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe AccountDailyBalanceRepositoryTest
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */


class AccountDailyBalanceRepositoryTest {

 /*   @Mock
    private JdbcClient jdbc;

    @Mock
    private JdbcClient.MappedQuerySpec<AccountDailySummaryDTO> mappedQuerySpec;

    @InjectMocks
    private AccountDailyBalanceRepository repository;

    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- Scenario 1: accountId null, plusieurs comptes existants ----------------
    @Test
    void testGetDailySummary_multipleAccounts_accountIdNull() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);

        List<AccountDailySummaryDTO> mockList = List.of(
                new AccountDailySummaryDTO(1L, "Alice", BigDecimal.valueOf(100), yesterday, BigDecimal.valueOf(2), BigDecimal.valueOf(200), true),
                new AccountDailySummaryDTO(2L, "Bob", BigDecimal.valueOf(50), yesterday, BigDecimal.valueOf(1), BigDecimal.valueOf(50), true)
        );

        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.list()).thenReturn(mockList);

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, null, 1, 10);

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getCustomerName());
        assertEquals("Bob", result.get(1).getCustomerName());
    }

    // ---------------- Scenario 2: accountId fourni et valide ----------------
    @Test
    void testGetDailySummary_accountIdProvided() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);

        List<AccountDailySummaryDTO> mockList = List.of(
                new AccountDailySummaryDTO(5L, "Charlie", BigDecimal.valueOf(200), yesterday, BigDecimal.valueOf(3), BigDecimal.valueOf(300), true)
        );

        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.list()).thenReturn(mockList);

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, 5L, 1, 10);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getAccountId());
        assertEquals("Charlie", result.get(0).getCustomerName());
    }

    // ---------------- Scenario 3: accountId inexistant ----------------
    @Test
    void testGetDailySummary_accountIdNonExistant() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.list()).thenReturn(List.of());

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, 99999L, 1, 10);

        assertTrue(result.isEmpty());
    }

    // ---------------- Scenario 4: pas de transactions pour la date ----------------
    @Test
    void testGetDailySummary_noTransactions() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        List<AccountDailySummaryDTO> mockList = List.of(
                new AccountDailySummaryDTO(1L, "Alice", BigDecimal.valueOf(100), yesterday, BigDecimal.ZERO, BigDecimal.ZERO, false)
        );

        when(mappedQuerySpec.list()).thenReturn(mockList);

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, null, 1, 10);

        assertEquals(1, result.size());
        assertEquals(BigDecimal.ZERO, result.get(0).getTotalTransactions());
        assertFalse(result.get(0).isDailyProcessed());
    }

    // ---------------- Scenario 5: pagination ----------------
    @Test
    void testGetDailySummary_pagination() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        List<AccountDailySummaryDTO> mockList = List.of(
                new AccountDailySummaryDTO(3L, "Dan", BigDecimal.valueOf(150), yesterday, BigDecimal.valueOf(2), BigDecimal.valueOf(200), true)
        );

        when(mappedQuerySpec.list()).thenReturn(mockList);

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, null, 2, 1);

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getAccountId());
    }

    // ---------------- Scenario 6: page > nombre de pages existantes ----------------
    @Test
    void testGetDailySummary_pageOutOfBounds() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.list()).thenReturn(List.of());

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, null, 1000, 10);

        assertTrue(result.isEmpty());
    }

    // ---------------- Scenario 7 et 8: page ou size invalid ----------------
    // Ces cas devraient être gérés via @Min dans le controller, pas ici

    // ---------------- Scenario 9: mapping DTO ----------------
    @Test
    void testGetDailySummary_dtoMapping() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        AccountDailySummaryDTO dto = new AccountDailySummaryDTO(1L, "Alice", BigDecimal.valueOf(100),
                yesterday, BigDecimal.valueOf(3), BigDecimal.valueOf(300), true);
        when(mappedQuerySpec.list()).thenReturn(List.of(dto));

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, null, 1, 10);

        assertEquals(1, result.size());
        AccountDailySummaryDTO r = result.get(0);
        assertEquals(1L, r.getAccountId());
        assertEquals("Alice", r.getCustomerName());
        assertEquals(BigDecimal.valueOf(100), r.getCurrentBalance());
        assertEquals(yesterday, r.getBalanceDate());
        assertEquals(BigDecimal.valueOf(3), r.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(300), r.getDailyTotal());
        assertTrue(r.isDailyProcessed());
    }

    // ---------------- Scenario 10: Transactions null ou missing ----------------
    @Test
    void testGetDailySummary_transactionsNullOrMissing() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Simuler DTO avec transactions null / missing
        AccountDailySummaryDTO dto = new AccountDailySummaryDTO(2L, "Eve", BigDecimal.valueOf(50),
                yesterday, BigDecimal.ZERO, BigDecimal.ZERO, false);

        when(mappedQuerySpec.list()).thenReturn(List.of(dto));

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, null, 1, 10);

        assertEquals(1, result.size());
        AccountDailySummaryDTO r = result.get(0);
        assertEquals(BigDecimal.ZERO, r.getTotalTransactions());
        assertEquals(BigDecimal.ZERO, r.getDailyTotal());
        assertFalse(r.isDailyProcessed());
    }

    // ---------------- Scenario 11: Pagination avec accountId spécifique ----------------
    @Test
    void testGetDailySummary_paginationWithAccountId() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Simuler une seule page avec accountId=5
        when(mappedQuerySpec.list()).thenReturn(List.of());

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, 5L, 2, 10);

        // Page 2, mais compte 5 n'a qu'une page → liste vide
        assertTrue(result.isEmpty());
    }

    // ---------------- Scenario 12: BalanceDate null ----------------
    @Test
    void testGetDailySummary_balanceDateNull() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Selon implémentation, balanceDate null → utiliser yesterday
        AccountDailySummaryDTO dto = new AccountDailySummaryDTO(1L, "Alice", BigDecimal.valueOf(100),
                yesterday, BigDecimal.valueOf(3), BigDecimal.valueOf(300), true);

        when(mappedQuerySpec.list()).thenReturn(List.of(dto));

        List<AccountDailySummaryDTO> result = repository.getDailySummary(null, null, 1, 10);

        assertEquals(1, result.size());
        assertEquals(yesterday, result.get(0).getBalanceDate());
    }

    // ---------------- Scenario 13: Date future ----------------
    @Test
    void testGetDailySummary_futureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Simuler aucun transaction future
        when(mappedQuerySpec.list()).thenReturn(List.of());

        List<AccountDailySummaryDTO> result = repository.getDailySummary(tomorrow, null, 1, 10);

        assertTrue(result.isEmpty());
    }

    // ---------------- Scenario 14: Paramètres mixtes (page, size, accountId) ----------------
    @Test
    void testGetDailySummary_mixedParams() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        AccountDailySummaryDTO dto = new AccountDailySummaryDTO(3L, "Dan", BigDecimal.valueOf(150),
                yesterday, BigDecimal.valueOf(2), BigDecimal.valueOf(200), true);

        when(mappedQuerySpec.list()).thenReturn(List.of(dto));

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, 3L, 2, 1);

        assertEquals(1, result.size());
        AccountDailySummaryDTO r = result.get(0);
        assertEquals(3L, r.getAccountId());
    }

    // ---------------- Scenario 15: Vérification requête SQL générée ----------------
    @Test
    void testGetDailySummary_sqlWhereClauseReplacement() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        when(mappedQuerySpec.list()).thenReturn(List.of());

        // Appel avec accountId null
        repository.getDailySummary(yesterday, null, 1, 10);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).sql(sqlCaptor.capture());
        String sqlUsed = sqlCaptor.getValue();
        assertFalse(sqlUsed.contains(":accountId")); // WHERE non présent

        // Appel avec accountId = 5
        repository.getDailySummary(yesterday, 5L, 1, 10);
        verify(jdbc, times(2)).sql(sqlCaptor.capture());
        String sqlUsed2 = sqlCaptor.getValue();
        assertTrue(sqlUsed2.contains(":accountId")); // WHERE présent
    }

    // ---------------- Scenario 10: Transactions null ou missing ----------------
    @Test
    void testGetDailySummary_transactionsNullOrMissing1() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Simuler DTO avec transactions null / missing
        AccountDailySummaryDTO dto = new AccountDailySummaryDTO(2L, "Eve", BigDecimal.valueOf(50),
                yesterday, BigDecimal.ZERO, BigDecimal.ZERO, false);

        when(mappedQuerySpec.list()).thenReturn(List.of(dto));

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, null, 1, 10);

        assertEquals(1, result.size());
        AccountDailySummaryDTO r = result.get(0);
        assertEquals(BigDecimal.ZERO, r.getTotalTransactions());
        assertEquals(BigDecimal.ZERO, r.getDailyTotal());
        assertFalse(r.isDailyProcessed());
    }

    // ---------------- Scenario 11: Pagination avec accountId spécifique ----------------
    @Test
    void testGetDailySummary_paginationWithAccountId() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Simuler une seule page avec accountId=5
        when(mappedQuerySpec.list()).thenReturn(List.of());

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, 5L, 2, 10);

        // Page 2, mais compte 5 n'a qu'une page → liste vide
        assertTrue(result.isEmpty());
    }

    // ---------------- Scenario 12: BalanceDate null ----------------
    @Test
    void testGetDailySummary_balanceDateNull() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Selon implémentation, balanceDate null → utiliser yesterday
        AccountDailySummaryDTO dto = new AccountDailySummaryDTO(1L, "Alice", BigDecimal.valueOf(100),
                yesterday, BigDecimal.valueOf(3), BigDecimal.valueOf(300), true);

        when(mappedQuerySpec.list()).thenReturn(List.of(dto));

        List<AccountDailySummaryDTO> result = repository.getDailySummary(null, null, 1, 10);

        assertEquals(1, result.size());
        assertEquals(yesterday, result.get(0).getBalanceDate());
    }

    // ---------------- Scenario 13: Date future ----------------
    @Test
    void testGetDailySummary_futureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        // Simuler aucun transaction future
        when(mappedQuerySpec.list()).thenReturn(List.of());

        List<AccountDailySummaryDTO> result = repository.getDailySummary(tomorrow, null, 1, 10);

        assertTrue(result.isEmpty());
    }

    // ---------------- Scenario 14: Paramètres mixtes (page, size, accountId) ----------------
    @Test
    void testGetDailySummary_mixedParams() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        AccountDailySummaryDTO dto = new AccountDailySummaryDTO(3L, "Dan", BigDecimal.valueOf(150),
                yesterday, BigDecimal.valueOf(2), BigDecimal.valueOf(200), true);

        when(mappedQuerySpec.list()).thenReturn(List.of(dto));

        List<AccountDailySummaryDTO> result = repository.getDailySummary(yesterday, 3L, 2, 1);

        assertEquals(1, result.size());
        AccountDailySummaryDTO r = result.get(0);
        assertEquals(3L, r.getAccountId());
    }

    // ---------------- Scenario 15: Vérification requête SQL générée ----------------
    @Test
    void testGetDailySummary_sqlWhereClauseReplacement() {
        when(jdbc.sql(anyString())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.params(any(Map.class))).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.query(AccountDailySummaryDTO.class)).thenReturn(mappedQuerySpec);

        when(mappedQuerySpec.list()).thenReturn(List.of());

        // Appel avec accountId null
        repository.getDailySummary(yesterday, null, 1, 10);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).sql(sqlCaptor.capture());
        String sqlUsed = sqlCaptor.getValue();
        assertFalse(sqlUsed.contains(":accountId")); // WHERE non présent

        // Appel avec accountId = 5
        repository.getDailySummary(yesterday, 5L, 1, 10);
        verify(jdbc, times(2)).sql(sqlCaptor.capture());
        String sqlUsed2 = sqlCaptor.getValue();
        assertTrue(sqlUsed2.contains(":accountId")); // WHERE présent
    }*/
}
