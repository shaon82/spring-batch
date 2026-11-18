package com.leads.spring_batch.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Non-functional tuning knobs of the loan migration job, bound from {@code batch.loan-migration.*}.
 *
 * @param chunkSize  rows read/written per transaction
 * @param partitions number of id-range partitions the work is split into
 * @param threads    worker threads processing partitions in parallel
 * @param retryLimit attempts for a chunk that fails with a transient database error
 * @param schedule   when the job is triggered automatically
 */
@ConfigurationProperties(prefix = "batch.loan-migration")
public record LoanMigrationProperties(
    @DefaultValue("1000") int chunkSize,
    @DefaultValue("8") int partitions,
    @DefaultValue("8") int threads,
    @DefaultValue("3") int retryLimit,
    @DefaultValue Schedule schedule) {

  public LoanMigrationProperties {
    requirePositive("chunk-size", chunkSize);
    requirePositive("partitions", partitions);
    requirePositive("threads", threads);
    requirePositive("retry-limit", retryLimit);
  }

  /**
   * @param enabled whether the scheduler is registered
   * @param cron    Spring cron expression (second minute hour day month weekday)
   * @param zone    time zone the cron expression is evaluated in
   */
  public record Schedule(
      @DefaultValue("true") boolean enabled,
      @DefaultValue("0 0 3 * * *") String cron,
      @DefaultValue("Asia/Dhaka") String zone) {}

  private static void requirePositive(String name, int value) {
    if (value < 1) {
      throw new IllegalArgumentException("batch.loan-migration." + name + " must be >= 1, was " + value);
    }
  }
}
