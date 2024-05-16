package it.unisalento.pasproject.transactionservice.exception;

import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends CustomErrorException {

    public TransactionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

}
