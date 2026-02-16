package com.example.batch_processing.repo;

import com.example.batch_processing.dto.AccountDailySummaryDTO;
import com.example.batch_processing.query.AccountQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Classe AccountDailyBalanceRepository
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */
@Repository
@RequiredArgsConstructor
public class AccountDailyBalanceRepository {

    private final JdbcClient jdbc;

    /**
     * Retourne la liste paginée des comptes avec transactions journalières
     *
     * @param balanceDate Date du jour à vérifier
     * @param accountId   Optionnel : filtrer sur un compte spécifique
     * @param page        Numéro de page (1-based)
     * @param size        Taille de la page
     * @return Liste de AccountDailySummaryDTO
     */
    public List<AccountDailySummaryDTO> getDailySummary(
            LocalDate balanceDate,
            Long accountId,
            int page,
            int size
    ) {
        int offset = (page - 1) * size;

        // Préparer la clause WHERE dynamique
        String whereClause = (accountId != null) ? "WHERE a.id = :accountId" : "";

        // Construire la requête finale
        String sql = AccountQuery.SELECT_DAILY_SUMMARY.replace("/**WHERE_CLAUSE**/", whereClause);

        // Construire le Map des paramètres
        Map<String, Object> params = (accountId != null)
                ? Map.of(
                "balanceDate", balanceDate,
                "size", size,
                "offset", offset,
                "accountId", accountId
        )
                : Map.of(
                "balanceDate", balanceDate,
                "size", size,
                "offset", offset
        );

        // Exécution et mapping automatique vers DTO
        return jdbc.sql(sql)
                .params(params)
                .query(AccountDailySummaryDTO.class)
                .list();
    }
}
