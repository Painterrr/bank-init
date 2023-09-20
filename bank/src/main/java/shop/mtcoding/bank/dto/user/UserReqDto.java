package shop.mtcoding.bank.dto.user;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성하세요.")
        @NotEmpty // null or emptyspace
        private String username;

        // 길이 4~20
        @NotEmpty
        @Size(min = 4, max = 20)
        private String password;

        // 이메일 형식
        @Pattern(regexp = "^[a-zA-Z0-9]{2,10}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}$", message = "이메일 형식을 작성하세요.")
        @NotEmpty
        private String email;

        // 영어, 한글 가능, 길이 1~20자
        @Pattern(regexp = "^[a-zA-Z가-힣]{1,20}$", message = "한글/영문 1~20자 이내로 작성하세요.")
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
