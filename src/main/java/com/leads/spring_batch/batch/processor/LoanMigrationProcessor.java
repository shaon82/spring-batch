package com.leads.spring_batch.batch.processor;

import com.leads.spring_batch.batch.mapper.LoanAccInfoMapper;
import com.leads.spring_batch.destination.LoanAccInfo;
import com.leads.spring_batch.source.LoanAccInfoStage;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/** Adapts the mapper to Spring Batch; stateless, so safe to share between worker threads. */
@Component
public class LoanMigrationProcessor implements ItemProcessor<LoanAccInfoStage, LoanAccInfo> {

  private final LoanAccInfoMapper mapper;

  public LoanMigrationProcessor(LoanAccInfoMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public LoanAccInfo process(LoanAccInfoStage item) {
    return mapper.toDestination(item);
  }
}
