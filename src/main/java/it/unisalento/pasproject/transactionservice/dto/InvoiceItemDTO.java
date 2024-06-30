package it.unisalento.pasproject.transactionservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceItemDTO {
    private String senderEmail;
    private String description;
    private double amount;
}
