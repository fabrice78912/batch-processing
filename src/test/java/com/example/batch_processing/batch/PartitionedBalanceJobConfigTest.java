package com.example.batch_processing.batch;

import com.example.batch_processing.service.JdbcClientWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PartitionedBalanceJobConfigTest {

    private PartitionedBalanceJobConfig config;
    private JdbcClient jdbcClient;
    private JdbcClientWrapper mockWrapper;

    @BeforeEach
    void setup() {
        jdbcClient = mock(JdbcClient.class);
        config = new PartitionedBalanceJobConfig(null, null, jdbcClient);
        mockWrapper = mock(JdbcClientWrapper.class);
    }

    /** ============================
     * Partitioner tests
     * ============================ */
    @Test
    void testRangePartitionerCreates8Partitions() {
        var partitions = config.rangePartitioner().partition(8);
        assertThat(partitions).hasSize(8);
    }

    @Test
    void testRangePartitionerLastPartitionMaxId() {
        var partitions = config.rangePartitioner().partition(8);
        long maxIdLast = partitions.get("partition7").getLong("maxId");
        assertThat(maxIdLast).isEqualTo(10_000_000L);
    }

    @Test
    void partitionsCoverFullRange() {
        var partitions = config.rangePartitioner().partition(8);
        long firstMin = partitions.get("partition0").getLong("minId");
        long lastMax = partitions.get("partition7").getLong("maxId");

        assertThat(firstMin).isEqualTo(1L);
        assertThat(lastMax).isEqualTo(10_000_000L);
    }

    /** ============================
     * Tasklet tests
     * ============================ */

    @Test
    void testTaskletWithValidIds() throws Exception {

        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(1);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(1);


        var tasklet = config.updateBalanceTaskletForTest(1L, 100L, mockWrapper);

        RepeatStatus status = tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void testTaskletWithNullIds() throws Exception {
        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(0);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(0);


        var tasklet = config.updateBalanceTaskletForTest(null, null, mockWrapper);
        RepeatStatus status = tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void testTaskletWithZeroUpdates() throws Exception {
        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(0);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(0);

        var tasklet = config.updateBalanceTaskletForTest(1L, 100L, mockWrapper);

        RepeatStatus status = tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        verify(mockWrapper).upsertDailyBalance(anyMap());
        verify(mockWrapper).updateAccountBalance(anyMap());
    }

    @Test
    void testTaskletWithMultipleAccounts() throws Exception {

        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(3);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(3);


        var tasklet = config.updateBalanceTaskletForTest(1L, 100L, mockWrapper);
        tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));
    }

    @Test
    void testTaskletIgnoresZeroDailyTotal() throws Exception {
        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(1);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(0);

        var tasklet = config.updateBalanceTaskletForTest(1L, 100L, mockWrapper);

        tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));
    }

    @Test
    void testTaskletWithInvalidTransactionStatus() throws Exception {

        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(0);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(0);

        var tasklet = config.updateBalanceTaskletForTest(1L, 100L, mockWrapper);
        tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));
    }

    @Test
    void testTaskletParamSourceCalled() throws Exception {

        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(1);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(1);

        var tasklet = config.updateBalanceTaskletForTest(5L, 50L, mockWrapper);
        tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));

        // Vérifie que les méthodes du wrapper ont été appelées
        verify(mockWrapper).upsertDailyBalance(argThat(map ->
                map.get("minId").equals(5L) && map.get("maxId").equals(50L)
        ));

        verify(mockWrapper).updateAccountBalance(argThat(map ->
                map.get("minId").equals(5L) && map.get("maxId").equals(50L)
        ));
    }

    @Test
    void testTaskletWithMinGreaterThanMax() throws Exception {

        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(0);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(0);

        var tasklet = config.updateBalanceTaskletForTest(100L, 1L, mockWrapper);
        RepeatStatus status = tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void testTaskletWithLargeRange() throws Exception {

        //JdbcClientWrapper mockWrapper = mock(JdbcClientWrapper.class);
        when(mockWrapper.upsertDailyBalance(anyMap())).thenReturn(5);
        when(mockWrapper.updateAccountBalance(anyMap())).thenReturn(5);

        var tasklet = config.updateBalanceTaskletForTest(1L, 10_000_000L, mockWrapper);
        tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));
    }

    @Test
    void testTaskletThrowsExceptionIfJdbcFails() {
        JdbcClient.StatementSpec specUpsert = mock(JdbcClient.StatementSpec.class);
        when(specUpsert.paramSource(any())).thenReturn(specUpsert);
        when(specUpsert.update()).thenThrow(new RuntimeException("DB Error"));
        when(jdbcClient.sql(contains("INSERT INTO account_daily_balance"))).thenReturn(specUpsert);

        var tasklet = config.updateBalanceTasklet(1L, 100L);

        try {
            tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("Cannot invoke \"org.springframework.jdbc.core.simple.JdbcClient$StatementSpec.update()\"");
        }
    }

    /** ============================
     * Thread pool test
     * ============================ */
    @Test
    void testTaskExecutorThreadPool() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.taskExecutor();
        assertThat(executor.getCorePoolSize()).isEqualTo(8);
        assertThat(executor.getMaxPoolSize()).isEqualTo(8);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("batch-thread-");
    }
}