package com.ep.studyplatform.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    // Varchar255로 못담기 때문에 Lob을 쓴다.
    // Lazy를 사용해서 지연시키면 성능 향상이 있겠지만 기본적으로는 Eager로 해두자.
    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch =FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean published;

    private boolean closed;

    private boolean useBanner;


    public void addManager(Account account) {
        this.managers.add(account);
    }
}