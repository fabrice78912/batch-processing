package com.example.batch_processing.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe EndOfDayBatchScheduler
 *
 * @author Fabrice
 * @version 1.0
 * @since 2026-02-16
 */

@Component
@RequiredArgsConstructor
public class EndOfDayBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job endOfDayBalanceJobPartitioned; // injecte ton Job Partitionné

    // ================= Scheduler =================
    @Scheduled(cron = "0 57 12 * * *", zone = "America/Toronto")
    public void runEndOfDayBatch() {
        try {
            // Ajouter un paramètre unique pour chaque exécution
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            jobLauncher.run(endOfDayBalanceJobPartitioned,
                    new JobParametersBuilder()
                            .addString("run.id", timestamp) // param unique pour éviter les doublons
                            .toJobParameters()
            );

            System.out.println("Batch EndOfDay exécuté avec succès : " + timestamp);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'exécution du batch EndOfDay");
        }
    }
}

