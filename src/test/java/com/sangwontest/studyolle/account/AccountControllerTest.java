package com.sangwontest.studyolle.account;

import com.sangwontest.studyolle.infra.mail.EmailMessage;
import com.sangwontest.studyolle.infra.mail.EmailService;
import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.account.AccountRepository;
import com.sangwontest.studyolle.modules.account.AccountService;
import com.sangwontest.studyolle.modules.account.SignUpForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;

    @MockBean
    EmailService emailService;

    @DisplayName("회원 가입 확인 보이는지 테스트")
    @Test
    public void signUpForm() throws Exception{
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk()) // 200이 나오는지
                .andExpect(view().name("account/sign-up")) // 실제로 보이는 페이지가 account/sign-up이 맞는지 확인
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated()); // 모델에 이름에 해당하는게 attribute에 있는지 확인
    }

    @DisplayName("회원가입 처리 - 입력값 오류")
    @Test
    public void signUpSubmit_error_with_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "sangwon")
                        .param("email", "email..")
                        .param("password", "12345")
                        .with(csrf())) // 이렇게 scrf를 같이 주지않으면 403 오류가 나게된다. 이유는 hidden 값으로 scrf토큰을 전송해서 내가 입력한 값인지 확인하기 때문이다.
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원가입 처리 - 입력값 정상  ")
    @Test
    public void signUpSubmit_correct_with_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "sangwon")
                        .param("email", "email@naver.com")
                        .param("password", "12345678")
                        .with(csrf())) // 이렇게 scrf를 같이 주지않으면 403 오류가 나게된다. 이유는 hidden 값으로 scrf토큰을 전송해서 내가 입력한 값인지 확인하기 때문이다.
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("sangwon"));

        Account account = accountRepository.findByEmail("email@naver.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678"); // encode해서 저장했으니 내가 입력한 값이랑 달라야 됨        assertNotNull(account.getEmailCheckToken());
        assertTrue(accountRepository.existsByEmail("email@naver.com"));
        //메일 보냈는지 확인
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @DisplayName("인증메일 확인 - 입력값 오류")
    @Test
    public void checkEmailToken_with_wrong_input() throws Exception{
        mockMvc.perform(get("/check-email-token")
                        .param("token", "asdfqwer")
                        .param("email", "email@naver.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());

    }

    @DisplayName("인증메일 확인 - 입력값 정상")
    @Test
    public void checkEmailToken() throws Exception{

        Account account = Account.builder()
                .email("test@naver.com")
                .password("12345678")
                .nickname("test")
                .build();

        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();


        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", account.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("test"));

    }

    @DisplayName("로그인 테스트 이메일")
    @Test
    public void login_with_email() throws Exception{
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@naver.com");
        signUpForm.setPassword("12341234");
        accountService.processNewAccount(signUpForm);

        mockMvc.perform(post("/login")
                        .param("username", "test@naver.com")
                        .param("password", "12341234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test"));
    }

    @DisplayName("로그인 테스트 닉네임")
    @Test
    public void login_with_Nickname() throws Exception{
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@naver.com");
        signUpForm.setPassword("12341234");
        accountService.processNewAccount(signUpForm);

        mockMvc.perform(post("/login")
                        .param("username", "test")
                        .param("password", "12341234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test"));
    }

    @DisplayName("로그인 실패")
    @Test
    public void login_fail() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@naver.com");
        signUpForm.setPassword("12341234");
        accountService.processNewAccount(signUpForm);

        mockMvc.perform(post("/login")
                        .param("username", "1111")
                        .param("password", "123123123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 실패")
    @Test
    public void logout() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@naver.com");
        signUpForm.setPassword("12341234");
        accountService.processNewAccount(signUpForm);

        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

}