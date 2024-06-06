package it.unisalento.pasproject.transactionservice.controller;

import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.ListTransactionDTO;
import it.unisalento.pasproject.transactionservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.transactionservice.dto.TransactionDTO;
import it.unisalento.pasproject.transactionservice.exception.DatabaseErrorException;
import it.unisalento.pasproject.transactionservice.exception.TransactionNotFoundException;
import it.unisalento.pasproject.transactionservice.repositories.TransactionRepository;
import it.unisalento.pasproject.transactionservice.service.CreateTransactionSaga;
import it.unisalento.pasproject.transactionservice.service.TransactionService;
import it.unisalento.pasproject.transactionservice.service.UserCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static it.unisalento.pasproject.transactionservice.security.SecurityConstants.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionSaga createTransactionSaga;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final UserCheckService userCheckService;

    @Autowired
    public TransactionController(CreateTransactionSaga createTransactionSaga, TransactionRepository transactionRepository, TransactionService transactionService, UserCheckService userCheckService) {
        this.createTransactionSaga = createTransactionSaga;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.userCheckService = userCheckService;
    }

    @PostMapping(value="/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionDTO createTransaction(@RequestBody TransactionCreationDTO transactionCreationDTO) throws DatabaseErrorException {
        return createTransactionSaga.createTransaction(transactionCreationDTO);
    }

    /**
     * This method is used to get all transactions (Admin only)
     * @return ListTransactionDTO
     */
    @GetMapping(value="/findall")
    @Secured({ROLE_ADMIN})
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

        //Controlla che l'utente sia autorizzato a visualizzare la transazione, ovvero se è il receiver o il sender corrisponde all'utente loggato
        //se ADMIN può vedere tutte le transazioni
        if(!userCheckService.isCorrectUser(transaction.get().getSenderEmail())
                && !userCheckService.isCorrectUser(transaction.get().getReceiverEmail())
                && !userCheckService.isAdministrator()){
            throw new TransactionNotFoundException("Transaction not found with id: " + id);
        }

        return transactionService.getTransactionDTO(transaction.get());
    }

    @GetMapping(value="/find/status")
    @Secured({ROLE_ADMIN})
    public ListTransactionDTO getCompletedTransactions(@RequestParam boolean completed) {
        ListTransactionDTO listTransactionDTO = new ListTransactionDTO();
        listTransactionDTO.setTransactions(transactionRepository.findAllByCompleted(completed).stream().map(transactionService::getTransactionDTO).toList());
        return listTransactionDTO;
    }

    @GetMapping(value="/find")
    public ListTransactionDTO getTransactionsByEmail(@RequestParam String id, @RequestParam String email) {

        //Controlla che l'utente sia autorizzato a visualizzare la transazione, ovvero se è il receiver o il sender corrisponde all'utente loggato
        //se ADMIN può vedere tutte le transazioni
        if(!userCheckService.isCorrectUser(email) && !userCheckService.isAdministrator()){
            throw new TransactionNotFoundException("Transactions not found with id: " + id);
        }

        ListTransactionDTO listTransactionDTO = new ListTransactionDTO();
        listTransactionDTO.setTransactions(transactionRepository.findAllByIdOrSenderEmailOrReceiverEmail(id,email, email).stream().map(transactionService::getTransactionDTO).toList());
        return listTransactionDTO;
    }


}
