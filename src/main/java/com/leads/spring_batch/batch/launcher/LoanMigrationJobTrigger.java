package com.leads.spring_batch.batch.launcher;

import static com.leads.spring_batch.batch.config.LoanMigrationJobConfig.JOB_NAME;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Launches the migration job with unique parameters and refuses to start a second run while one
 * is still in progress (a 5M-row run can outlast the schedule interval).
 */
@Component
public class LoanMigrationJobTrigger implements BatchJobTrigger {

  private static final Logger log = LoggerFactory.getLogger(LoanMigrationJobTrigger.class);

  private final JobLauncher jobLauncher;
  private final JobExplorer jobExplorer;
  private final Job job;

  public LoanMigrationJobTrigger(
      JobLauncher jobLauncher, JobExplorer jobExplorer, @Qualifier(JOB_NAME) Job job) {
    this.jobLauncher = jobLauncher;
    this.jobExplorer = jobExplorer;
    this.job = job;
  }

  @Override
  public void trigger() {
    if (!jobExplorer.findRunningJobExecutions(JOB_NAME).isEmpty()) {
      log.warn("Skipping '{}': a previous execution is still running", JOB_NAME);
      return;
    }

    JobParameters parameters =
        new JobParametersBuilder().addLocalDateTime("triggeredAt", LocalDateTime.now()).toJobParameters();
    try {
      JobExecution execution = jobLauncher.run(job, parameters);
      log.info("'{}' execution {} ended with {}", JOB_NAME, execution.getId(), execution.getStatus());
    } catch (JobExecutionException e) {
      // Never let a failed launch kill the scheduler thread; the next cron tick retries.
      log.error("Could not run '{}'", JOB_NAME, e);
    }
  }
}
