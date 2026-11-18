package com.leads.spring_batch.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Threading and scheduling infrastructure, kept apart from the job definition. */
@Configuration
@EnableScheduling
public class BatchExecutorConfig {

  public static final String MIGRATION_EXECUTOR = "migrationTaskExecutor";

  /** Runs the partitions of the migration job in parallel. */
  @Bean(MIGRATION_EXECUTOR)
  public ThreadPoolTaskExecutor migrationTaskExecutor(LoanMigrationProperties properties) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("loan-migration-");
    executor.setCorePoolSize(properties.threads());
    executor.setMaxPoolSize(properties.threads());
    executor.setQueueCapacity(properties.partitions());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    return executor;
  }
}
