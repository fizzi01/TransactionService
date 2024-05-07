package it.unisalento.pasproject.paymentservice.service;

import it.unisalento.pasproject.paymentservice.dto.MessageDTO;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    public boolean isError(MessageDTO response) {
        return response.getCode() != 200;
    }

    public boolean isServerError(MessageDTO response) {
        return response.getCode() == 500;
    }

    public String getResponse(MessageDTO response) {
        return response.getResponse();
    }

}
