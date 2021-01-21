package com.ep.studyplatform.account;

import com.ep.studyplatform.domain.Account;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;


@Controller
@Log4j2
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
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
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);

        // TODO 회원가입 처리
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {

        // 리퍼지토리를 로직으로 볼것이냐 도메인으로 볼것이냐는 논란이 많다.
        // 기선님은 리퍼지토리를 도메인으로 쓰되, 서비스나 컨트롤러를 repository, domain에서 참조하지 않는다.
        Account account = accountRepository.findByEmail(email);
        String view = "account/checkedEmail";
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
        if(!account.isValidToken(token)){
            model.addAttribute("error","wrong.token");
            return view;
        }

        // 아래 두 라인은 한 번에 이뤄져야 한다.
        //account.setEmailVerified(true);
        //account.setJoinedAt(LocalDateTime.now());

        // 트랜잭션을 넣어줘야 한다.
        // 트랜잭션 관리는 service에 위임해서 넘기자.

        accountService.completeSignUp(account);
//        account.completeSignUp();
//        accountService.login(account);
        model.addAttribute("numberOfUser",accountRepository.count());
        model.addAttribute("nickname",account.getNickname());
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model){

        if(account != null){
            model.addAttribute("email",account.getEmail());
        }

        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendEmail(@CurrentUser Account account,Model model){

        if(!account.canSendConfirmEmail()) {
            model.addAttribute("error","인증 이메일은 1시간에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email",account.getEmail());
            return "account/check-email";
        }
        accountService.sendSignUpConfirmEmail(account);

        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable("nickname") String nickname, Model model, @CurrentUser Account account){

        Account byNickname = accountRepository.findByNickname(nickname);
        if(byNickname == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        // 이름을 주지 않으면 기본 값으로 객체 타입의 Camel Cas로 들어간다. 따라서 account로 들어가게 된다.
        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));

        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm(){
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes){
        Account account = accountRepository.findByEmail(email);
        if(account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }
        if(!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            return "account/email-login";
        }
        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message","이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email,Model model){
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if(account == null || !account.isValidToken(token)) {
            model.addAttribute("error","로그인할 수 없습니다.");
            return view;
        }

        accountService.login(account);
        return view;
    }

}
