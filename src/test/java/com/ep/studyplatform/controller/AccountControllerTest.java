package com.ep.studyplatform.controller;

import com.ep.studyplatform.account.AccountRepository;
import com.ep.studyplatform.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 포트를 띄워서 할 수도 있다.
//@AutoConfigureWebClient 서블릿을 띄우면 해당 컨피규어를 사용할 수 있다.
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    // mocking을 한다.
    @MockBean
    private JavaMailSender javaMailSender;

    // 테스트 코드는 정해진 경우에는 효력이 강력하지만 유동적으로 기능이 바뀌는 경우에는 테스트 코드보다도 화면이 중요할 수 있다.
    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print()) // html 코드를 보여준다.
                .andExpect(status().isOk()) // 정상 처리가 되었는지
                .andExpect(view().name("account/sign-up")) // view name(파일, return 값)이 맞는지
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname","ep")
                .param("email","emaidsadsa")
                .param("password","12345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up")); // 요청이 거절되서 원래페이지로 돌아간다.
    }

    @DisplayName("회원가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_correct_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname","epepep")
                .param("email","email@dsds.com")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));


        Account account = accountRepository.findByEmail("email@dsds.com");

        assertNotNull(account);
        assertNotEquals(account.getPassword(),12345678);

        // Mockito 기능 활용. 호출이 되었는지.
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}