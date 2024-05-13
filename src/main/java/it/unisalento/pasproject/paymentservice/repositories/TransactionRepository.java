package it.unisalento.pasproject.paymentservice.repositories;

import it.unisalento.pasproject.paymentservice.domain.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, String>{
    List<Transaction> findAllBySenderEmail(String senderEmail);
    List<Transaction> findAllByReceiverEmail(String receiverEmail);
    List<Transaction> findAllBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);
    List<Transaction> findAllBySenderEmailAndReceiverEmail(String senderEmail, String receiverEmail);
    List<Transaction> findAllByIdOrSenderEmailOrReceiverEmail(String id, String senderEmail, String receiverEmail);
    List<Transaction> findAllByCompleted(boolean isCompleted);
}
