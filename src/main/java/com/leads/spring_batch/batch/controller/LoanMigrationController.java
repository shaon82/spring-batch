package com.leads.spring_batch.batch.controller;

import com.leads.spring_batch.batch.launcher.BatchJobTrigger;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manual trigger for the loan migration job, e.g. for debugging or on-demand runs.
 *
 * <p>The call is <b>synchronous</b>: the HTTP response is sent only after the whole job has
 * finished (or was skipped because a previous run is still active). Because the job TRUNCATEs the
 * destination table, this endpoint is only registered when
 * {@code batch.loan-migration.api.enabled=true}; keep it off (or protected) in production.
 */
@RestController
@RequestMapping("/api/batch/loan-migration")
//@ConditionalOnProperty(prefix = "batch.loan-migration.api", name = "enabled", havingValue = "true")
public class LoanMigrationController {

  private final BatchJobTrigger jobTrigger;

  public LoanMigrationController(BatchJobTrigger jobTrigger) {
    this.jobTrigger = jobTrigger;
  }

  @GetMapping("/trigger")
  public ResponseEntity<Map<String, String>> trigger() {
    jobTrigger.trigger();
    return ResponseEntity.ok(
        Map.of(
            "message", "Loan migration trigger returned. Check the application log or "
                + "BATCH_JOB_EXECUTION for the outcome."));
  }
}
