package com.example.moneytransfer.service.impl;

import com.example.moneytransfer.Enums.Currency;
import com.example.moneytransfer.Enums.Role;
import com.example.moneytransfer.Enums.Status;
import com.example.moneytransfer.entity.Client;
import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import com.example.moneytransfer.paging.Paged;
import com.example.moneytransfer.paging.Paging;
import com.example.moneytransfer.repository.ClientRepository;
import com.example.moneytransfer.repository.TransactionRepository;
import com.example.moneytransfer.repository.UserRepository;
import com.example.moneytransfer.request.RefreshTransactionRequest;
import com.example.moneytransfer.request.SendTransactionRequest;
import com.example.moneytransfer.service.TransactionService;
import com.example.moneytransfer.utils.CodeGenerator;
import com.example.moneytransfer.utils.DateConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
/*@EnableScheduling*/
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  ClientRepository clientRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public Transaction send(SendTransactionRequest request) {
        if (request.getUsernameReceiver().equals(request.getUsernameSender())) {
            throw new RuntimeException("Не допустимая транзакция");
        }

        User sender = userRepository.findByUsername(request.getUserSender()).orElseThrow();

        Client clientSender = Client.builder()
                .name(request.getUsernameSender())
                .phoneNumber(request.getPhoneNumberSender())
                .build();

        Client clientReceiver = Client.builder()
                .name(request.getUsernameReceiver())
                .phoneNumber(request.getPhoneNumberReceiver())
                .build();

        clientRepository.saveAll(List.of(clientSender, clientReceiver));

        Transaction transaction = Transaction.builder()
                .userSender(sender)
                .senderClient(clientSender)
                .receiverClient(clientReceiver)
                .description(request.getDescription())
                .currency(Currency.valueOf(request.getCurrency()))
                .status(Status.ACTIVE)
                .amount(request.getAmount())
                .code(CodeGenerator.generate(10))
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction update(Long id, String newStatus) {
        return transactionRepository.findById(id)
                .map(transaction1 -> {
                    transaction1.setStatus(Status.valueOf(newStatus));
                    transactionRepository.save(transaction1);
                    return transaction1;
                }).orElseThrow(() -> new RuntimeException("transaction not found exception"));
    }

    @Override
    public Paged<Transaction> getAll(int pageNum, int pageSize, String sortField, String sortDir) {
        PageRequest request = PageRequest.of(pageNum - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending());
        Page<Transaction> postPage = transactionRepository.findAll(request);

        return new Paged<>(postPage, Paging.of(postPage.getTotalPages(), pageNum, pageSize));
    }

    @Override
    public Paged<Transaction> getAllByDate(int pageNum, int pageSize, String sortField, String sortDir, Date date1, Date date2) {
        PageRequest request = PageRequest.of(pageNum - 1, pageSize, sortDir.equals("asc") ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending());
        Page<Transaction> postPage = transactionRepository.findAllByDateCreatedBetween(date1, date2, request);
        return new Paged<>(postPage, Paging.of(postPage.getTotalPages(), pageNum, pageSize));
    }

    @Override
    public Paged<Transaction> getByCode(String code, int pageNum, int pageSize) {

        PageRequest request = PageRequest.of(pageNum - 1, pageSize, Sort.by("dateCreated").descending());
        Page<Transaction> postPage = transactionRepository.findByCode(code, request);
        return new Paged<>(postPage, Paging.of(postPage.getTotalPages(), pageNum, pageSize));}

    @Override
    public Transaction receive(String code) {
        return transactionRepository.findByCode(code)
                .map(transaction1 -> {
                    transaction1.setStatus(Status.COMPLETE);
                    return transactionRepository.save(transaction1);
                })
                .orElseThrow(() -> new RuntimeException("Такой транзакции не существует"));
    }

    @Override
    public Transaction refresh(RefreshTransactionRequest request) {
        return transactionRepository.findByCode(request.getOldCode())
                .map(transaction1 -> {
                    transaction1.setStatus(Status.ACTIVE);
                    transaction1.setDateCreated(DateConverter.convertToDateViaInstant(LocalDateTime.now()));
                    transaction1.setCode(CodeGenerator.generate(10));
                    return transactionRepository.save(transaction1);
                })
                .orElseThrow(() -> new RuntimeException("Такой транзакции не существует"));
    }

    @Override
    public Map<String, Long> calcTotalAmount(List<Transaction> transactions) {
        Map<String, Long> currencyAmount = new HashMap<>();
        long totalUsd = 0L;
        long totalKgz = 0L;
        long totalYtl = 0L;
        long totalRub = 0L;
        for (Transaction transaction : transactions) {
            if (transaction.getCurrency().equals(Currency.USD)) {
                totalUsd += transaction.getAmount().longValue();
            }
            if (transaction.getCurrency().equals(Currency.RUB)) {
                totalRub += transaction.getAmount().longValue();
            }
            if (transaction.getCurrency().equals(Currency.YTL)) {
                totalYtl += transaction.getAmount().longValue();
            }
            if (transaction.getCurrency().equals(Currency.KGS)) {
                totalKgz += transaction.getAmount().longValue();
            }

        }
        currencyAmount.put(Currency.USD.toString(), totalUsd);
        currencyAmount.put(Currency.KGS.toString(), totalKgz);
        currencyAmount.put(Currency.YTL.toString(), totalYtl);
        currencyAmount.put(Currency.RUB.toString(), totalRub);
        return currencyAmount;
    }

    /*@PostConstruct
    public void persist() {
        for (int i = 0; i < 10; i++) {
            transactionRepository.save(Transaction.builder()
                            .userSender(userRepository.findByUsername("eldar")
                                    .orElseThrow(() -> new RuntimeException()))
                            .userReceiver(userRepository.findByUsername("dastan")
                                    .orElseThrow(() -> new RuntimeException()))
                            .senderClientNumber("0999343434")
                            .receiverClientNumber("0999565656")
                            .code(CodeGenerator.generate(10))
                            .status(Status.ACTIVE)
                            .description("зарплата")
                            .amount(BigDecimal.valueOf(1000))
                            .currency(Currency.USD)
                            .dateCreated(DateConverter.convertToDateViaInstant(LocalDateTime.now().minusWeeks(2)))
                    .build());
        }
    }*/

    @Scheduled(cron = "0 * * * * *")
    public void checkExpiration() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        for (Transaction transaction : allTransactions) {
            if (transaction.getDateCreated().before(DateConverter.convertToDateViaInstant(LocalDateTime.now().minusMinutes(10)))
                    && transaction.getStatus().equals(Status.valueOf("ACTIVE"))) {
                transaction.setStatus(Status.OVERDUE);
                transactionRepository.save(transaction);
            }
        }
    }
}
