package shop.mtcoding.bank.domain.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>{

    // jpa query method
    // checkpoint: 리펙토링 필요
    Optional<Account> findByNumber(Long number);
    
}
