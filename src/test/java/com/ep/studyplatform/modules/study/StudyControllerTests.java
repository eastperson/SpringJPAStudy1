package com.ep.studyplatform.modules.study;

import com.ep.studyplatform.modules.account.WithAccount;
import com.ep.studyplatform.modules.account.Account;
import com.ep.studyplatform.modules.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@RequiredArgsConstructor
@SpringBootTest
@Log4j2
public class StudyControllerTests {


    @Autowired protected MockMvc mockMvc;
    @Autowired protected StudyService studyService;
    @Autowired protected StudyRepository studyRepository;
    @Autowired protected AccountRepository accountRepository;

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("스터디 가입")
    void joinStudy() throws Exception {
        Account whiteship = createAccount("whiteship");

        Study study = createStudy("test-study", whiteship);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account keesun = accountRepository.findByNickname("keesun");
        assertTrue(study.getMembers().contains(keesun));
    }

    @Test
    @WithAccount("keesun")
    @DisplayName("스터디 탈퇴")
    void leaveStudy() throws Exception {
        Account whiteship = createAccount("whiteship");
        Study study = createStudy("test-study", whiteship);

        Account keesun = accountRepository.findByNickname("keesun");
        studyService.addMember(study, keesun);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(keesun));
    }

    protected Study createStudy(String path, Account manager) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, manager);
        return study;
    }

    protected Account createAccount(String nickname) {
        Account whiteship = new Account();
        whiteship.setNickname(nickname);
        whiteship.setEmail(nickname + "@email.com");
        accountRepository.save(whiteship);
        return whiteship;
    }

    @Test
    @WithAccount("epepep")
    @DisplayName("스터디 개설 폼 조회")
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithAccount("epepep")
    @DisplayName("스터디 개설 폼 입력 - 입력값 정상")
    void study_input_correct() throws Exception{
        mockMvc.perform(post("/new-study")
                .param("path","test-path")
                .param("title","제목값")
                .param("shortDescription","짧은 소개 입니다.")
                .param("fullDescription","긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("epepep");
        assertTrue(study.getManagers().contains(account));

    }

    @Test
    @WithAccount("epepep")
    @DisplayName("스터디 개설 폼 입력 - 입력값 오류")
    void study_input_error() throws Exception{
        mockMvc.perform(post("/new-study")
                .param("path","d")
                .param("title","제목값")
                .param("shortDescription","짧은 소개 입니다.")
                .param("fullDescription","긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.긴 소개입니다.")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("study/form"))
                .andExpect(authenticated().withUsername("epepep"));

    }

    @Test
    @WithAccount("epepep")
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception{
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p?full description</p>");

        Account epepep = accountRepository.findByNickname("epepep");
        studyService.createNewStudy(study,epepep);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

    }



}
