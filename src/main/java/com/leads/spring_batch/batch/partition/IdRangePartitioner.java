package com.leads.spring_batch.batch.partition;

import static com.leads.spring_batch.batch.config.LoanStageQueries.SOURCE_TABLE;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Splits the id span of the source table into contiguous ranges, one per partition, so worker
 * threads read disjoint rows and never contend with each other.
 */
@Component
public class IdRangePartitioner implements Partitioner {

  public static final String MIN_ID = "minId";
  public static final String MAX_ID = "maxId";

  private static final String BOUNDS_SQL = "SELECT MIN(id), MAX(id) FROM " + SOURCE_TABLE;

  private final JdbcTemplate jdbcTemplate;

  public IdRangePartitioner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    long[] bounds =
        jdbcTemplate.query(
            BOUNDS_SQL,
            rs -> {
              rs.next();
              long min = rs.getLong(1);
              if (rs.wasNull()) {
                return null; // source table is empty
              }
              return new long[] {min, rs.getLong(2)};
            });

    if (bounds == null) {
      return new HashMap<>();
    }
    return split(bounds[0], bounds[1], gridSize);
  }

  /** Divides {@code [min, max]} into at most {@code gridSize} inclusive, non-overlapping ranges. */
  static Map<String, ExecutionContext> split(long min, long max, int gridSize) {
    long total = max - min + 1;
    long size = (total + gridSize - 1) / gridSize; // ceil

    Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
    for (int i = 0; i < gridSize; i++) {
      long lower = min + i * size;
      if (lower > max) {
        break;
      }
      ExecutionContext context = new ExecutionContext();
      context.putLong(MIN_ID, lower);
      context.putLong(MAX_ID, Math.min(lower + size - 1, max));
      partitions.put("partition" + i, context);
    }
    return partitions;
  }
}
