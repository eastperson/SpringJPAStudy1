package com.ep.studyplatform.modules.event;

import com.ep.studyplatform.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id
    @GeneratedValue
    private Long id;

    // 1대 N에서 N이 되는 쪽이 foreign key로 참조하는 것이 가장 일반적인 참조관계이다.
    // foreign key는 enrollment에서 생긴다. 조인 테이블을 생기지 않는다.
    @ManyToOne
    private Event event;


    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

}
