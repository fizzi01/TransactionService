package it.unisalento.pasproject.paymentservice.service;

import it.unisalento.pasproject.paymentservice.domain.Transaction;
import it.unisalento.pasproject.paymentservice.dto.MessageDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionDTO;
import it.unisalento.pasproject.paymentservice.exception.CommunicationErrorException;
import it.unisalento.pasproject.paymentservice.exception.DatabaseErrorException;
import it.unisalento.pasproject.paymentservice.repositories.TransactionRepository;
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
            throw new DatabaseErrorException();
        }

        TransactionDTO transactionDTO = transactionService.getTransactionDTO(transaction);

        // Invio messaggio di handshake al servizio wallet
        transactionService.requestTransaction(transactionDTO);

        return transactionService.getTransactionDTO(transaction);
    }

    @RabbitListener(queues = "${rabbitmq.queue.responseTransaction.name}")
    public void receiveTransactionResponse(MessageDTO messageDTO) throws CommunicationErrorException {

        if(messageDTO == null){
            throw new CommunicationErrorException();
        }

        Optional<Transaction> ret = repository.findById(messageDTO.getResponse());
        if(ret.isEmpty()){
            throw new CommunicationErrorException();
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

    @RabbitListener(queues = "${rabbitmq.queue.receiveTransaction.name}")
    public void receiveTransactionRequest(TransactionCreationDTO transactionCreationDTO) throws DatabaseErrorException {
        createTransaction(transactionCreationDTO);
    }


}