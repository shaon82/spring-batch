package com.leads.spring_batch.destination;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LoanAccInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String accountNumber;
  private String accountName;
  private String accountType;
  private Double amount;
  private Double interestRate;
  private Double interest;
  private Double payment;

  private Boolean status;

  public Long getId() {
    return id;
  }

  public LoanAccInfo setId(Long id) {
    this.id = id;
    return this;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public LoanAccInfo setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
    return this;
  }

  public String getAccountName() {
    return accountName;
  }

  public LoanAccInfo setAccountName(String accountName) {
    this.accountName = accountName;
    return this;
  }

  public String getAccountType() {
    return accountType;
  }

  public LoanAccInfo setAccountType(String accountType) {
    this.accountType = accountType;
    return this;
  }

  public Double getAmount() {
    return amount;
  }

  public LoanAccInfo setAmount(Double amount) {
    this.amount = amount;
    return this;
  }

  public Double getInterestRate() {
    return interestRate;
  }

  public LoanAccInfo setInterestRate(Double interestRate) {
    this.interestRate = interestRate;
    return this;
  }

  public Double getInterest() {
    return interest;
  }

  public LoanAccInfo setInterest(Double interest) {
    this.interest = interest;
    return this;
  }

  public Double getPayment() {
    return payment;
  }

  public LoanAccInfo setPayment(Double payment) {
    this.payment = payment;
    return this;
  }

  public Boolean getStatus() {
    return status;
  }

  public LoanAccInfo setStatus(Boolean status) {
    this.status = status;
    return this;
  }
}
