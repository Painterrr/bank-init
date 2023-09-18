package shop.mtcoding.bank.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.user.UserReqDto.JoinReqDto;
import shop.mtcoding.bank.dto.user.UserRespDto.JoinRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@RequiredArgsConstructor
@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 서비스는 DTO를 요청받고 DTO로 응답한다.
    @Transactional 
    public JoinRespDto 회원가입(JoinReqDto joinReqDto) {
        // 1. 동일 유저 존재 검사
        Optional<User> userOP = userRepository.findByUsername(joinReqDto.getUsername());
        if(userOP.isPresent()) {
            throw new CustomApiException("동일한 username이 존재합니다.");
        }

        // 2. 패스워드 인코딩 + 회원가입
        User userPS = userRepository.save(joinReqDto.toEntity(passwordEncoder));

        // 3. DTO 응답
        return new JoinRespDto(userPS);
    }
   
}
