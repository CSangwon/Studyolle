package com.sangwontest.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangwontest.studyolle.WithAccount;
import com.sangwontest.studyolle.modules.account.AccountRepository;
import com.sangwontest.studyolle.modules.account.AccountService;
import com.sangwontest.studyolle.modules.account.TagForm;
import com.sangwontest.studyolle.modules.account.repository.TagsRepository;
import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.account.SettingsController;
import com.sangwontest.studyolle.modules.account.Tags;
import org.junit.jupiter.api.AfterEach;
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
@Transactional
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TagsRepository tagsRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountService accountService;


    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    @WithAccount("sangwon") // 인증된 사용자만 접근 가능하기 때문에 이게 없으면 동작하지 않음!!
    @DisplayName("태그 수정 - 폼")
    @Test
    public void updateTagsForm() throws Exception {
        mockMvc.perform(get("/" + SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whiteList"));
    }

    @WithAccount("sangwon") // 인증된 사용자만 접근 가능하기 때문에 이게 없으면 동작하지 않음!!
    @DisplayName("태그 추가")
    @Test
    public void addTags() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/" + SettingsController.SETTINGS_TAGS_VIEW_NAME + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tags newTag = tagsRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account sangwon = accountRepository.findByNickname("sangwon");
        assertTrue(sangwon.getTags().contains(newTag));
    }

    @WithAccount("sangwon") // 인증된 사용자만 접근 가능하기 때문에 이게 없으면 동작하지 않음!!
    @DisplayName("태그 삭제")
    @Test
    public void removeTags() throws Exception {

        Account sangwon1 = accountRepository.findByNickname("sangwon");
        Tags newTag1 = tagsRepository.save(Tags.builder().title("newTag").build());
        accountService.addTag(sangwon1, newTag1);

        assertTrue(sangwon1.getTags().contains(newTag1));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/" + SettingsController.SETTINGS_TAGS_VIEW_NAME + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());


        assertFalse(sangwon1.getTags().contains(newTag1));
    }
    //test오류 공부할것


    @WithAccount("sangwon") // 인증된 사용자만 접근 가능하기 때문에 이게 없으면 동작하지 않음!!
    @DisplayName("프로필 수정하기 - 폼")
    @Test
    public void updateProfileForm() throws Exception{

        mockMvc.perform(get("/" + SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }


    @WithAccount("sangwon")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    public void updateProfile() throws Exception{
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post("/" + SettingsController.SETTINGS_PROFILE_VIEW_NAME)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(flash().attributeExists("message"));

        Account sangwon = accountRepository.findByNickname("sangwon");
        assertEquals(bio, sangwon.getBio());
    }

    @WithAccount("sangwon")
    @DisplayName("프로필 수정하기 - 입력값 error")
    @Test
    public void updateProfile_error() throws Exception{
        String bio = "길게 소개를 수정하는 경우짧은 길게 수정하는 경우짧은 길게 수정하는 경우짧은 길게 수정하는 경우짧은 길게 수정하는 경우짧은 소개를 수정하는 경우";
        mockMvc.perform(post("/" + SettingsController.SETTINGS_PROFILE_VIEW_NAME)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account sangwon = accountRepository.findByNickname("sangwon");
        assertNull(sangwon.getBio());
    }

    @WithAccount("sangwon") // 인증된 사용자만 접근 가능하기 때문에 이게 없으면 동작하지 않음!!
    @DisplayName("패스워드 수정하기 - 폼")
    @Test
    public void updatePasswordForm() throws Exception{
        mockMvc.perform(get("/" + SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }


    @WithAccount("sangwon")
    @DisplayName("패스워드 수정하기 - 입력값 정상")
    @Test
    public void updatePassword() throws Exception{
        mockMvc.perform(post("/" + SettingsController.SETTINGS_PASSWORD_VIEW_NAME)
                        .param("newPassword", "123123123")
                        .param("newPasswordConfirm", "123123123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(flash().attributeExists("message"));

        Account sangwon = accountRepository.findByNickname("sangwon");
        assertTrue(passwordEncoder.matches("123123123", sangwon.getPassword()));
    }

    @WithAccount("sangwon")
    @DisplayName("패스워드 수정하기 - 입력값 error")
    @Test
    public void updatePassword_error() throws Exception{
        mockMvc.perform(post("/" + SettingsController.SETTINGS_PASSWORD_VIEW_NAME)
                        .param("newPassword", "123123123")
                        .param("newPasswordConfirm", "123412341")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors());
    }

    @WithAccount("sangwon")
    @DisplayName("계정의 지역정보 수정 폼")
    @Test
    public void updateZonesForm() throws Exception{
        mockMvc.perform(get("/" + SettingsController.SETTINGS_ZONE_NAME))
                .andExpect(view().name(SettingsController.SETTINGS_ZONE_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whiteList"));
    }


}