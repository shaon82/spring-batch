package com.leads.spring_batch.batch.listener;

import static com.leads.spring_batch.batch.config.LoanMigrationJobConfig.WORKER_STEP;

import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

/** Logs the overall outcome of a run: status, duration and total rows moved. */
@Component
public class LoanMigrationJobListener implements JobExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(LoanMigrationJobListener.class);

  @Override
  public void beforeJob(JobExecution jobExecution) {
    log.info("Job '{}' started, params={}", jobExecution.getJobInstance().getJobName(),
        jobExecution.getJobParameters());
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    long migrated = 0;
    for (StepExecution step : jobExecution.getStepExecutions()) {
      // partition step executions are named "<workerStep>:partitionN"
      if (step.getStepName().startsWith(WORKER_STEP + ":")) {
        migrated += step.getWriteCount();
      }
    }
    Duration took =
        jobExecution.getStartTime() == null
            ? Duration.ZERO
            : Duration.between(jobExecution.getStartTime(), LocalDateTime.now());

    if (jobExecution.getStatus().isUnsuccessful()) {
      log.error("Job '{}' finished with status={} after {} migrated={} failures={}",
          jobExecution.getJobInstance().getJobName(), jobExecution.getStatus(), took, migrated,
          jobExecution.getAllFailureExceptions().size());
    } else {
      log.info("Job '{}' finished with status={} after {} migrated={}",
          jobExecution.getJobInstance().getJobName(), jobExecution.getStatus(), took, migrated);
    }
  }
}
