package it.unisalento.pasproject.transactionservice.exception;

import it.unisalento.pasproject.transactionservice.exception.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends CustomErrorException {

    public TransactionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

}
