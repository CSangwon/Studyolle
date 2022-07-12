package com.sangwontest.studyolle.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangwontest.studyolle.WithAccount;
import com.sangwontest.studyolle.modules.account.AccountRepository;
import com.sangwontest.studyolle.modules.account.TagForm;
import com.sangwontest.studyolle.modules.account.repository.TagsRepository;
import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.study.Study;
import com.sangwontest.studyolle.modules.study.StudyRepository;
import com.sangwontest.studyolle.modules.study.StudyService;
import com.sangwontest.studyolle.modules.account.Tags;
import com.sangwontest.studyolle.modules.study.form.StudyForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class StudySettingControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    StudyService studyService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TagsRepository tagsRepository;


    static final String STUDY_PATH = "sangwon";

    @BeforeEach
    void setStudy() {
        Account sangwon1 = accountRepository.findByNickname("sangwon");

        StudyForm studyForm = new StudyForm();
        studyForm.setPath(STUDY_PATH);
        studyForm.setTitle("sangwon");
        studyForm.setShortDescription("sangwon");
        studyForm.setFullDescription("sangwon");
        studyService.createNewStudy(studyForm, sangwon1);
    }

    @AfterEach
    void afterEach() {
        studyRepository.deleteAll();
    }

    @WithAccount("sangwon")
    @DisplayName("스터디 태그 추가")
    @Test
    public void studyTagAdd() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("test");
        mockMvc.perform(post("/study/" + STUDY_PATH + "/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tags test = tagsRepository.findByTitle("test");
        assertNotNull(test);

        Study study = studyRepository.findStudyWithTagsByPath(STUDY_PATH);
        assertTrue(study.getTags().contains(test));
    }

    @WithAccount("sangwon")
    @DisplayName("스터디 배너 폼")
    @Test
    public void studyBannerSettingForm() throws Exception {
        mockMvc.perform(get("/study/" + STUDY_PATH + "/settings/banner"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/banner"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @WithAccount("sangwon")
    @DisplayName("스터디 배너 등록")
    @Test
    public void studyBannerSetting() throws Exception {
        mockMvc.perform(post("/study/" + STUDY_PATH + "/settings/banner")
                        .param("image", "image-test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + STUDY_PATH + "/settings/banner"));
    }

    @WithAccount("sangwon")
    @DisplayName("스터디 배너 사용")
    @Test
    public void studyBannerEnable() throws Exception {
        mockMvc.perform(post("/study/" + STUDY_PATH + "/settings/banner/enable")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + STUDY_PATH + "/settings/banner"));

        Study byPath = studyRepository.findByPath(STUDY_PATH);

        assertTrue(byPath.isUseBanner());
    }

    @WithAccount("sangwon")
    @DisplayName("스터디 배너 미사용")
    @Test
    public void studyBannerDisable() throws Exception {
        mockMvc.perform(post("/study/" + STUDY_PATH + "/settings/banner/disable")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + STUDY_PATH + "/settings/banner"));

        Study byPath = studyRepository.findByPath(STUDY_PATH);

        assertFalse(byPath.isUseBanner());

    }


    @WithAccount("sangwon")
    @DisplayName("스터디 세팅 폼")
    @Test
    public void studySettingForm() throws Exception {
        mockMvc.perform(get("/study/" + STUDY_PATH + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @WithAccount("sangwon")
    @DisplayName("스터디 세팅 정상")
    @Test
    public void studySetting() throws Exception {
        Account account = accountRepository.findByNickname("sangwon");
        mockMvc.perform(post("/study/" + STUDY_PATH + "/settings/description")
                        .param("shortDescription", "tests")
                        .param("fullDescription", "testf")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + STUDY_PATH + "/settings/description"));

        Study study = studyService.getStudyToUpdate(account, STUDY_PATH);
        assertThat("tests").isEqualTo(study.getShortDescription());
        assertEquals("tests", study.getShortDescription());

        assertThat("testf").isEqualTo(study.getFullDescription());
        assertEquals("testf", study.getFullDescription());

    }
}