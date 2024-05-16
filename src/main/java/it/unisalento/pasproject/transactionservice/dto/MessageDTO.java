package it.unisalento.pasproject.transactionservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {

    //Messaggio di risposta
    private String response;

    //Codice di risposta stile http (200, 404, 500, ...)
    private int code;


}
