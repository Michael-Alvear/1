package com.shepherdmoney.interviewproject.vo.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UpdateBalancePayload {

    private String creditCardNumber;
    
    private LocalDate balanceDate;

    private double balanceAmount;

    public LocalDate getTransactionTime() {
        return balanceDate;
    }

    public double getTransactionAmount() {
        return balanceAmount;
    }

    public void setTransactionTime(LocalDate transactionTime) {
        this.balanceDate = transactionTime;
    }
}
