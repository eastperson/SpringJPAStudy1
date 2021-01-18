package com.ep.studyplatform.account;

import com.ep.studyplatform.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    // private final의 생성자를 만들어준다. 선별적으로 자동 주입해줄 수 있는 좋은 방법이다.
    // @Autowired없이도 빈을 주입 받을 수 있따.
    private final AccountRepository accountRepository;

    // @RequiredArgsConstructor를 사용하면 아래와 같이 생성자가 만들어진다고 보면된다.
//    public SignUpFormValidator(AccountRepository accountRepository) {
//        this.accountRepository = accountRepository;
//    }

    // 어떤 타입의 인스턴스를 검증할 것인지
    // SignUpForm 클래스를 검사
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(SignUpForm.class);
    }

    // email, nickname 중복 검사를 해야한다. repository 필요
    @Override
    public void validate(Object target, Errors errors) {
        // TODO email, nickname
        SignUpForm signUpForm = (SignUpForm) target;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email","invalid.email",new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname","invalid.email", new Object[]{signUpForm.getEmail()},"이미 사용중인 닉네임입니다.");
        }
    }
}
