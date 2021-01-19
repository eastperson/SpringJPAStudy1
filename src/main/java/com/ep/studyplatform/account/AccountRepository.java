package com.ep.studyplatform.account;

import com.ep.studyplatform.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account,Long> {
    // exists 메서드는 Spring JPA가 자동으로 만들어준다.
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickname);
}
