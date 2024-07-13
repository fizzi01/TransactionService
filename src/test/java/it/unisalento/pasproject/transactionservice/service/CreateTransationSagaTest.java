package it.unisalento.pasproject.transactionservice.service;

import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.MessageDTO;
import it.unisalento.pasproject.transactionservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.transactionservice.exception.CommunicationErrorException;
import it.unisalento.pasproject.transactionservice.exception.DatabaseErrorException;
import it.unisalento.pasproject.transactionservice.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@SpringBootTest(classes = {CreateTransactionSaga.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateTransactionSagaTest {

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private TransactionRepository repository;

    @MockBean
    private MessageService messageService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CreateTransactionSaga createTransactionSaga;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = mock(TransactionService.class);
        repository = mock(TransactionRepository.class);
        messageService = mock(MessageService.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        createTransactionSaga = new CreateTransactionSaga(transactionService, repository, messageService);

        when(transactionService.getTransactionDTO(any(Transaction.class))).thenCallRealMethod();
    }

    @Test
void createTransaction_successfullyCreatesTransaction() throws DatabaseErrorException {
    TransactionCreationDTO transactionCreationDTO = new TransactionCreationDTO();
    transactionCreationDTO.setSenderEmail("sender@example.com");
    transactionCreationDTO.setReceiverEmail("receiver@example.com");
    transactionCreationDTO.setAmount(100);
    transactionCreationDTO.setDescription("Test transaction");
    when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

    createTransactionSaga.createTransaction(transactionCreationDTO);

    verify(repository).save(transactionCaptor.capture());
    Transaction capturedTransaction = transactionCaptor.getValue();

    assertEquals("sender@example.com", capturedTransaction.getSenderEmail());
    assertEquals("receiver@example.com", capturedTransaction.getReceiverEmail());
    assertEquals(100, capturedTransaction.getAmount());
    assertEquals("Test transaction", capturedTransaction.getDescription());
    assertNotNull(capturedTransaction.getCreationDate());
}

@Test
void receiveTransactionResponse_updatesTransactionSuccessfully_transactionCompleted() throws CommunicationErrorException {
    MessageDTO messageDTO = new MessageDTO();
    messageDTO.setCode(200);
    messageDTO.setResponse("1");

    Transaction transaction = new Transaction();
    transaction.setId("1");
    transaction.setAmount(100);
    transaction.setCompleted(false);
    when(repository.findById("1")).thenReturn(Optional.of(transaction));

    createTransactionSaga.receiveTransactionResponse(messageDTO);

    verify(repository).save(transactionCaptor.capture());
    Transaction updatedTransaction = transactionCaptor.getValue();

    assertTrue(updatedTransaction.isCompleted());
    assertNotNull(updatedTransaction.getCompletionDate());
}

    @Test
    void receiveTransactionResponse_updatesTransactionSuccessfully_transactionFailed() throws CommunicationErrorException {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setCode(400);
        messageDTO.setResponse("1");

        Transaction transaction = new Transaction();
        transaction.setId("1");
        transaction.setAmount(100);
        transaction.setCompleted(false);
        when(repository.findById("1")).thenReturn(Optional.of(transaction));

        createTransactionSaga.receiveTransactionResponse(messageDTO);

        verify(repository).save(transactionCaptor.capture());
        Transaction updatedTransaction = transactionCaptor.getValue();

        assertFalse(updatedTransaction.isCompleted());
        assertNotNull(updatedTransaction.getCompletionDate());
    }

@Test
void receiveTransactionRequest_createsTransactionOnRequest() throws DatabaseErrorException {
    TransactionCreationDTO transactionCreationDTO = new TransactionCreationDTO();
    transactionCreationDTO.setSenderEmail("sender@example.com");
    transactionCreationDTO.setReceiverEmail("receiver@example.com");
    transactionCreationDTO.setAmount(100);
    transactionCreationDTO.setDescription("Test transaction");

    createTransactionSaga.receiveTransactionRequest(transactionCreationDTO);

    verify(repository).save(transactionCaptor.capture());
    Transaction capturedTransaction = transactionCaptor.getValue();

    assertEquals("sender@example.com", capturedTransaction.getSenderEmail());
    assertEquals("receiver@example.com", capturedTransaction.getReceiverEmail());
    assertEquals(100, capturedTransaction.getAmount());
    assertEquals("Test transaction", capturedTransaction.getDescription());
    assertNotNull(capturedTransaction.getCreationDate());
}
}
