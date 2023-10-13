package shop.mtcoding.bank.temp;

import static org.assertj.core.api.Assertions.assertThat;

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
        if (number1 == number2) {
            System.out.println("테스트 : same");
        } else {
            System.out.println("테스트 : differ");
        }

        if (number1.longValue() == number2.longValue()) {
            System.out.println("테스트(longValue()) : same");
        } else {
            System.out.println("테스트(longValue()) : differ");
        }

        if (amount1 < amount2) {
            System.out.println("테스트 : amount1이 작습니다.");
        } else {
            System.out.println("테스트 : amount1이 작지 않습니다.");
        }

        if (number1 < number2) {
            System.out.println("테스트(longValue()) : amount1이 작습니다.");
        } else {
            System.out.println("테스트(longValue()) : amount1이 작지 않습니다.");
        }

        // then

    }

    @Test
    public void long_test2() throws Exception {
        // given
        Long v1 = 1000L;
        Long v2 = 1000L;

        // when
        if (v1 == v2) {
            System.out.println("테스트 : true");
        } else {
            System.out.println("테스트 : false");
        }

        // long 값이 작으면(8bit: -126 ~ 127) ==으로 동일 판별 가능.
        // 크면(8bit 넘는 값) ==으로 불가. longValue() 해야 함.

        // then

    }

    @Test
    public void long_test3() throws Exception {
        // given
        Long v1 = 1000L;
        Long v2 = 1000L;
    
        // when
    
    
        // then
        assertThat(v1).isEqualTo(v2);
    }
}
