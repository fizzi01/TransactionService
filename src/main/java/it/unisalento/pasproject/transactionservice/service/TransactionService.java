package it.unisalento.pasproject.transactionservice.service;

import it.unisalento.pasproject.transactionservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.transactionservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.*;
import it.unisalento.pasproject.transactionservice.repositories.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

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
        List<InvoiceItemDTO> invoiceItemDTOList = new ArrayList<>();
        invoiceItemListDTO.setInvoiceItemDTOS(invoiceItemDTOList);

        for (Transaction transaction : transactions) {
            InvoiceItemDTO invoiceItemDTO = new InvoiceItemDTO();
            invoiceItemDTO.setSenderEmail(transaction.getSenderEmail());
            invoiceItemDTO.setDescription(transaction.getDescription());
            invoiceItemDTO.setAmount(transaction.getAmount());

            invoiceItemDTOList.add(invoiceItemDTO);

            LOGGER.info("InvoiceItemDTO: {}", invoiceItemDTO.getSenderEmail());
        }

        LOGGER.info("InvoiceItemListDTO: {}", invoiceItemListDTO.getInvoiceItemDTOS().size());

        return invoiceItemListDTO;
    }

    //TODO: Implement this method
    @RabbitListener(queues = "${rabbitmq.queue.requestTransaction.name}")
    public InvoiceItemListDTO getInvoiceTransaction(TransactionRequestMessageDTO transactionRequestMessageDTO) {
        try {
            LOGGER.info("Received message: {}", transactionRequestMessageDTO.getFrom());
            LOGGER.info("Received message: {}", transactionRequestMessageDTO.getTo());

            List<Transaction> transactions = transactionRepository.findAllBySenderEmailAndCompletionDateBetweenAndCompleted(
                    transactionRequestMessageDTO.getUserEmail(),
                    transactionRequestMessageDTO.getFrom(),
                    transactionRequestMessageDTO.getTo(),
                    true
            );

            LOGGER.info("Received transactions: {}", transactions.size());
            LOGGER.info("Received transactions: {}, {}, {}, {}, {}, {}, {}", transactions.getFirst().getSenderEmail(), transactions.getFirst().getReceiverEmail(), transactions.getFirst().getAmount(), transactions.getFirst().getDescription(), transactions.getFirst().getCreationDate(), transactions.getFirst().getCompletionDate(), transactions.getFirst().isCompleted());
            LOGGER.info("Received transactions: {}", transactions.getLast());

            if (transactions.isEmpty()) {
                return null;
            }

            return getInvoiceItemListDTO(transactions);

        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage());
            return null;
        }
    }

}
