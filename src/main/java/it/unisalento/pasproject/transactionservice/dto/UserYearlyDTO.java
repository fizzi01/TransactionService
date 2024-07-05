package it.unisalento.pasproject.transactionservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserYearlyDTO {
    private String senderEmail;
    private int month;
    private int year;
    private float totalAmount;
    private int totalTransactions;
    private float amountPerTransaction;
    private float averageAmount;
    private float maxAmount;
    private float minAmount;
}
