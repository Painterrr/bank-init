package shop.mtcoding.bank.domain.account;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>{

    // jpa query method.
    // checkpoint: 리펙토링 필요. (계좌 소유자 확인 시, 쿼리가 2번 생성되기 때문에 join fetch 필요)
    Optional<Account> findByNumber(Long number);
    
    // jpa query method.
    // select * from account where user_id = :id
    List<Account> findByUser_id(Long id);

    
}
