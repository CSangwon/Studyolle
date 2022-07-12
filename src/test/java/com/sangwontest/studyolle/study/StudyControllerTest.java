//package com.sangwontest.studyolle.study;
//
//import com.sangwontest.studyolle.WithAccount;
//import com.sangwontest.studyolle.modules.account.AccountRepository;
//import com.sangwontest.studyolle.modules.account.Account;
//import com.sangwontest.studyolle.modules.study.StudyRepository;
//import com.sangwontest.studyolle.modules.study.StudyService;
//import com.sangwontest.studyolle.modules.study.form.StudyForm;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@Transactional
//@AutoConfigureMockMvc
//class StudyControllerTest {
//
//    @Autowired
//    MockMvc mockMvc;
//    @Autowired
//    StudyRepository studyRepository;
//    @Autowired
//    AccountRepository accountRepository;
//    @Autowired
//    StudyService studyService;
//
//    @WithAccount("sangwon")
//    @DisplayName("스터디 맴버 뷰")
//    @Test
//    public void studyMemberView() throws Exception {
//        Account sangwon1 = accountRepository.findByNickname("sangwon");
//
//        StudyForm studyForm = new StudyForm();
//        studyForm.setPath("sangwon");
//        studyForm.setTitle("sangwon");
//        studyForm.setShortDescription("sangwon");
//        studyForm.setFullDescription("sangwon");
//        studyService.createNewStudy(studyForm, sangwon1);
//
//        mockMvc.perform(get("/study/sangwon/members")
//                        .param("path", "sangwon")
//                        .param("title", "sangwon")
//                        .param("shortDescription", "sangwon")
//                        .param("fullDescription", "sangwon")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(model().attributeExists("account"))
//                .andExpect(model().attributeExists("study"));
//
//    }
//
//
//    @WithAccount("sangwon")
//    @DisplayName("스터디 폼 조회")
//    @Test
//    public void createStudyForm() throws Exception {
//        mockMvc.perform(get("/new-study"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("study/form"))
//                .andExpect(model().attributeExists("account"))
//                .andExpect(model().attributeExists("studyForm"));
//    }
//
//
//
//    @WithAccount("sangwon")
//    @DisplayName("스터디 추가 - 정상")
//    @Test
//    public void createStudy() throws Exception {
//        mockMvc.perform(post("/new-study")
//                        .param("path", "sangwon")
//                        .param("title", "sangwon")
//                        .param("shortDescription", "sangwon")
//                        .param("fullDescription", "sangwon")
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/study/sangwon"));
//
//        boolean sangwon = studyRepository.existsByPath("sangwon");
//        assertTrue(sangwon);
//    }
//
//    @WithAccount("sangwon")
//    @DisplayName("스터디 추가 - 비정상(입력값 path)")
//    @Test
//    public void createStudyError1() throws Exception {
//        mockMvc.perform(post("/new-study")
//                        .param("path", "s")
//                        .param("title", "sangwon")
//                        .param("shortDescription", "sangwon")
//                        .param("fullDescription", "sangwon")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(view().name("study/form"))
//                .andExpect(model().attributeExists("studyForm"))
//                .andExpect(model().hasErrors());
//    }
//
//    @WithAccount("sangwon")
//    @DisplayName("스터디 추가 - 비정상(입력값 title)")
//    @Test
//    public void createStudyError2() throws Exception {
//        String title = "asfasdfasdfasdfafdasdasfasdfasdfasdfafdasdasfasdfasdfasdfafdasdasfasdfasdfasdfafdasd";
//        mockMvc.perform(post("/new-study")
//                        .param("path", "sangwon")
//                        .param("title", title)
//                        .param("shortDescription", "sangwon")
//                        .param("fullDescription", "sangwon")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(view().name("study/form"))
//                .andExpect(model().attributeExists("studyForm"))
//                .andExpect(model().hasErrors());
//    }
//
//    @WithAccount("sangwon")
//    @DisplayName("스터디 추가 - 비정상(중복)")
//    @Test
//    public void createStudyError3() throws Exception {
//        Account sangwon1 = accountRepository.findByNickname("sangwon");
//
//        StudyForm studyForm = new StudyForm();
//        studyForm.setPath("sangwon");
//        studyForm.setTitle("sangwon");
//        studyForm.setShortDescription("sangwon");
//        studyForm.setFullDescription("sangwon");
//        studyService.createNewStudy(studyForm, sangwon1);
//
//        mockMvc.perform(post("/new-study")
//                        .param("path", "sangwon")
//                        .param("title", "sangwon")
//                        .param("shortDescription", "sangwon")
//                        .param("fullDescription", "sangwon")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(view().name("study/form"))
//                .andExpect(model().attributeExists("studyForm"))
//                .andExpect(model().hasErrors());
//    }
//
//}