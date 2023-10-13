package shop.mtcoding.bank.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionEnum;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountTransferReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountWithdrawReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountDepositRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountDetailRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountTransferRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountWithdrawRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@RequiredArgsConstructor
@Transactional
@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountListRespDto 계좌목록보기_유저별(Long userId) {
        User userPS = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("user를 찾을 수 없습니다."));

        // 해당 유저의 모든 계좌 목록
        List<Account> accountListPS = accountRepository.findByUser_id(userId);
        return new AccountListRespDto(userPS, accountListPS);
    }

    @Transactional
    public AccountSaveRespDto 계좌등록(AccountSaveReqDto accountSaveReqDto, Long userId) {
        // userId로 User가 DB에 등록된 상태인지 검증
        User userPS = userRepository.findById(userId).orElseThrow(
            () -> new CustomApiException("user를 찾을 수 없습니다. 회원가입을 먼저 진행하세요."));

        // 해당 계좌(userId와 일치하는)가 DB에 있는지 중복여부 체크
        Optional<Account> accountOP = accountRepository.findByNumber(accountSaveReqDto.getNumber());
        if(accountOP.isPresent()) {
            throw new CustomApiException("해당 계좌번호가 이미 존재합니다.");
        }

        // 계좌 등록
        Account accountPS = accountRepository.save(accountSaveReqDto.toEntity(userPS));

        // DTO 응답
        return new AccountSaveRespDto(accountPS);
    }

    @Transactional
    public void 계좌삭제(Long number, Long userId) {
        // 계좌 유무 확인
        System.out.println("테스트 : number: " + number);
        System.out.println("테스트 : userId: " + userId);

        Account accountPS = accountRepository.findByNumber(number).orElseThrow(
                () -> new CustomApiException("계좌를 찾을 수 없습니다"));

        System.out.println("테스트 : 이하 계좌 정보");
        System.out.println("테스트 : id: " + accountPS.getId());
        System.out.println("테스트 : number: " + accountPS.getNumber());
        System.out.println("테스트 : userId: " + accountPS.getUser().getId());
        
        // 계좌 소유자 일치 확인
        accountPS.checkOwner(userId);

        // 계좌 삭제
        accountRepository.deleteById(accountPS.getId());
    }

    // 인증 필요 없음
    // 비즈니스 로직 순서 중요
    @Transactional
    public AccountDepositRespDto 계좌입금(AccountDepositReqDto accountDepositReqDto) {
        // 0원 체크
        if (accountDepositReqDto.getAmount() <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }
        
        // 입금 계좌 유무 확인
        Account depositAccountPS = accountRepository.findByNumber(accountDepositReqDto.getNumber())
                .orElseThrow(
                    () -> new CustomApiException("입금할 계좌를 찾을 수 없습니다."));

        // 입금 (=해당 계좌 balance 조정. update 쿼리 - 더티 체킹)
        depositAccountPS.deposit(accountDepositReqDto.getAmount());
        
        // 거래 내역 기록
        Transaction transaction = Transaction.builder()
                .withdrawAccount(null)
                .depositAccount(depositAccountPS)
                .withdrawAccountBalance(null)
                .depositAccountBalance(depositAccountPS.getBalance())
                .amount(accountDepositReqDto.getAmount())
                .gubun(TransactionEnum.DEPOSIT)
                .sender("ATM")
                .receiver(accountDepositReqDto.getNumber() + "")
                .tel(accountDepositReqDto.getTel())
                .build();
                
        Transaction transactionPS = transactionRepository.save(transaction);
        return new AccountDepositRespDto(depositAccountPS, transactionPS);
    }

    @Transactional
    public AccountWithdrawRespDto 계좌출금(AccountWithdrawReqDto accountWithdrawReqDto, Long userId) {
        // 0원 체크
        if (accountWithdrawReqDto.getAmount() <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }

        // 출금 계좌 유무 확인
        Account withdrawAccountPS = accountRepository.findByNumber(accountWithdrawReqDto.getNumber())
                .orElseThrow(
                    () -> new CustomApiException("출금할 계좌를 찾을 수 없습니다."));

        // 출금 소유자 확인(로그인 유저와 출금 계좌 소유자 동일인 확인)
        withdrawAccountPS.checkOwner(userId);

        // 출금 계좌 비밀번호 확인
        withdrawAccountPS.checkSamePassword(accountWithdrawReqDto.getPassword());

        // 출금 계좌 잔액 확인(계좌 잔액 >= 출금 금액)
        withdrawAccountPS.checkBalance(accountWithdrawReqDto.getAmount());

        // 출금 확인
        withdrawAccountPS.withdraw(accountWithdrawReqDto.getAmount());

        // 거래내역 기록(내 계좌에서 ATM으로 출금)
        Transaction transaction = Transaction.builder()
                .withdrawAccount(withdrawAccountPS)
                .depositAccount(null)
                .withdrawAccountBalance(withdrawAccountPS.getBalance())
                .depositAccountBalance(null)
                .amount(accountWithdrawReqDto.getAmount())
                .gubun(TransactionEnum.WITHDRAW)
                .sender(accountWithdrawReqDto.getNumber() + "")
                .receiver("ATM")
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);

        // DTO 응답
        return new AccountWithdrawRespDto(withdrawAccountPS, transactionPS);
    }

    @Transactional
    public AccountTransferRespDto 계좌이체(AccountTransferReqDto accountTransferReqDto, Long userId) {
        // 출금 계좌와 입금 계좌가 동일하면 안됨.
        if(accountTransferReqDto.getWithdrawNumber().longValue() == accountTransferReqDto.getDepositNumber().longValue()) {
            throw new CustomApiException("입출금 계좌가 동일할 수 없습니다.");
        }


        // 0원 체크
        if (accountTransferReqDto.getAmount() <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }

        // 출금 계좌 유무 확인
        Account withdrawAccountPS = accountRepository.findByNumber(accountTransferReqDto.getWithdrawNumber())
                .orElseThrow(
                    () -> new CustomApiException("출금계좌를 찾을 수 없습니다."));

        // 입금 계좌 유무 확인
        Account depositAccountPS = accountRepository.findByNumber(accountTransferReqDto.getDepositNumber())
                .orElseThrow(
                    () -> new CustomApiException("입금계좌를 찾을 수 없습니다."));

        // 출금 소유자 확인(로그인 유저와 출금 계좌 소유자 동일인 확인)
        withdrawAccountPS.checkOwner(userId);

        // 출금 계좌 비밀번호 확인
        withdrawAccountPS.checkSamePassword(accountTransferReqDto.getWithdrawPassword());

        // 출금 계좌 잔액 확인(계좌 잔액 >= 출금 금액)
        withdrawAccountPS.checkBalance(accountTransferReqDto.getAmount());

        // 이체 확인
        withdrawAccountPS.withdraw(accountTransferReqDto.getAmount());
        depositAccountPS.deposit(accountTransferReqDto.getAmount());

        // 거래내역 기록(내 계좌에서 ATM으로 출금)
        Transaction transaction = Transaction.builder()
                .withdrawAccount(withdrawAccountPS)
                .depositAccount(depositAccountPS)
                .withdrawAccountBalance(withdrawAccountPS.getBalance())
                .depositAccountBalance(depositAccountPS.getBalance())
                .amount(accountTransferReqDto.getAmount())
                .gubun(TransactionEnum.TRANSFER)
                .sender(accountTransferReqDto.getWithdrawNumber() + "")
                .receiver(accountTransferReqDto.getDepositNumber() + "")
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);

        // DTO 응답
        return new AccountTransferRespDto(withdrawAccountPS, transactionPS);
    }

    public AccountDetailRespDto 계좌상세보기(Long number, Long userId, Integer page) {
        // 1, 구분값, 페이지 고정
        String gubun = "ALL";
        
        Account accountPS = accountRepository.findByNumber(number).orElseThrow(
            () -> new CustomApiException("계좌를 찾을 수 없습니다."));

        accountPS.checkOwner(userId);

        List<Transaction> transactionList = transactionRepository.findTransactionList(accountPS.getId(), gubun, page);
        return new AccountDetailRespDto(accountPS, transactionList);
    }
}
