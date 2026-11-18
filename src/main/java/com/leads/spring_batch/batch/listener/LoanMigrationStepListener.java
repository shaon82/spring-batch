package com.leads.spring_batch.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/** Logs one summary line per partition (observability only, no business logic). */
@Component
public class LoanMigrationStepListener implements StepExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(LoanMigrationStepListener.class);

  @Override
  public void beforeStep(StepExecution stepExecution) {
    log.info("[{}] started", stepExecution.getStepName());
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    log.info(
        "[{}] finished status={} read={} written={} rollbacks={}",
        stepExecution.getStepName(),
        stepExecution.getStatus(),
        stepExecution.getReadCount(),
        stepExecution.getWriteCount(),
        stepExecution.getRollbackCount());
    return stepExecution.getExitStatus();
  }
}
