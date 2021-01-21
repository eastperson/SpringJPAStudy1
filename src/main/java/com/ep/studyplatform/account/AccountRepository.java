package com.ep.studyplatform.account;

import com.ep.studyplatform.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


// 리퍼지토리는 항상 트랜잭셔널을 넣어줘야 한다.
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account,Long> {
    // exists 메서드는 Spring JPA가 자동으로 만들어준다.
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickname);
}
