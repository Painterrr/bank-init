package shop.mtcoding.bank.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@RequiredArgsConstructor
@Transactional
@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

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
            throw new CustomApiException("해당 계좌가 이미 존재합니다.");
        }

        // 계좌 등록
        Account accountPS = accountRepository.save(accountSaveReqDto.toEntity(userPS));

        // DTO 응답
        return new AccountSaveRespDto(accountPS);
    }

    @Transactional
    public void 계좌삭제(Long number, Long userId) {
        // 계좌 유무 확인
        Account accountPS = accountRepository.findByNumber(number).orElseThrow(
            () -> new CustomApiException("계좌를 찾을 수 없습니다"));

        // 계좌 소유자 일치 확인
        accountPS.checkOwner(userId);

        // 계좌 삭제
        accountRepository.deleteById(accountPS.getId());
    }
}
