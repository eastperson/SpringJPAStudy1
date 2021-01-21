package com.ep.studyplatform.account;

import com.ep.studyplatform.domain.Account;
import com.ep.studyplatform.domain.Tag;
import com.ep.studyplatform.settings.form.Notifications;
import com.ep.studyplatform.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
// 로그인 인증을 위해 UserDetailsService를 구현한다.
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    //private final AuthenticationManager authenticationManager; 따로 설정하는데 해당 수업에서는 필요가 없으니깐 빼준다.
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("동인 스터디, 회원 가입 인증"); // 제목
        mailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken()
                +"&email="+ newAccount.getEmail());

        // 메시지 보내기
        javaMailSender.send(mailMessage);
    }

    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        // 작성한 코드가 지저분하면 메서드로 빼서 만들자. 디버깅에 유리하다.
        Account newAccount = saveNewAccount(signUpForm);

        // 트랙잭션이 없어서 DB에 싱크가 되지 않았다.
        newAccount.generateEmailCheckToken();
        // 메일 센더를 주입받아서 사용
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    }

    public void login(Account account) {

        // 아이디와 패스워드, 권한을 부여한 토큰을 가져온다.
        // 사실 Authentication Manager가 사용하는 메서드이다.
        // 인코딩된 패스워드를 사용하기 때문에 변형된 방법으로 로그인을 하는 것이다. 정석방법으로는 실제 password로 접근을 해야하기 떄문이다.
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(token);

        // 정석적인 처리
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                username,password
//        );
//        Authentication authentication = authenticationManager.authenticate(token);

        // Context안에 Authentication을 세팅해줄 수 있다.
//        SecurityContext context = SecurityContextHolder.getContext();
//        context.setAuthentication(token);
    }


    // 데이터를 변경하는 것이 아니라 읽기위한 메서드는 readonly를 써준다. write lock을 안써줄 수 있다.
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }
        if(account == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        // Principal에 해당하는 객체를 넘기면 된다 User를 상속한 UserAccount를 돌려주자.
        return new UserAccount(account);
    }

    @Transactional
    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {



        // Account는 persisntent 상태가 아니다. principal 안에 있는 객체이다. 이미 트랜잭션이 끝나있다. detached 상태의 객체이다.
        // 어떻게 DB와 싱크를 맞출 수 있을까
//        account.setBio(profile.getBio());
//        account.setOccupation(profile.getOccupation());
//        account.setLocation(profile.getLocation());
//        account.setUrl(profile.getUrl());
//        account.setProfileImage(profile.getProfileImage());

        // 모든 프로퍼티가 맵핑이 된다는 가정(변수 이름이 일치) 맵핑 가능
        modelMapper.map(profile,account);

        // save를 해주면 된다.
        // JPA를 사용할 때는 현재 사용하고 있는 상태가 어떤건지 알고 있어야한다.
        accountRepository.save(account);

    }

    public void updatePassword(Account account, String newPassword) {
        // detached 상태
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {



//        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
//        account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
//        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
//        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
//        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
//        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());

        modelMapper.map(notifications,account);

        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {

        // detached 객체
        account.setNickname(nickname);
        accountRepository.save(account);
        // authentication에서 관리하는 nickname이 업데이트 되지 않는다. 따라서 로그인해서 바꾸자.
        login(account);
    }

    public void sendLoginLink(Account account) {

        account.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("동인 스터디, 로그인 링크"); // 제목
        mailMessage.setText("/login-by-email?token="+ account.getEmailCheckToken()
                +"&email="+ account.getEmail());

        // 메시지 보내기
        javaMailSender.send(mailMessage);
    }

//    public void addTag(Account account, Tag tag) {
//        // account detached 객체는 lazy loading이 가능하다.
//        // get과 findById가 가능하다.
//        Optional<Account> byId = accountRepository.findById(account.getId());
//        // 만약 있으면 tag를 추가
//        byId.ifPresent(a -> a.getTags().add(tag));
//
//    }
//
//    public Set<Tag> getTags(Account account) {
//        Optional<Account> byId = accountRepository.findById(account.getId());
//        return byId.orElseThrow().getTags();
//    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }
}
