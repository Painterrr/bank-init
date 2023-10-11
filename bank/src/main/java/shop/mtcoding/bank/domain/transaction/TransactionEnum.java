package shop.mtcoding.bank.domain.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionEnum {
    WITHDRAW("!-Withdraw-!"), DEPOSIT("!-Deposit-!"), TRANSFER("!-Transfer-!"), ALL("!-Deposit/withdrawal details-!");

    private String value;
}
