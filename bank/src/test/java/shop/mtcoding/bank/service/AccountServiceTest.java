package shop.mtcoding.bank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountTransferReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountDepositRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest extends DummyObject {

    @InjectMocks // 모든 Mock들이 InjectMocks로 주입됨
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Spy // 실제 객체를 InjectMocks에 주입
    private ObjectMapper om;

    @Test
    public void 계좌등록_test() throws Exception {
        // given
        Long userId = 1L;

        AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto();
        accountSaveReqDto.setNumber(1111L);
        accountSaveReqDto.setPassword(1234L);

        // stub 1L
        User ssar = newMockUser(userId, "ssar", "쌀");
        when(userRepository.findById(any())).thenReturn(Optional.of(ssar));

        // stub 2
        when(accountRepository.findByNumber(any())).thenReturn(Optional.empty());

        // stub 3
        Account ssarAccount = newMockAccount(1L, 1111L, 1000L, ssar);
        when(accountRepository.save(any())).thenReturn(ssarAccount);

        // when
        AccountSaveRespDto accountSaveRespDto = accountService.계좌등록(accountSaveReqDto, userId);
        // 계좌등록(accountSaveReqDto, userId);
        String responseBody = om.writeValueAsString(accountSaveRespDto);
        System.out.println("테스트 : " + responseBody);

        // then
        assertThat(accountSaveRespDto.getNumber()).isEqualTo(1111L);
    }

    @Test
    public void 계좌목록보기_유저별_test() throws Exception {
        // given
        Long userId = 1L;

        // stub
        User ssar = newMockUser(1L, "ssar", "쌀");
        when(userRepository.findById(userId)).thenReturn(Optional.of(ssar));

        Account ssarAccount1 = newMockAccount(1L, 1111L, 1000L, ssar);
        Account ssarAccount2 = newMockAccount(2L, 2222L, 1000L, ssar);
        List<Account> accountList = Arrays.asList(ssarAccount1, ssarAccount2);
        when(accountRepository.findByUser_id(any())).thenReturn(accountList);

        // when
        AccountListRespDto accountListRespDto = accountService.계좌목록보기_유저별(userId);

        // then
        assertThat(accountListRespDto.getFullname()).isEqualTo("쌀"); // 소유자가 user와 동일한 이름인지
        assertThat(accountListRespDto.getAccounts().size()).isEqualTo(2); // stub에 설정한 2개의 계좌와 사이즈가 같은지
        System.out.println("테스트 : 보유 계좌 수: " + accountListRespDto.getAccounts().size());
    }

    // 계좌 유무 확인, 계좌 삭제의 역할은 레파지토리의 영역.
    // 서비스 단에서의 계좌삭제 테스트는 로그인 유저와 소유자 일치 확인만.
    @Test
    public void 계좌삭제_test() throws Exception {
        // given: 다른 user가 계좌삭제를 시도하면 Throws로 처리.
        // 동일 user가 시도하면 예외 발생.
        Long number = 1111L;
        Long userId = 2L;

        // stub
        User ssar = newMockUser(1L, "ssar", "쌀");
        Account ssarAccount = newMockAccount(1L, 1111L, 1000L, ssar);
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(ssarAccount));

        // when
        assertThrows(CustomApiException.class, () -> accountService.계좌삭제(number, userId));
    }

    // 실제 account의 balance가 변경되는지
    // trasaction log의 balance가 제대로 기록되는지
    @Test
    public void 계좌입금_test() throws Exception {
        // given
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01033337777");

        // stub1
        User ssar = newMockUser(1L, "ssar", "쌀"); // 실행됨
        // 실행됨 (ssarAccount1 현 잔액 1000원)
        Account ssarAccount1 = newMockAccount(1L, 1111L, 1000L, ssar);
        // service 호출 후 입금계좌 확인이 호출되면 입금메서드가 호출되면서 실행됨(이 경우 ssarAccount1 현 잔액 1100)
        // 다음 단계가 transaction에 account를 저장하는 것이니 잔액은 1200원
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(ssarAccount1)); // 실행 안됨

        // stub2
        // 실행됨 (ssarAccount1 현 잔액 1100원 / transation 현 잔액 1100원)
        // 해서 stub이 진행될 때마다 연관된 객체는 다시 사용하지 않고 새로 만들어서 사용(서로 영향을 줄 수 있기 때문)
        Account ssarAccount2 = newMockAccount(1L, 1111L, 1000L, ssar);
        Transaction transaction = newMockDepositTransaction(1L, ssarAccount2);
        when(transactionRepository.save(any())).thenReturn(transaction); // 실행 안됨

        // when
        AccountDepositRespDto accountDepositRespDto = accountService.계좌입금(accountDepositReqDto);
        System.out.println("테스트 : 트랜젝션 입금계좌 잔액: " + accountDepositRespDto.getTransaction().getDepositAccountBalance());
        System.out.println("테스트 : account1 잔액: " + ssarAccount1.getBalance());
        System.out.println("테스트 : account2 잔액: " + ssarAccount2.getBalance());

        // then
        assertThat(ssarAccount1.getBalance()).isEqualTo(1100L);
        assertThat(accountDepositRespDto.getTransaction().getDepositAccountBalance()).isEqualTo(1100L);
    }

    @Test
    public void 계좌입금_test2() throws Exception {
        // given
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01088887777");

        // stub 1L
        User ssar = newMockUser(1L, "ssar", "쌀"); // 실행됨
        Account ssarAccount1 = newMockAccount(1L, 1111L, 1000L, ssar); // 실행됨 - ssarAccount1 -> 1000원
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(ssarAccount1));

        // stub 2
        User ssar2 = newMockUser(1L, "ssar", "쌀"); // 실행됨
        Account ssarAccount2 = newMockAccount(1L, 1111L, 1000L, ssar2); // 실행됨 - ssarAccount1 -> 1000원
        Transaction transaction = newMockDepositTransaction(1L, ssarAccount2);
        when(transactionRepository.save(any())).thenReturn(transaction); // 실행안됨

        // when
        AccountDepositRespDto accountDepositRespDto = accountService.계좌입금(accountDepositReqDto);
        String responseBody = om.writeValueAsString(accountDepositRespDto);
        System.out.println("테스트 : " + responseBody);

        // then
        assertThat(ssarAccount1.getBalance()).isEqualTo(1100L);
    }

    // 서비스 테스트를 강의한 것은 기술적인 테크닉뿐
    // 진짜 서비스를 테스트하고 싶으면, 내가 지금 무엇을 여기서 테스트할지 명확히 구분해야 함.(책임 분리)
    // DTO를 만드는 책임 -> 서비스에 있지만 서비스에서 DTO를 검증하지 않고 controller 테스트할 것.
    // DB 관련된 것도 -> 서비스 책임이 아님.
    // (DB 관련된 것을 조회했을 때, 그 값을 통해서 어떤 비즈니스 로직이 흘러가는 것이 있으면 stub으로 정의해서 테스트 해야 함.)
    // 하지만 지금처럼 DB stub을 만들면서 deposit, 0원 검증 등의 테스트를 할 필요가 있을까 고민해야 함.
    @Test
    public void 계좌입금_test3() throws Exception {
        // given
        Account account = newMockAccount(1L, 1111L, 1000L, null);
        Long amount = 100L;

        // when
        if (amount <= 0) {
            throw new CustomApiException("0원 이하의 금액은 입금할 수 없습니다.");
        }
        account.deposit(100L);

        // then
        assertThat(account.getBalance()).isEqualTo(1100L);
    }

    // 계좌 출금_테스트
    @Test
    public void 계좌출금_test() throws Exception {
        // given
        Long amount = 100L;
        Long password = 1234L;
        Long userId = 1L;

        User ssar = newMockUser(1L, "ssar", "쌀");
        Account ssarAccount = newMockAccount(1L, 1111L, 1000L, ssar);

        // when
        if (amount <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }

        ssarAccount.checkOwner(userId);
        ssarAccount.checkSamePassword(password);
        ssarAccount.checkBalance(amount);
        ssarAccount.withdraw(amount);

        // then
        assertThat(ssarAccount.getBalance()).isEqualTo(900L);
    }

    // 완벽한 테스트는 존재할 수 없음.
    // 최대한 꼼꼼히 값을 테스트 해야 함.
    // 계좌 이체_테스트
    @Test
    public void 계좌이체_test() throws Exception {
        // given
        Long userId = 1L;
        AccountTransferReqDto accountTransferReqDto = new AccountTransferReqDto();
        accountTransferReqDto.setWithdrawNumber(1111L);
        accountTransferReqDto.setDepositNumber(2222L);
        accountTransferReqDto.setWithdrawPassword(1234L);
        accountTransferReqDto.setAmount(100L);
        accountTransferReqDto.setGubun("TRANSFER");

        User ssar = newMockUser(1L, "ssar", "쌀");
        User cos = newMockUser(2L, "cos", "코스");

        Account withdrawAccount = newMockAccount(1L, 1111L, 1000L, ssar);
        Account depositAccount = newMockAccount(2L, 2222L, 1000L, cos);
    
        // when
        if(accountTransferReqDto.getWithdrawNumber().longValue() == accountTransferReqDto.getDepositNumber().longValue()) {
            throw new CustomApiException("입출금계좌가 동일할 수 없습니다.");
        }

        if(accountTransferReqDto.getAmount() <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }
    
        // 출금 소유자 확인(로그인 유저와 출금 계좌 소유자 동일인 확인)
        withdrawAccount.checkOwner(userId);

        // 출금 계좌 비밀번호 확인
        withdrawAccount.checkSamePassword(accountTransferReqDto.getWithdrawPassword());

        // 출금 계좌 잔액 확인(계좌 잔액 >= 출금 금액)
        withdrawAccount.checkBalance(accountTransferReqDto.getAmount());

        // 이체 확인
        withdrawAccount.withdraw(accountTransferReqDto.getAmount());
        depositAccount.deposit(accountTransferReqDto.getAmount());
        
        // then
        assertThat(withdrawAccount.getBalance()).isEqualTo(900L);
        assertThat(depositAccount.getBalance()).isEqualTo(1100L);
    }

    // 계좌목록보기_유저별_테스트

    // 계좌상세보기_테스트
}
