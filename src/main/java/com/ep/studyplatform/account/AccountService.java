package com.ep.studyplatform.account;

import com.ep.studyplatform.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    public Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword()) //TODO encoding 해야함
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

    public void processNewAccount(SignUpForm signUpForm) {
        // 작성한 코드가 지저분하면 메서드로 빼서 만들자. 디버깅에 유리하다.
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        // 메일 센더를 주입받아서 사용
        sendSignUpConfirmEmail(newAccount);

    }


    public Account checkToken(String token,String email) {

        Account account = accountRepository.findByEmail(email);

        if(account != null && token.equals(account.getEmailCheckToken()))
            return account;

        return null;
    }
}
