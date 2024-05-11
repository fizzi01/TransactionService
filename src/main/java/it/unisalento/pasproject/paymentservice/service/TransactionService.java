package it.unisalento.pasproject.paymentservice.service;

import it.unisalento.pasproject.paymentservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.paymentservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.paymentservice.domain.Transaction;
import it.unisalento.pasproject.paymentservice.dto.MessageDTO;
import it.unisalento.pasproject.paymentservice.dto.RequestTransactionDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {


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
    public TransactionService (MessageProducer messageProducer,@Qualifier("RabbitMQProducer") MessageProducerStrategy messageProducerStrategy) {
        this.messageProducer = messageProducer;
        messageProducer.setStrategy(messageProducerStrategy);
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
        responseTransaction.setAmount(transaction.getAmount());
        responseTransaction.setDescription(transaction.getDescription());
        responseTransaction.setCreationDate(transaction.getCreationDate());
        responseTransaction.setCompleted(transaction.isCompleted());
        responseTransaction.setCompletionDate(transaction.getCompletionDate());
        return responseTransaction;
    }

}
