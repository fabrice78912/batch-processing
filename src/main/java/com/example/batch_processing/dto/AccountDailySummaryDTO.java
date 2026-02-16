package com.example.batch_processing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Classe AccountDailySommaryDto
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */
@Data
@AllArgsConstructor
public class AccountDailySummaryDTO {
    private Long accountId;
    private String customerName;
    private BigDecimal currentBalance;
    private LocalDate balanceDate;
    private BigDecimal totalTransactions;
    private BigDecimal dailyTotal;
    private Boolean dailyProcessed;
}

