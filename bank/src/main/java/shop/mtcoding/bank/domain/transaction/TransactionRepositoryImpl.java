package shop.mtcoding.bank.domain.transaction;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.data.repository.query.Param;

import lombok.RequiredArgsConstructor;

interface Dao {
    List<Transaction> findTransactionList(@Param("accountId") Long accountId, @Param("gubun") String gubun,
    @Param("Page") Integer page);
    
}

// naming convention: 앞에 기존 레파지토리 이름(TransactionRepository)과
// 어미에 Impl가 붙어야 함.
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements Dao {
    private final EntityManager em;

    // JPQL이 뭔지
    // join fetch
    // outer join
    @Override
    public List<Transaction> findTransactionList(Long accountId, String gubun, Integer page) {
        // gubun값으로 동적쿼리 작성(DEPOSIT, WITHDRAW, ALL)
        // JPQL
        String sql = "";
        sql += "select t from Transaction t ";

        if (gubun.equals("WITHDRAW")) {
            sql += "join fetch t.withdrawAccount wa ";
            sql += "where t.withdrawAccount.id = :withdrawAccountId";
        } else if (gubun.equals("DEPOSIT")) {
            sql += "join fetch t.depositAccount da ";
            sql += "where t.depositAccount.id = :depositAccountId";
        } else { // gubun = ALL
            sql += "left join t.withdrawAccount wa "; // 1,3,4,5
            sql += "left join t.depositAccount da "; // 3,4,5
            sql += "where t.withdrawAccount.id = :withdrawAccountId ";
            sql += "or ";
            sql += "t.depositAccount.id = :depositAccountId";
        }

        TypedQuery<Transaction> query = em.createQuery(sql, Transaction.class);

        if(gubun.equals("WITHDRAW")) {
            query = query.setParameter("withdrawAccountId", accountId);
        } else if(gubun.equals("DEPOSIT")) {
            query = query.setParameter("depositAccountId", accountId);
        } else {
            query = query.setParameter("withdrawAccountId", accountId);
            query = query.setParameter("depositAccountId", accountId);
        }

        query.setFirstResult(page * 5); // page == 0 -> 0 / 1 -> 5 / 2 -> 10
        query.setMaxResults(5);

        return query.getResultList();
    }
}
