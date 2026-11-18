package com.leads.spring_batch.batch.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

class ClearDestinationTaskletTest {

  @Test
  void truncatesOnlyTheDestinationTableAndFinishesInOneExecution() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    RepeatStatus status = new ClearDestinationTasklet(jdbcTemplate).execute(null, null);

    verify(jdbcTemplate).execute("TRUNCATE TABLE loan_acc_info");
    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
  }
}
