package it.unisalento.pasproject.paymentservice.controller;

import it.unisalento.pasproject.paymentservice.domain.Transaction;
import it.unisalento.pasproject.paymentservice.dto.ListTransactionDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionDTO;
import it.unisalento.pasproject.paymentservice.exception.DatabaseErrorException;
import it.unisalento.pasproject.paymentservice.exception.TransactionNotFoundException;
import it.unisalento.pasproject.paymentservice.repositories.TransactionRepository;
import it.unisalento.pasproject.paymentservice.service.CreateTransactionSaga;
import it.unisalento.pasproject.paymentservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionSaga createTransactionSaga;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(CreateTransactionSaga createTransactionSaga, TransactionRepository transactionRepository, TransactionService transactionService) {
        this.createTransactionSaga = createTransactionSaga;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @PostMapping(value="/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionDTO createTransaction(@RequestBody TransactionCreationDTO transactionCreationDTO) throws DatabaseErrorException {
        return createTransactionSaga.createTransaction(transactionCreationDTO);
    }

    @GetMapping(value="/findall")
    public ListTransactionDTO getTransactions() {
        ListTransactionDTO listTransactionDTO = new ListTransactionDTO();
        listTransactionDTO.setTransactions(transactionRepository.findAll().stream().map(transactionService::getTransactionDTO).toList());
        return listTransactionDTO;
    }

    @GetMapping(value="/find/{id}")
    public TransactionDTO getTransactionById(@PathVariable String id) throws TransactionNotFoundException {

        Optional<Transaction> transaction = transactionRepository.findById(id);


        if (transaction.isEmpty()) {
            throw new TransactionNotFoundException("Transaction not found with id: " + id);
        }

        return transactionService.getTransactionDTO(transaction.get());
    }

    @GetMapping(value="/find/status")
    public ListTransactionDTO getCompletedTransactions(@RequestParam boolean completed) {
        ListTransactionDTO listTransactionDTO = new ListTransactionDTO();
        listTransactionDTO.setTransactions(transactionRepository.findAllByCompleted(completed).stream().map(transactionService::getTransactionDTO).toList());
        return listTransactionDTO;
    }

    @GetMapping(value="/find")
    public ListTransactionDTO getTransactionsByEmail(@RequestParam String id, @RequestParam String email) {
        ListTransactionDTO listTransactionDTO = new ListTransactionDTO();
        listTransactionDTO.setTransactions(transactionRepository.findAllByIdOrSenderEmailOrReceiverEmail(id,email, email).stream().map(transactionService::getTransactionDTO).toList());
        return listTransactionDTO;
    }


}
