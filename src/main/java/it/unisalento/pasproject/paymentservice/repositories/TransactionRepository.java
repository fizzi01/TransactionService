package it.unisalento.pasproject.paymentservice.repositories;

import it.unisalento.pasproject.paymentservice.domain.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String>{
}
