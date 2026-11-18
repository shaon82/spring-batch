package com.leads.spring_batch.batch.scheduler;

import com.leads.spring_batch.batch.config.LoanMigrationProperties;
import com.leads.spring_batch.batch.launcher.BatchJobTrigger;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * Triggers the loan migration on the configured cron (default: every day at 03:00). The cron
 * expression and zone come from {@link LoanMigrationProperties} so ops can change them without a
 * code change; set {@code batch.loan-migration.schedule.enabled=false} to switch it off.
 */
@Component
@ConditionalOnProperty(
    prefix = "batch.loan-migration.schedule",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class LoanMigrationScheduler implements SchedulingConfigurer {

  private static final Logger log = LoggerFactory.getLogger(LoanMigrationScheduler.class);

  private final BatchJobTrigger jobTrigger;
  private final LoanMigrationProperties.Schedule schedule;

  public LoanMigrationScheduler(BatchJobTrigger jobTrigger, LoanMigrationProperties properties) {
    this.jobTrigger = jobTrigger;
    this.schedule = properties.schedule();
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    log.info("Loan migration scheduled: cron='{}' zone={}", schedule.cron(), schedule.zone());
    registrar.addTriggerTask(
        jobTrigger::trigger, new CronTrigger(schedule.cron(), ZoneId.of(schedule.zone())));
  }
}
