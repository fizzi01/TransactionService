package it.unisalento.pasproject.transactionservice.exception;

import it.unisalento.pasproject.transactionservice.exception.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class UserNotAuthorizedException extends CustomErrorException {

    public UserNotAuthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
