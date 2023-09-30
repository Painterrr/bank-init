package shop.mtcoding.bank.domain.account;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@NoArgsConstructor // 스프링이 User 객체 생성할 때 반 생성자로 new 하기 때문
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "account_tb")
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 4)
    private Long number; // 계좌번호
    @Column(nullable = false, length = 4)
    private Long password; // 계좌 비밀번호
    @Column(nullable = false)
    private Long balance; // 잔액(default = 1000)

    // ORM에서 fk의 주인은 Many Entity
    @ManyToOne(fetch = FetchType.LAZY) // account.getUser().아무필드호출() == LAZY 발동
    private User user; // user_id
    
    @CreatedDate // insert
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate // Insert, Update
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Account(Long id, Long number, Long password, Long balance, User user, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.number = number;
        this.password = password;
        this.balance = balance;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void checkOwner(Long userId) {
        if(user.getId() != userId) {
            throw new CustomApiException("계좌 소유자가 아닙니다.");
        }
    }
}
