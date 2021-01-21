package com.ep.studyplatform.settings;

import com.ep.studyplatform.WithAccount;
import com.ep.studyplatform.account.AccountRepository;
import com.ep.studyplatform.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    // 매번 계쩡을 만들어주면ㄴ 삭제를 다시한다.
    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("epepep") // beforeeach보다 먼저 실행하게 된다. setupBefore = TestExecutionEvent.TEST_EXECUTION을 넣으면 될 수도 있지만, 버그가 있어서 되지 않는다.
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfileForm() throws Exception {
        String bio = "짧은 소개를 수정하는 경우..";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

    }

    @WithAccount("epepep") // beforeeach보다 먼저 실행하게 된다. setupBefore = TestExecutionEvent.TEST_EXECUTION을 넣으면 될 수도 있지만, 버그가 있어서 되지 않는다.
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우..";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account epepep = accountRepository.findByNickname("epepep");
        assertEquals(bio,epepep.getBio());

    }

    @WithAccount("epepep") // beforeeach보다 먼저 실행하게 된다. setupBefore = TestExecutionEvent.TEST_EXECUTION을 넣으면 될 수도 있지만, 버그가 있어서 되지 않는다.
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "짧은 소개를 수정하는 경우..길게길게길게길게길게길게길게길게길게길게길게길게길게길게길게길게길게길게";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account epepep = accountRepository.findByNickname("epepep");
        assertNull(epepep.getBio());

    }

    @WithAccount("epepep")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("epepep")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_correct() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","123456789")
                .param("newPasswordConfirm","123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account epepep = accountRepository.findByNickname("epepep");
        assertTrue(passwordEncoder.matches("123456789",epepep.getPassword()));

    }

    @WithAccount("epepep")
    @DisplayName("패스워드 수정 - 입력값 오류")
    @Test
    void updatePassword_error() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","12")
                .param("newPassword","12")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));


        Account epepep = accountRepository.findByNickname("epepep");
        assertFalse(passwordEncoder.matches("123456789",epepep.getPassword()));

    }

    @WithAccount("epepep")
    @DisplayName("패스워드 수정 - 입력값 불일치")
    @Test
    void updatePassword_error2() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","123456789")
                .param("newPassword","111111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

        Account epepep = accountRepository.findByNickname("epepep");
        assertFalse(passwordEncoder.matches("111111111",epepep.getPassword()));

    }

}