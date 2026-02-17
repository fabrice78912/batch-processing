package com.example.batch_processing.web;

import com.example.batch_processing.domain.Response;
import com.example.batch_processing.dto.AccountDailySummaryDTO;
import com.example.batch_processing.service.AccountDailyBalanceService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Classe AccountDailyBalanceController
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
public class AccountDailyBalanceController {

    private final AccountDailyBalanceService service;

    @GetMapping("/daily-summary")
    public ResponseEntity<Response> getDailySummary(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "page doit être > 0") int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size doit être > 0") int size,

            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String date
    ) {

        LocalDate balanceDate = (StringUtils.isNotBlank(date))
                ? LocalDate.parse(date)
                : LocalDate.now().minusDays(1);

        List<AccountDailySummaryDTO> result =
                service.getDailySummary(balanceDate, accountId, page, size);

        Response response = Response.builder()
                .time(LocalDateTime.now().toString())
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Résumé journalier récupéré avec succès")
                .data(Map.of("accounts", result))
                .build();

        return ResponseEntity.ok(response);
    }
}
