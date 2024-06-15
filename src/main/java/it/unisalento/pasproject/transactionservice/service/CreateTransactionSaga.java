package it.unisalento.pasproject.transactionservice.service;

import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.MessageDTO;
import it.unisalento.pasproject.transactionservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.transactionservice.dto.TransactionDTO;
import it.unisalento.pasproject.transactionservice.exception.CommunicationErrorException;
import it.unisalento.pasproject.transactionservice.exception.DatabaseErrorException;
import it.unisalento.pasproject.transactionservice.repositories.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


// Deve inviare un messaggio al servizio wallet per prelevare i soldi dal wallet del sender
// Il Wallet deve quindi controllare se il sender ha abbastanza soldi per la transazione
// Se il sender ha abbastanza soldi, il wallet deve prelevare i soldi e aggiungerli al receiver (se abilitato)
// Se il sender non ha abbastanza soldi, la transazione deve fallire
// Se il receiver non è abilitato, la transazione deve fallire
// Se il receiver è abilitato, la transazione deve essere completata
// IL wallet manda un messaggio al servizio payment per completare o meno la transazione
// Il servizio payment deve aggiungere la transazione al database

@Service
public class CreateTransactionSaga {

    private final TransactionService transactionService;
    private final TransactionRepository repository;
    private final MessageService messageService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTransactionSaga.class);

    @Autowired
    public CreateTransactionSaga(TransactionService transactionService, TransactionRepository repository, MessageService messageService) {
        this.transactionService = transactionService;
        this.repository = repository;
        this.messageService = messageService;
    }

    public TransactionDTO createTransaction(TransactionCreationDTO transactionDto) throws DatabaseErrorException {

        Transaction transaction = new Transaction();
        transaction.setSenderEmail(transactionDto.getSenderEmail());
        transaction.setReceiverEmail(transactionDto.getReceiverEmail());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setCreationDate(LocalDateTime.now());

        if (transactionDto.getTransactionOwner() == null) {
            transaction.setTransactionOwner(transaction.getSenderEmail());
        } else {
            transaction.setTransactionOwner(transactionDto.getTransactionOwner());
        }

        //Salvo la transazione
        try{
            transaction = repository.save(transaction);
        } catch (Exception e) {
            LOGGER.error("Database error occurred.");
        }

        LOGGER.info("Transaction created with id: " + transaction.getId());

        TransactionDTO transactionDTO = transactionService.getTransactionDTO(transaction);

        // Invio messaggio di handshake al servizio wallet
        LOGGER.info("Sending transaction {} request to wallet service.", transaction.getId());
        transactionService.requestTransaction(transactionDTO);

        return transactionService.getTransactionDTO(transaction);
    }

    @RabbitListener(queues = "${rabbitmq.queue.responseTransaction.name}")
    public void receiveTransactionResponse(MessageDTO messageDTO) throws CommunicationErrorException {
        try {
            if (messageDTO == null) {
                LOGGER.error("Communication error occurred.");
            }

            LOGGER.info("Received response for transaction: " + messageDTO.getResponse() + " with code: " + messageDTO.getCode());

            Optional<Transaction> ret = repository.findById(messageDTO.getResponse());
            if (ret.isEmpty()) {
                throw new CommunicationErrorException("Communication error occurred.");
            }
            Transaction transaction = ret.get();

            //Se ritorna != da 200 la transazione non è andata a buon fine e non viene completata
            //Anche se non completata si salva tutto
            //ZUCKEMBERGGG
            transaction.setCompleted(messageDTO.getCode() == 200);

            transaction.setCompletionDate(LocalDateTime.now());
            repository.save(transaction);

            // Notifico della chiusura della transazione sul canale di notifica (esito positivo o negativo)
            transactionService.notifyTransactionCompleted(transactionService.getTransactionDTO(transaction));
        }
        catch (Exception e) {
            LOGGER.error("Transaction response error.");
        }

    }

    @RabbitListener(queues = "${rabbitmq.queue.receiveTransaction.name}")
    public void receiveTransactionRequest(TransactionCreationDTO transactionCreationDTO) throws DatabaseErrorException {
        LOGGER.info("Received transaction request from service.");
        try{
            createTransaction(transactionCreationDTO);
        } catch (Exception e) {
            LOGGER.error("Transaction creation error.");
        }
    }


}