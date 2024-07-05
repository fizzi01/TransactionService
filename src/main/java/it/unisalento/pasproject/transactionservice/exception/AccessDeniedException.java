package it.unisalento.pasproject.transactionservice.exception;

import it.unisalento.pasproject.transactionservice.exception.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends CustomErrorException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
