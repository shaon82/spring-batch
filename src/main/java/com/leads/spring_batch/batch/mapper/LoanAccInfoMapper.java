package com.leads.spring_batch.batch.mapper;

import com.leads.spring_batch.destination.LoanAccInfo;
import com.leads.spring_batch.source.LoanAccInfoStage;
import org.springframework.stereotype.Component;

/** Pure field mapping between the staging entity and the destination entity. */
@Component
public class LoanAccInfoMapper {

  public LoanAccInfo toDestination(LoanAccInfoStage source) {
    return new LoanAccInfo()
        .setAccountNumber(source.getAccountNumber())
        .setAccountName(source.getAccountName())
        .setAccountType(source.getAccountType())
        .setAmount(source.getAmount())
        .setInterestRate(source.getInterestRate())
        .setInterest(source.getInterest())
        .setPayment(source.getPayment())
        .setStatus(source.getStatus());
  }
}
