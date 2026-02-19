package com.example.batch_processing.batch;

import com.example.batch_processing.service.JdbcClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class PartitionedBalanceJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JdbcClient jdbcClient;

    // ================= JOB =================
    @Bean
    public Job endOfDayBalanceJobPartitioned() {
        return new JobBuilder("endOfDayBalanceJobPartitioned", jobRepository)
                .start(partitionStep())
                .build();
    }

    // ================= MASTER STEP =================
    @Bean
    public Step partitionStep() {
        return new StepBuilder("partitionStep", jobRepository)
                .partitioner("updateBalanceWorkerStep", rangePartitioner())
                .step(updateBalanceWorkerStep())
                .gridSize(8)
                .taskExecutor(taskExecutor())
                .build();
    }

    // ================= WORKER STEP =================
    @Bean
    public Step updateBalanceWorkerStep() {
        return new StepBuilder("updateBalanceWorkerStep", jobRepository)
                .tasklet(updateBalanceTasklet(null, null), transactionManager)
                .build();
    }

    // ================= PARTITIONER =================
    @Bean
    public Partitioner rangePartitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> result = new HashMap<>();
            long minId = 1;
            long maxId = 10_000_000;
            int gridSizeInt = 8;
            long targetSize = (maxId - minId) / gridSizeInt;

            long start = minId;
            long end;

            for (int i = 0; i < gridSizeInt; i++) {
                end = (i == gridSizeInt - 1) ? maxId : start + targetSize - 1;
                ExecutionContext context = new ExecutionContext();
                context.putLong("minId", start);
                context.putLong("maxId", end);
                result.put("partition" + i, context);
                start = end + 1;
            }

            return result;
        };
    }

    // ================= TASKLET =================
    @Bean
    @StepScope
    public Tasklet updateBalanceTasklet(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId) {

        return (contribution, chunkContext) -> {

            Map<String, Object> params = new HashMap<>();
            params.put("minId", minId);
            params.put("maxId", maxId);

            // ===============================
            // 1️⃣ Insert ou update account_daily_balance uniquement si total des transactions > 0
            // ===============================
            String sqlUpsertDailyBalance = """
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
                    """;

            int dailyUpdated = jdbcClient.sql(sqlUpsertDailyBalance)
                    .paramSource(params)
                    .update();

            System.out.println("Partition [" + minId + "-" + maxId + "] : account_daily_balance mis à jour = " + dailyUpdated);

            // ===============================
            // 2️⃣ Mettre à jour la balance du compte uniquement si daily_total > 0
            // ===============================
            String sqlUpdateAccountBalance = """
                UPDATE accounts a
                SET balance = a.balance + adb.daily_total
                FROM account_daily_balance adb
                WHERE a.id = adb.account_id
                  AND adb.balance_date = CURRENT_DATE - INTERVAL '1 day'
                  AND adb.daily_total <> 0
                  AND a.id BETWEEN :minId AND :maxId
                """;

            int accountsUpdated = jdbcClient.sql(sqlUpdateAccountBalance)
                    .paramSource(params)
                    .update();

            System.out.println("Partition [" + minId + "-" + maxId + "] : comptes mis à jour = " + accountsUpdated);

            return RepeatStatus.FINISHED;
        };
    }

    // ================= THREAD POOL =================
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }

    // ================= TASKLET pour tests =================
    //@Bean
    public Tasklet updateBalanceTaskletForTest(Long minId, Long maxId, JdbcClientWrapper jdbcWrapper) {
        return (contribution, chunkContext) -> {
            Map<String, Object> params = Map.of(
                    "minId", minId != null ? minId : 1L,
                    "maxId", maxId != null ? maxId : 10_000_000L
            );

            jdbcWrapper.upsertDailyBalance(params);
            jdbcWrapper.updateAccountBalance(params);

            return RepeatStatus.FINISHED;
        };
    }
}
