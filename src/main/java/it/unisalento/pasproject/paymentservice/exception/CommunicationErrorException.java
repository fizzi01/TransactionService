package it.unisalento.pasproject.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Communication error occurred.")
public class CommunicationErrorException extends Exception{
}
