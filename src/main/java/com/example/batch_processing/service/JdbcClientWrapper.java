package com.example.batch_processing.service;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Map;

/**
 * Classe JdbcClientWrapper
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-17
 */
public class JdbcClientWrapper {
    private final JdbcClient jdbcClient;

    public JdbcClientWrapper(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public int upsertDailyBalance(Map<String, Object> params) {
        return jdbcClient.sql("""
            INSERT INTO account_daily_balance (account_id, balance_date, daily_total, processed)
            SELECT t.account_id, CURRENT_DATE - INTERVAL '1 day', SUM(t.amount), TRUE
            FROM transactions t
            WHERE t.transaction_date >= CURRENT_DATE - INTERVAL '1 day'
                  AND t.transaction_date < CURRENT_DATE
                  AND t.status = 'VALIDATED'
                  AND t.account_id BETWEEN :minId AND :maxId
            GROUP BY t.account_id
            HAVING SUM(t.amount) <> 0
            ON CONFLICT (account_id, balance_date)
            DO UPDATE SET daily_total = EXCLUDED.daily_total,
                          processed = TRUE
            """)
                .paramSource(params)
                .update();
    }

    public int updateAccountBalance(Map<String, Object> params) {
        return jdbcClient.sql("""
            UPDATE accounts a
            SET balance = a.balance + adb.daily_total
            FROM account_daily_balance adb
            WHERE a.id = adb.account_id
              AND adb.balance_date = CURRENT_DATE - INTERVAL '1 day'
              AND adb.daily_total <> 0
              AND a.id BETWEEN :minId AND :maxId
            """)
                .paramSource(params)
                .update();
    }
}

