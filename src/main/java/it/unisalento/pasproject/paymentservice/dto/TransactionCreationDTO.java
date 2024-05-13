package it.unisalento.pasproject.paymentservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionCreationDTO {
    private String senderEmail;
    private String receiverEmail;
    private double amount;
    private String description;
    private String transactionOwner;
}
