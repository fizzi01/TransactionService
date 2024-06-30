package it.unisalento.pasproject.transactionservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionRequestMessageDTO {
    private String userEmail;
    private LocalDateTime from;
    private LocalDateTime to;
}
