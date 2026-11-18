package com.leads.spring_batch.batch.partition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;

class IdRangePartitionerTest {

  @Test
  void splitsRangeIntoContiguousNonOverlappingPartitionsCoveringEveryId() {
    Map<String, ExecutionContext> result = IdRangePartitioner.split(1, 10, 3);

    assertThat(result).hasSize(3);
    assertThat(range(result, "partition0")).containsExactly(1L, 4L);
    assertThat(range(result, "partition1")).containsExactly(5L, 8L);
    assertThat(range(result, "partition2")).containsExactly(9L, 10L);
  }

  @Test
  void createsFewerPartitionsWhenThereAreFewerIdsThanGridSize() {
    Map<String, ExecutionContext> result = IdRangePartitioner.split(100, 101, 8);

    assertThat(result).hasSize(2);
    assertThat(range(result, "partition0")).containsExactly(100L, 100L);
    assertThat(range(result, "partition1")).containsExactly(101L, 101L);
  }

  @Test
  void singleIdProducesSinglePartition() {
    Map<String, ExecutionContext> result = IdRangePartitioner.split(7, 7, 4);

    assertThat(result).hasSize(1);
    assertThat(range(result, "partition0")).containsExactly(7L, 7L);
  }

  @Test
  void handlesFiveMillionRowsWithoutGapsOrOverlap() {
    Map<String, ExecutionContext> result = IdRangePartitioner.split(1, 5_000_000, 8);

    long expectedNext = 1;
    for (int i = 0; i < result.size(); i++) {
      long[] r = range(result, "partition" + i);
      assertThat(r[0]).isEqualTo(expectedNext);
      expectedNext = r[1] + 1;
    }
    assertThat(expectedNext).isEqualTo(5_000_001L);
  }

  private static long[] range(Map<String, ExecutionContext> partitions, String name) {
    ExecutionContext ctx = partitions.get(name);
    return new long[] {ctx.getLong(IdRangePartitioner.MIN_ID), ctx.getLong(IdRangePartitioner.MAX_ID)};
  }
}
