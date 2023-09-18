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
        
    }
    
    @Test
    public void 영어만가능_test() throws Exception {
        
    }

    @Test
    public void 영어만불가능_test() throws Exception {
        
    }

    @Test
    public void 영어와숫자만불가능_test() throws Exception {
        
    }

    @Test
    public void 영어만가능_길이는최소2최대4이다_test() throws Exception {
        
    }

    // username, email, fullname test
}
