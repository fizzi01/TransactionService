package it.unisalento.pasproject.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomErrorResponse {
    private String traceId;
    private OffsetDateTime timestamp;
    private HttpStatus status;
    private String message;
}
