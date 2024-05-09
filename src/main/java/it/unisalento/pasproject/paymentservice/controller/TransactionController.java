package it.unisalento.pasproject.paymentservice.controller;

import it.unisalento.pasproject.paymentservice.dto.RequestTransactionDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionDTO;
import it.unisalento.pasproject.paymentservice.exception.DatabaseErrorException;
import it.unisalento.pasproject.paymentservice.service.CreateTransactionSaga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionSaga createTransactionSaga;

    @Autowired
    public TransactionController(CreateTransactionSaga createTransactionSaga) {
        this.createTransactionSaga = createTransactionSaga;
    }

    @PostMapping(value="/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionDTO createTransaction(@RequestBody TransactionCreationDTO transactionCreationDTO) throws DatabaseErrorException {
        return createTransactionSaga.createTransaction(transactionCreationDTO);
    }

}
