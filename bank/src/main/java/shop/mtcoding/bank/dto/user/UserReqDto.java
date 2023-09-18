package shop.mtcoding.bank.dto.user;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class UserReqDto {
    @Getter
    @Setter
    // validation check
    public static class JoinReqDto {
        // 영문, 숫자만 가능. 길이 2~20자
        @Pattern(regexp = "", message = "영문/숫자 2~20자 이내로 작성하세요.")
        @NotEmpty // null or emptyspace
        private String username;
        // 길이 4~20
        @NotEmpty
        private String password;
        // 이메일 형식
        @NotEmpty
        private String email;
        // 영어, 한글 가능, 길이 1~20자
        @NotEmpty
        private String fullname;

        public User toEntity(BCryptPasswordEncoder passwordEncoder) {
            return User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .email(email)
            .fullname(fullname)
            .role(UserEnum.CUSTOMER)
            .build();
        }
    }
}
