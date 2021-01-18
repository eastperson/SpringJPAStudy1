package com.ep.studyplatform.account;

import com.ep.studyplatform.ConsoleMailSender;
import com.ep.studyplatform.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Controller
@Log4j2
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final  AccountService accountService;
    private final AccountRepository accountRepository;

    // 해당하는 객체를 파라미터로 받을 때 validator를 추가할 수 있다. 그러면 검증이 @Valid, Validator를 동시에 적용할 수 있다.
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUp(Model model){

        // 참고로 si
        model.addAttribute("signUpForm", new SignUpForm());

        // 클래스 이름이 Camel Case인 경우 attribute 이름을 생략할 수 있다. test로 확인 가능하다.
        //model.addAttribute(new SignUpForm());

        return "account/sign-up";
    }

    // 원래 @ModelAttribute를 사용한다. 복합객체로 받는 것을 위해서이다.
    // ModelAttribute는 생략이 가능하다.

    // 바인딩할 때 발생하는 에러를 Errors로 받는다.
    // validation되는 중간에 에러가 바인딩 되면 원래 페이지로 돌아간다.
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){

        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        // initBinder로 빼낼 수 있다.
//        signUpFormValidator.validate(signUpForm,errors);
//        if (errors.hasErrors()) {
//            return "account/sign-up";
//        }
        accountService.processNewAccount(signUpForm);

        // TODO 회원가입 처리
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public void checkEmailToken(String token, String email, Model model) {

        // 리퍼지토리를 로직으로 볼것이냐 도메인으로 볼것이냐는 논란이 많다.
        // 기선님은 리퍼지토리를 도메인으로 쓰되,
        //Account account - accountRepository

    }

}
