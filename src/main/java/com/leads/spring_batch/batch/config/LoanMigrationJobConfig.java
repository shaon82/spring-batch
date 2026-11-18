package com.leads.spring_batch.batch.config;

import static com.leads.spring_batch.batch.config.LoanStageQueries.SOURCE_TABLE;

import com.leads.spring_batch.batch.listener.LoanMigrationJobListener;
import com.leads.spring_batch.batch.listener.LoanMigrationStepListener;
import com.leads.spring_batch.batch.partition.IdRangePartitioner;
import com.leads.spring_batch.batch.processor.LoanMigrationProcessor;
import com.leads.spring_batch.batch.tasklet.ClearDestinationTasklet;
import com.leads.spring_batch.destination.LoanAccInfo;
import com.leads.spring_batch.source.LoanAccInfoStage;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the full-refresh migration from the staging table to the destination table:
 *
 * <pre>
 * job
 *  ├─ clear step    : TRUNCATE destination
 *  └─ manager step  : IdRangePartitioner ──► N partitions (run in parallel)
 *        └─ worker step (per partition): paging reader ► processor ► JPA batch insert
 * </pre>
 *
 * Every run replaces the destination content with the current staging content, so re-running
 * never produces duplicates. The source table is only read, never modified.
 */
@Configuration
@EnableConfigurationProperties(LoanMigrationProperties.class)
public class LoanMigrationJobConfig {

  public static final String JOB_NAME = "loanMigrationJob";
  public static final String CLEAR_STEP = "loanMigrationClearStep";
  public static final String MANAGER_STEP = "loanMigrationManagerStep";
  public static final String WORKER_STEP = "loanMigrationWorkerStep";

  @Bean
  public Job loanMigrationJob(
      JobRepository jobRepository,
      @Qualifier(CLEAR_STEP) Step clearStep,
      @Qualifier(MANAGER_STEP) Step managerStep,
      LoanMigrationJobListener jobListener) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .start(clearStep)
        .next(managerStep)
        .listener(jobListener)
        .build();
  }

  @Bean(CLEAR_STEP)
  public Step loanMigrationClearStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ClearDestinationTasklet clearDestinationTasklet) {
    return new StepBuilder(CLEAR_STEP, jobRepository)
        .tasklet(clearDestinationTasklet, transactionManager)
        .build();
  }

  @Bean(MANAGER_STEP)
  public Step loanMigrationManagerStep(
      JobRepository jobRepository,
      @Qualifier(WORKER_STEP) Step workerStep,
      IdRangePartitioner partitioner,
      @Qualifier(BatchExecutorConfig.MIGRATION_EXECUTOR) TaskExecutor taskExecutor,
      LoanMigrationProperties properties) {
    return new StepBuilder(MANAGER_STEP, jobRepository)
        .partitioner(WORKER_STEP, partitioner)
        .step(workerStep)
        .gridSize(properties.partitions())
        .taskExecutor(taskExecutor)
        .build();
  }

  @Bean(WORKER_STEP)
  public Step loanMigrationWorkerStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      JdbcPagingItemReader<LoanAccInfoStage> loanStageReader,
      LoanMigrationProcessor processor,
      JpaItemWriter<LoanAccInfo> loanAccInfoWriter,
      LoanMigrationStepListener stepListener,
      LoanMigrationProperties properties) {
    return new StepBuilder(WORKER_STEP, jobRepository)
        .<LoanAccInfoStage, LoanAccInfo>chunk(properties.chunkSize(), transactionManager)
        .reader(loanStageReader)
        .processor(processor)
        .writer(loanAccInfoWriter)
        .listener(stepListener)
        .faultTolerant()
        .retry(TransientDataAccessException.class) // deadlocks, lock timeouts, dropped connections
        .retryLimit(properties.retryLimit())
        .build();
  }

  /** Reads the rows of one partition (an id range), ordered by id, one page per chunk. */
  @Bean
  @StepScope
  public JdbcPagingItemReader<LoanAccInfoStage> loanStageReader(
      DataSource dataSource,
      LoanMigrationProperties properties,
      @Value("#{stepExecutionContext['" + IdRangePartitioner.MIN_ID + "']}") Long minId,
      @Value("#{stepExecutionContext['" + IdRangePartitioner.MAX_ID + "']}") Long maxId) {
    return new JdbcPagingItemReaderBuilder<LoanAccInfoStage>()
        .name("loanStageReader")
        .dataSource(dataSource)
        .selectClause(
            "SELECT id, account_number, account_name, account_type, amount, interest_rate,"
                + " interest, payment, status")
        .fromClause("FROM " + SOURCE_TABLE)
        .whereClause("WHERE id BETWEEN :minId AND :maxId")
        .parameterValues(Map.of(IdRangePartitioner.MIN_ID, minId, IdRangePartitioner.MAX_ID, maxId))
        .sortKeys(Map.of("id", Order.ASCENDING))
        .rowMapper(new BeanPropertyRowMapper<>(LoanAccInfoStage.class))
        .pageSize(properties.chunkSize())
        .fetchSize(properties.chunkSize())
        .build();
  }

  /** Inserts each chunk into the destination as one JDBC batch. */
  @Bean
  public JpaItemWriter<LoanAccInfo> loanAccInfoWriter(EntityManagerFactory entityManagerFactory) {
    return new JpaItemWriterBuilder<LoanAccInfo>()
        .entityManagerFactory(entityManagerFactory)
        .usePersist(true)
        .build();
  }
}
