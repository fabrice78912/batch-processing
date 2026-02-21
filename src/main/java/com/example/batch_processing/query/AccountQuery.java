package com.example.batch_processing.query;

/**
 * Classe AccountDailyBalanceQuery
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */
public class AccountQuery {

    private AccountQuery() {
        // classe utilitaire
    }

    public static final String SELECT_DAILY_SUMMARY =
            """
            SELECT
                a.id AS account_id,
                a.customer_name,
                a.balance AS current_balance,
                COALESCE(adb.balance_date, :balanceDate) AS balance_date,
                COALESCE(SUM(t.amount), 0) AS total_transactions,
                COALESCE(adb.daily_total, 0) AS daily_total,
                COALESCE(adb.processed, FALSE) AS daily_processed
            FROM public.accounts a
            LEFT JOIN public.account_daily_balance adb
                ON adb.account_id = a.id
               AND adb.balance_date = :balanceDate
            LEFT JOIN public.transactions t
                ON t.account_id = a.id
               AND t.transaction_date = :balanceDate
               AND t.status = 'VALIDATED'
            /**WHERE_CLAUSE**/
            GROUP BY a.id, a.customer_name, a.balance, adb.balance_date, adb.daily_total, adb.processed
            ORDER BY a.id
            LIMIT :size OFFSET :offset
            """;
}

