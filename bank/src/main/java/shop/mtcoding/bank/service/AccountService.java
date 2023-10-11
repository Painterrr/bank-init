package shop.mtcoding.bank.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionEnum;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;
import shop.mtcoding.bank.util.CustomDateUtil;

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
        System.out.println("테스트 : user: " + accountPS.getUser());
        
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

    @Setter
    @Getter
    public static class AccountDepositRespDto {
        private Long id; // 계좌 ID
        private Long number; // 계좌번호
        // transaction log. transaction으로 하면 절대 안됨.(controller에 Entity 노출됨 -> 양방향 매핑에 걸리면 순환 참조 걸림)
        private TransactionDto transaction;

        public AccountDepositRespDto(Account account, Transaction transaction) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.transaction = new TransactionDto(transaction);
        }

        @Setter
        @Getter
        public class TransactionDto {
            private Long id;
            private String gubun;
            private String sender;
            private String reciever;
            private Long amount;
            private String tel;
            private String createdAt;
            @JsonIgnore
            private Long depositAccountBalance; // 클라이언트에게 전달 X, 서비스단에서의 테스트용

            public TransactionDto(Transaction transaction) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun().getValue();
                this.sender = transaction.getSender();
                this.reciever = transaction.getReceiver();
                this.amount = transaction.getAmount();
                this.tel = transaction.getTel();
                this.createdAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
                this.depositAccountBalance = transaction.getDepositAccountBalance();
            }
        }
    }

    @Setter
    @Getter
    public static class AccountDepositReqDto {
        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long number;
        @NotNull
        private Long amount;
        @NotEmpty
        @Pattern(regexp = "DEPOSIT")
        private String gubun; // DEPOSIT
        @NotEmpty
        @Pattern(regexp = "^[0-9]{11}")
        private String tel; // 입금이 잘못 되었을 때 송신자 확인

    }

}
