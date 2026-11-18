package com.leads.spring_batch.batch.config;

/** Table names used by the raw-SQL parts of the migration (partitioner, reader, truncate step). */
public final class LoanStageQueries {

  /** Source table of {@code LoanAccInfoStage}. */
  public static final String SOURCE_TABLE = "loan_acc_info_stage";

  /** Destination table of {@code LoanAccInfo}. */
  public static final String DESTINATION_TABLE = "loan_acc_info";

  private LoanStageQueries() {}
}
