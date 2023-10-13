package shop.mtcoding.bank.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public void 입출금목록보기(Long userId, Long accountNumber) {
        
    }
}
