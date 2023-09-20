package shop.mtcoding.bank.temp;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

// java.util.regex.Pattern
public class RegexTest {

    @Test
    public void 한글만가능_test() throws Exception {
        String value = "한글";
        boolean result = Pattern.matches("^[가-힣]+$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 한글만불가능_test() throws Exception {
        String value = "abc";
        boolean result = Pattern.matches("^[^ㄱ-ㅎ가-힣]*$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어만가능_test() throws Exception {
        String value = "ssar";
        boolean result = Pattern.matches("^[a-zA-Z]+$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어만불가능_test() throws Exception {
        String value = "abc";
        boolean result = Pattern.matches("^[^a-zA-Z]*$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어와숫자만가능_test() throws Exception {
        String value = "ssar3425";
        boolean result = Pattern.matches("^[a-zA-Z0-9]+$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어만가능_길이는최소2최대4이다_test() throws Exception {
        String value = "ssar";
        boolean result = Pattern.matches("^[a-zA-Z]{2,4}$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void user_username_test() throws Exception {
        String username = "sssa";
        boolean result = Pattern.matches("^[a-zA-Z0-9]{2,20}$", username);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void user_fullname_test() throws Exception {
        String fullname = "쌀jj";
        boolean result = Pattern.matches("^[a-zA-Z가-힣]{1,20}$", fullname);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void user_email_test() throws Exception {
        String email = "ssar@nate.com"; // ac.kr co.kr or.kr은 라이브러리 활용
        boolean result = Pattern.matches("^[a-zA-Z0-9]{2,10}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}$", email);
        System.out.println("테스트 : " + result);
    }
}
