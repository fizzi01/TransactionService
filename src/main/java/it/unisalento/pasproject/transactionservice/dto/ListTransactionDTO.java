package it.unisalento.pasproject.transactionservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListTransactionDTO {

    private List<TransactionDTO> transactions;
}
