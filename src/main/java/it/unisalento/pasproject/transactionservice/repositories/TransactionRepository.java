package it.unisalento.pasproject.transactionservice.repositories;

import it.unisalento.pasproject.transactionservice.domain.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String>{
    List<Transaction> findAllBySenderEmail(String senderEmail);
    List<Transaction> findAllByReceiverEmail(String receiverEmail);
    List<Transaction> findAllBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);
    List<Transaction> findAllBySenderEmailAndReceiverEmail(String senderEmail, String receiverEmail);
    List<Transaction> findAllByIdOrSenderEmailOrReceiverEmail(String id, String senderEmail, String receiverEmail);
    List<Transaction> findAllByCompleted(boolean isCompleted);
    List<Transaction> findAllBySenderEmailAndCompletionDateBetweenAndIsCompleted(String senderEmail, LocalDateTime from, LocalDateTime to, boolean isCompleted);
}
