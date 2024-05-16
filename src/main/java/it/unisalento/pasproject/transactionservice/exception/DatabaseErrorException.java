package it.unisalento.pasproject.transactionservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Database error occurred.")
public class DatabaseErrorException extends Exception {
}
