package com.leads.spring_batch.batch.tasklet;

import static com.leads.spring_batch.batch.config.LoanStageQueries.DESTINATION_TABLE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * First step of the full refresh: empties the destination table. {@code TRUNCATE} is used instead
 * of {@code DELETE} because it is instant even for millions of rows.
 */
@Component
public class ClearDestinationTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(ClearDestinationTasklet.class);

  static final String TRUNCATE_SQL = "TRUNCATE TABLE " + DESTINATION_TABLE;

  private final JdbcTemplate jdbcTemplate;

  public ClearDestinationTasklet(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    jdbcTemplate.execute(TRUNCATE_SQL);
    log.info("Destination table '{}' truncated", DESTINATION_TABLE);
    return RepeatStatus.FINISHED;
  }
}
