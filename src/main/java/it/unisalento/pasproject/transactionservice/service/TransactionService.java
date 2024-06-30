package it.unisalento.pasproject.transactionservice.service;

import it.unisalento.pasproject.transactionservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.transactionservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.*;
import it.unisalento.pasproject.transactionservice.repositories.TransactionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    @Value("${rabbitmq.routing.execTransaction.name}")
    private String transactionExecutionRoutingKey;

    @Value("${rabbitmq.exchange.transaction.name}")
    private String transactionExchange;

    @Value("${rabbitmq.queue.responseTransaction.name}")
    private String responseTransactionQueue;

    @Value("${rabbitmq.routing.notifyTransaction.name}")
    private String notifyTransactionRoutingKey;

    private final MessageProducer messageProducer;

    @Autowired
    public TransactionService (MessageProducer messageProducer, @Qualifier("RabbitMQProducer") MessageProducerStrategy messageProducerStrategy, TransactionRepository transactionRepository) {
        this.messageProducer = messageProducer;
        messageProducer.setStrategy(messageProducerStrategy);
        this.transactionRepository = transactionRepository;
    }


    public void requestTransaction(TransactionDTO transaction) {

        RequestTransactionDTO requestTransactionDTO = new RequestTransactionDTO();
        requestTransactionDTO.setId(transaction.getId());
        requestTransactionDTO.setSenderEmail(transaction.getSenderEmail());
        requestTransactionDTO.setReceiverEmail(transaction.getReceiverEmail());
        requestTransactionDTO.setAmount(transaction.getAmount());

        //Invia messaggio al wallet
        messageProducer.sendMessage(requestTransactionDTO, transactionExecutionRoutingKey, transactionExchange, responseTransactionQueue);
    }

    public void notifyTransactionCompleted(TransactionDTO transaction) {
        messageProducer.sendMessage(transaction, notifyTransactionRoutingKey, transactionExchange);
    }

    public TransactionDTO getTransactionDTO(Transaction transaction) {
        TransactionDTO responseTransaction = new TransactionDTO();
        responseTransaction.setId(transaction.getId());
        responseTransaction.setSenderEmail(transaction.getSenderEmail());
        responseTransaction.setReceiverEmail(transaction.getReceiverEmail());
        responseTransaction.setTransactionOwner(transaction.getTransactionOwner());
        responseTransaction.setAmount(transaction.getAmount());
        responseTransaction.setDescription(transaction.getDescription());
        responseTransaction.setCreationDate(transaction.getCreationDate());
        responseTransaction.setCompleted(transaction.isCompleted());
        responseTransaction.setCompletionDate(transaction.getCompletionDate());
        return responseTransaction;
    }

    public InvoiceItemListDTO getInvoiceItemListDTO(List<Transaction> transactions) {
        InvoiceItemListDTO invoiceItemListDTO = new InvoiceItemListDTO();

        for (Transaction transaction : transactions) {
            InvoiceItemDTO invoiceItemDTO = new InvoiceItemDTO();
            invoiceItemDTO.setSenderEmail(transaction.getSenderEmail());
            invoiceItemDTO.setDescription(transaction.getDescription());
            invoiceItemDTO.setAmount(transaction.getAmount());

            invoiceItemListDTO.getInvoiceItemDTOS().add(invoiceItemDTO);
        }

        return invoiceItemListDTO;
    }

    //TODO: Implement this method
    @RabbitListener(queues = "${rabbitmq.queue.requestTransaction.name}")
    public InvoiceItemListDTO getInvoiceTransaction(TransactionRequestMessageDTO transactionRequestMessageDTO) {
        List<Transaction> transactions = transactionRepository.findBySenderEmailAndCompletionDateAfterAndCompletionDateBefore(
                transactionRequestMessageDTO.getUserEmail(),
                transactionRequestMessageDTO.getFrom(),
                transactionRequestMessageDTO.getTo()
        );

        if (!transactions.isEmpty()){
            return getInvoiceItemListDTO(transactions);
        }

        return null;
    }

}
