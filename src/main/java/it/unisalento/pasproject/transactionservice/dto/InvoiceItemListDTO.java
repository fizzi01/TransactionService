package it.unisalento.pasproject.transactionservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InvoiceItemListDTO {
    List<InvoiceItemDTO> items;

    public InvoiceItemListDTO() {
        this.items = new ArrayList<>();
    }
}
