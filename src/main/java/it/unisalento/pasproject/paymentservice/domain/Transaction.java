package it.unisalento.pasproject.paymentservice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Document(collection = "transactions")
public class Transaction {
    @Id
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
