package com.ep.studyplatform.study;

import com.ep.studyplatform.WithAccount;
import com.ep.studyplatform.account.AccountRepository;
import com.ep.studyplatform.domain.Account;
import com.ep.studyplatform.domain.Study;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@RequiredArgsConstructor
@SpringBootTest
public class StudyControllerTests {


    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
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
        studyService.createNewStsudy(study,epepep);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

    }



}
