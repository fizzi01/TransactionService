package it.unisalento.pasproject.transactionservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InvoiceItemListDTO {
    List<InvoiceItemDTO> invoiceItemDTOS;

    public InvoiceItemListDTO() {
        this.invoiceItemDTOS = new ArrayList<>();
    }
}
