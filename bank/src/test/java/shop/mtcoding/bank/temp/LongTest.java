package shop.mtcoding.bank.temp;

import org.junit.jupiter.api.Test;

public class LongTest {
    @Test
    public void long_test() throws Exception {
        // given
        Long number1 = 1111L;
        Long number2 = 1111L;
        Long amount1 = 1L;
        Long amount2 = 2L;
    
        // when
        if(number1 == number2) {
            System.out.println("테스트 : same");
        } else {
            System.out.println("테스트 : differ");
        }
        
        if(number1.longValue() == number2.longValue()) {
            System.out.println("테스트(longValue()) : same");
        } else {
            System.out.println("테스트(longValue()) : differ");
        }

        if(amount1 < amount2) {
            System.out.println("테스트 : amount1이 작습니다.");
        } else {
            System.out.println("테스트 : amount1이 작지 않습니다.");
        }

        if(number1 < number2) {
            System.out.println("테스트(longValue()) : amount1이 작습니다.");
        } else {
            System.out.println("테스트(longValue()) : amount1이 작지 않습니다.");
        }

        // then
    
    }
}
