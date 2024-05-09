package it.unisalento.pasproject.paymentservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TransactionDTO {
    private String id;
    private String senderEmail;
    private String receiverEmail;

    private double amount;

    private String description;

    // DTO: @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime creationDate;
    private LocalDateTime completionDate;

    private boolean isCompleted;
}
