package com.example.moneytransfer.service.impl;

import com.example.moneytransfer.Enums.Currency;
import com.example.moneytransfer.Enums.Status;
import com.example.moneytransfer.entity.Client;
import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import com.example.moneytransfer.repository.ClientRepository;
import com.example.moneytransfer.repository.TransactionRepository;
import com.example.moneytransfer.repository.UserRepository;
import com.example.moneytransfer.request.SendTransactionRequest;
import com.example.moneytransfer.service.TransactionService;
import com.example.moneytransfer.utils.CodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestPropertySource("/application-test.properties")
@SpringBootTest
@Sql(scripts = "/init-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TransactionServiceImplTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TransactionService transactionService;

    private Transaction testTransaction;

    private SendTransactionRequest sendRequest;

    @BeforeEach
    public void setup() {
        sendRequest = new SendTransactionRequest(
                "dastan",
                "user1name",
                "0999777888",
                "user2name",
                "0777666555",
                "some description",
                BigDecimal.valueOf(300),
                "USD"
        );


        User sender = userRepository.findByUsername(sendRequest.getUserSender()).orElseThrow();

        Client clientSender = Client.builder()
                .name(sendRequest.getUsernameSender())
                .phoneNumber(sendRequest.getPhoneNumberSender())
                .build();

        Client clientReceiver = Client.builder()
                .name(sendRequest.getUsernameReceiver())
                .phoneNumber(sendRequest.getPhoneNumberReceiver())
                .build();

        clientRepository.saveAll(List.of(clientSender, clientReceiver));

        testTransaction = transactionRepository.save(Transaction.builder()
                .id(1L)
                .userSender(sender)
                .senderClient(clientSender)
                .receiverClient(clientReceiver)
                .description(sendRequest.getDescription())
                .currency(Currency.valueOf(sendRequest.getCurrency()))
                .status(Status.ACTIVE)
                .amount(sendRequest.getAmount())
                .code(CodeGenerator.generate(10))
                .build());
    }

    @DisplayName("unit test for send transaction method")
    @Test
    void send() {
        Transaction transaction = transactionService.send(sendRequest);

        log.info(transaction.toString());
        assertThat(transaction).isNotNull();
    }

    @DisplayName("unit test for update transaction method")
    @Test
    void update() {

        Transaction transaction = transactionService.update(testTransaction.getId(), "COMPLETE");
        assertThat(transaction.getStatus()).isEqualTo(Status.COMPLETE);
    }

    @DisplayName("unit test for receive transaction method")
    @Test
    void receive() {
        Transaction transaction = transactionService.receive(testTransaction.getCode());
        assertThat(transaction.getStatus()).isEqualTo(Status.COMPLETE);
    }
}