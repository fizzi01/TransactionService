package it.unisalento.pasproject.paymentservice.service;

import com.rabbitmq.stream.Message;
import it.unisalento.pasproject.paymentservice.domain.Transaction;
import it.unisalento.pasproject.paymentservice.dto.MessageDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.paymentservice.exception.CommunicationErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


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

    @Autowired
    private TransactionService transactionService;


    @Autowired
    private MessageService messageService;

    public Transaction createTransaction(TransactionCreationDTO transactionDto) throws CommunicationErrorException {

        Transaction transaction = new Transaction();
        transaction.setSenderEmail(transactionDto.getSenderEmail());
        transaction.setReceiverEmail(transactionDto.getReceiverEmail());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setCreationDate(LocalDateTime.now());

        // Check if the sender has enough money
        MessageDTO response = transactionService.requestTransaction(transactionDto);

        if (messageService.isServerError(response)) {
            throw new CommunicationErrorException();
        }

        if (messageService.isError(response)) {
            // The sender does not have enough money or the wallet is disabled
            transaction.setCompleted(false);
            transaction.setCompletionDate(LocalDateTime.now());
            return transaction;
        }

        // Save the transaction to the database
        //paymentService.saveTransaction(transaction);

        return transaction;
    }
}