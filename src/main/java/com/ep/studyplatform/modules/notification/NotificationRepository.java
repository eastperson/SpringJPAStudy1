package com.ep.studyplatform.modules.notification;

import com.ep.studyplatform.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification,Long>{
    long countByAccountAndChecked(Account account, boolean checked);

    // readonly가 아니다 변경을 해야하기 때문에
    @Transactional
    List<Notification> findByAccountAndCheckedOrderByCreatedDateTimeDesc(Account account, boolean checked);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean checked);
}
