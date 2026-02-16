package com.example.batch_processing.service;

import com.example.batch_processing.dto.AccountDailySummaryDTO;
import com.example.batch_processing.repo.AccountDailyBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


/**
 * Classe AccountDailyBalanceService
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */
@Service
@RequiredArgsConstructor
public class AccountDailyBalanceService {

    private final AccountDailyBalanceRepository repository;

    public List<AccountDailySummaryDTO> getDailySummary(LocalDate date, Long accountId, int page, int size) {
        return repository.getDailySummary(date, accountId, page, size);
    }
}

