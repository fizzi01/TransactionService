package it.unisalento.pasproject.transactionservice.exception;

import it.unisalento.pasproject.transactionservice.exception.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class BadFormatRequestException extends CustomErrorException {
    public BadFormatRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
