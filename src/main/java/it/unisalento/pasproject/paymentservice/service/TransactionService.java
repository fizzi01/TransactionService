package it.unisalento.pasproject.paymentservice.service;

import it.unisalento.pasproject.paymentservice.business.io.exchanger.MessageExchangeStrategy;
import it.unisalento.pasproject.paymentservice.business.io.exchanger.MessageExchanger;
import it.unisalento.pasproject.paymentservice.dto.MessageDTO;
import it.unisalento.pasproject.paymentservice.dto.RequestTransactionDTO;
import it.unisalento.pasproject.paymentservice.dto.TransactionCreationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private MessageExchanger messageExchanger;

    @Autowired
    @Qualifier("RabbitMQExchange")
    private MessageExchangeStrategy messageExchangeStrategy;

    public MessageDTO requestTransaction(TransactionCreationDTO transaction) {

        RequestTransactionDTO requestTransactionDTO = new RequestTransactionDTO();
        requestTransactionDTO.setSenderEmail(transaction.getSenderEmail());
        requestTransactionDTO.setReceiverEmail(transaction.getReceiverEmail());
        requestTransactionDTO.setAmount(transaction.getAmount());

        MessageDTO response = messageExchanger.exchangeMessage(requestTransactionDTO,"", "", MessageDTO.class);

        if (response == null) {
            response = new MessageDTO();
            response.setCode(500);
            response.setResponse("Internal server error");
        }

        return response;
    }
}
