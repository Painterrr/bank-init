package shop.mtcoding.bank.domain.account;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>{

    // jpa query method.
    // select * from account where number = :number
    // checkpoint: 리펙토링 필요. (계좌 소유자 확인 시, 쿼리가 2번 생성되기 때문에 join fetch 필요)
    // = account.getUser().getId()
    // join fetch를 하면 조인해서 객체에 값을 미리 가져올 수 있다.
    // @Query("SELECT ac FROM Account ac JOIN FETCH ac.user u WHERE ac.number = :number")
    // 위와 같이 쿼리를 설정하면 Lazy로 설정한 것을 미리 조인하여 가져올 수 있음.
    Optional<Account> findByNumber(Long number);
    
    // jpa query method.
    // select * from account where user_id = :id
    List<Account> findByUser_id(Long id);

    
}
