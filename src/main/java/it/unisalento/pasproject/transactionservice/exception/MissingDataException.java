package it.unisalento.pasproject.transactionservice.exception;

import it.unisalento.pasproject.transactionservice.exception.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class MissingDataException extends CustomErrorException {
    public MissingDataException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
