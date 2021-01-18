package com.ep.studyplatform.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@EqualsAndHashCode(of = "id") // 연관관계가 복잡해질때, 서로다른 관계를 순환참조하느라 무한루프, stackoverflow가 발생할 수 있다.
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    // 이메일 인증 절차시 검증받은 아이디인지를 확인
    private boolean emailVerified;

    // 검증시 필요한 토큰 필드
    private String emailCheckToken;

    // 인증을 거친 사용자들은 가입날짜를 기록
    private LocalDateTime joinedAt;

    // 프로필과 관련된 정보
    private String bio;

    // 개인 URL
    private String url;

    // 직업
    private String occupation;

    // 거주지
    private String location;


    // @Lob은 텍스트 타입에 매칭
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    // 알림 설정
    // 스터디가 만들어졌다는 알림을 이메일로 받을 것인지, 웹으로 만들것인지
    // 가입 신청 결과를 이메일로 받을 것인지, 웹으로 받을 것인지

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

    public void generateEmailCheckToken(){
        this.emailCheckToken = UUID.randomUUID().toString();
    }
}
