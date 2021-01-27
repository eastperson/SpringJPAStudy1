package com.ep.studyplatform.modules.account;

import com.ep.studyplatform.modules.account.form.TagForm;
import com.ep.studyplatform.modules.account.form.ZoneForm;
import com.ep.studyplatform.modules.tag.Tag;
import com.ep.studyplatform.modules.tag.TagRepository;
import com.ep.studyplatform.modules.zone.Zone;
import com.ep.studyplatform.modules.zone.ZoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    // 매번 계쩡을 만들어주면ㄴ 삭제를 다시한다.
    @AfterEach
    void afterEach() {
        accountRepository.deleteAll(); zoneRepository.deleteAll();
    }

    @WithAccount("epepep")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZoneForm() throws Exception{
        mockMvc.perform(get(SettingsController.SETTINGS_ZONE_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(status().isOk());

    }

    @Transactional
    @WithAccount("epepep")
    @DisplayName("계정의 지역 정보 수정 추가")
    @Test
    void addZone() throws Exception{
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONE_URL+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                //.content("{\"tagTitle\":newTag\"}"))
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account epepep= accountRepository.findByNickname("epepep");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(),testZone.getProvince());
        assertTrue(epepep.getZones().contains(zone));
    }


    @Transactional
    @WithAccount("epepep")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void removeZone() throws Exception{

        Account epepep = accountRepository.findByNickname("epepep");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(),testZone.getProvince());
        accountService.addZone(epepep,zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONE_URL+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                //.content("{\"tagTitle\":newTag\"}"))
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(epepep.getZones().contains(zone));
    }



    @WithAccount("epepep") // beforeeach보다 먼저 실행하게 된다. setupBefore = TestExecutionEvent.TEST_EXECUTION을 넣으면 될 수도 있지만, 버그가 있어서 되지 않는다.
    @DisplayName("계정의 태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception{
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));

    }
    // Detached 상태를 Persistent 상태로 바꾸기 위해서 @Transactional 추가
    @Transactional
    @WithAccount("epepep") // beforeeach보다 먼저 실행하게 된다. setupBefore = TestExecutionEvent.TEST_EXECUTION을 넣으면 될 수도 있지만, 버그가 있어서 되지 않는다.
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");


        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL+"/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        //.content("{\"tagTitle\":newTag\"}"))
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                        .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        accountRepository.findByNickname("epepep").getTags().contains(newTag);
    }

    // Detached 상태를 Persistent 상태로 바꾸기 위해서 @Transactional 추가
    @Transactional
    @WithAccount("epepep") // beforeeach보다 먼저 실행하게 된다. setupBefore = TestExecutionEvent.TEST_EXECUTION을 넣으면 될 수도 있지만, 버그가 있어서 되지 않는다.
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception{
        Account epepep = accountRepository.findByNickname("epepep");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(epepep,newTag);

        assertTrue(epepep.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");


        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                //.content("{\"tagTitle\":newTag\"}"))
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(epepep.getTags().contains(newTag));
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