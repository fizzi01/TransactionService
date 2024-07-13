package it.unisalento.pasproject.transactionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.pasproject.transactionservice.TestSecurityConfig;
import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.ListTransactionDTO;
import it.unisalento.pasproject.transactionservice.dto.TransactionCreationDTO;
import it.unisalento.pasproject.transactionservice.dto.TransactionDTO;
import it.unisalento.pasproject.transactionservice.exception.TransactionNotFoundException;
import it.unisalento.pasproject.transactionservice.repositories.TransactionRepository;
import it.unisalento.pasproject.transactionservice.service.CreateTransactionSaga;
import it.unisalento.pasproject.transactionservice.service.TransactionService;
import it.unisalento.pasproject.transactionservice.service.UserCheckService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc()
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateTransactionSaga createTransactionSaga;

    @MockBean
    private TransactionService transactionService;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private UserCheckService userCheckService;

    @MockBean
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionController transactionController;

    private static final String EMAIL = "valid@example.com";
    private static final String RECEIVER_EMAIL = "receiver@example.com";

    @Test
    @WithMockUser(username = EMAIL)
    void createTransaction_whenUserAuthorized_returnsTransactionDTO() throws Exception {
        TransactionCreationDTO transactionCreationDTO = new TransactionCreationDTO();
        transactionCreationDTO.setSenderEmail(EMAIL);
        transactionCreationDTO.setReceiverEmail(RECEIVER_EMAIL);
        transactionCreationDTO.setAmount(100);
        transactionCreationDTO.setDescription("Test transaction");
        transactionCreationDTO.setTransactionOwner(EMAIL);

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(100);
        transactionDTO.setCompleted(true);

        given(userCheckService.isCorrectUser(any())).willReturn(true);
        given(createTransactionSaga.createTransaction(any(TransactionCreationDTO.class))).willReturn(transactionDTO);

        mockMvc.perform(post("/api/transactions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(transactionCreationDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void createTransaction_whenUserNotAuthorized_throwsUserNotAuthorizedException() throws Exception {
        given(userCheckService.isCorrectUser(any())).willReturn(false);

        mockMvc.perform(post("/api/transactions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void getTransactionById_whenTransactionExists_returnsTransactionDTO() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setId("1");
        transactionDTO.setAmount(100);
        transactionDTO.setCompleted(true);

        Transaction transaction = new Transaction();
        transaction.setId("1");
        transaction.setAmount(100);
        transaction.setCompleted(true);

        given(transactionService.getTransactionDTO(any())).willReturn(transactionDTO);
        given(userCheckService.isCorrectUser(any())).willReturn(true);
        given(transactionRepository.findById("1")).willReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/find/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void getTransactionById_whenTransactionDoesNotExist_throwsTransactionNotFoundException() throws Exception {
        mockMvc.perform(get("/api/transactions/find/{id}", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void getTransactionsByEmail_whenUserAuthorized_returnsListTransactionDTO() throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        Transaction transactionOne = new Transaction();
        transactionOne.setId("1");
        transactionOne.setTransactionOwner(EMAIL);
        transactionOne.setSenderEmail(EMAIL);
        transactionOne.setReceiverEmail("receiver@example.com");
        transactionOne.setAmount(100);
        transactionOne.setCompleted(true);
        transactions.add(transactionOne);

        Transaction transactionTwo = new Transaction();
        transactionTwo.setId("2");
        transactionTwo.setTransactionOwner("receiver@example.com");
        transactionTwo.setSenderEmail("receiver@example.com");
        transactionTwo.setReceiverEmail(EMAIL);
        transactionTwo.setAmount(200);
        transactionTwo.setCompleted(true);
        transactions.add(transactionTwo);

        when(transactionService.getTransactionDTO(any(Transaction.class))).thenCallRealMethod();
        when(transactionRepository.findAllByIdOrSenderEmailOrReceiverEmail(null, EMAIL, EMAIL)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/find")
                        .param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions[0].id").value("1"))
                .andExpect(jsonPath("$.transactions[0].amount").value(100))
                .andExpect(jsonPath("$.transactions[1].id").value("2"))
                .andExpect(jsonPath("$.transactions[1].amount").value(200));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void getTransactionsByEmail_whenUserNotAuthorized_throwsTransactionNotFoundException() throws Exception {
        given(userCheckService.isCorrectUser(anyString())).willCallRealMethod();

        mockMvc.perform(get("/api/transactions/find")
                        .param("email", "user@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompletedTransactions_whenAdminRequestsIncompleteTransactions_returnsIncompleteTransactions() throws Exception {
        Transaction transactionOne = new Transaction();
        transactionOne.setId("1");
        transactionOne.setAmount(100);
        transactionOne.setCompleted(false);

        Transaction transactionTwo = new Transaction();
        transactionTwo.setId("2");
        transactionTwo.setAmount(200);
        transactionTwo.setCompleted(false);

        Transaction transactionThree = new Transaction();
        transactionThree.setId("3");
        transactionThree.setAmount(300);
        transactionThree.setCompleted(true);

        List<Transaction> transactions = Arrays.asList(transactionOne, transactionTwo, transactionThree);

        given(transactionRepository.findAllByCompleted(anyBoolean())).willAnswer(
                invocation -> transactions.stream().filter(transaction -> (boolean)invocation.getArgument(0) == transaction.isCompleted()).toList());

        given(transactionService.getTransactionDTO(any(Transaction.class))).willCallRealMethod();

        mockMvc.perform(get("/api/transactions/find/status")
                        .param("completed", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions[0].id").value("1"))
                .andExpect(jsonPath("$.transactions[0].amount").value(100))
                .andExpect(jsonPath("$.transactions[1].id").value("2"))
                .andExpect(jsonPath("$.transactions[1].amount").value(200));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCompletedTransactions_whenNonAdminUserRequests_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/transactions/find/status?completed=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
