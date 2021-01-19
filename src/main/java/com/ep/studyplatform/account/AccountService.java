package com.ep.studyplatform.account;

import com.ep.studyplatform.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
// 로그인 인증을 위해 UserDetailsService를 구현한다.
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    //private final AuthenticationManager authenticationManager; 따로 설정하는데 해당 수업에서는 필요가 없으니깐 빼준다.
    private final PasswordEncoder passwordEncoder;

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
}
