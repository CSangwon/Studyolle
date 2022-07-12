package com.sangwontest.studyolle.modules.account;

import com.sangwontest.studyolle.infra.config.AppProperties;
import com.sangwontest.studyolle.modules.zone.Zones;
import com.sangwontest.studyolle.infra.mail.EmailMessage;
import com.sangwontest.studyolle.infra.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;


    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm); // 메일 보내는 부분에서 오류가나면 새로운 유저는 등록이 안된다
        newAccount.generateEmailCheckToken(); // 토큰값 생성
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    }

    /*
    회원저장
     */
    private Account saveNewAccount(SignUpForm signUpForm) {

        //회원 저장
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdateByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);
        return newAccount;
    }
    /*
    이메일
     */
    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디 올래 서비스를 사용하려면 링크를 클릭하세요");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올레, 회원가입 인증")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }


    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {

        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.updateEmailVerified(true, LocalDateTime.now());
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        account.updateProfile(profile);
        accountRepository.save(account); // merge
    }

    public void updatePassword(Account account, String newPassword) {
        account.updatePassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account); // merge
    }

    public void updateNotifications(Account account, Notifications notifications) {
        account.updateNotifications(notifications);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.updateNickname(nickname);
        accountRepository.save(account); // detached 객체이기 때문에 변경이력을 감지 하지않아 자동으로 db 반영 해주지않음
        login(account); //로그인을 안해주면 닉네임 표시해주는 부분이 변경되지않음
        //닉네임을 내부적으로 수정하였어도 인증정보를 업데이트시켜주지 않으면 해당 내용이 반영되지 않아 실제 프로필 아이콘 하위 메뉴에 있는 닉네임은 기존 닉네임이 계속 표시되게 됨.
    }

    /*
    이메일 보내기
     */
    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken()
                + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일로 로그인하기");
        context.setVariable("message", "스터디 올래 로그인하려면 링크를 클릭하세요");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올레, 로그인 링크 ")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public Set<Tags> getTags(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getTags();
    }

    public void addTag(Account account, Tags tags) {
        accountRepository.findById(account.getId()) //Account가 detached상태이기 때문에 persist상태로 가져와야함 그래서 findByid 해줌
                .ifPresent(a -> a.getTags().add(tags));
    }

    public void removeTag(Account account, Tags tags) {
        accountRepository.findById(account.getId()) //Account가 detached상태이기 때문에 persist상태로 가져와야함 그래서 findByid 해줌
                .ifPresent(a -> a.getTags().remove(tags));
    }

    public Set<Zones> getZone(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getZones();
    }

    public void addZone(Account account, Zones zones) {
        accountRepository.findById(account.getId())//Account가 detached상태이기 때문에 persist상태로 가져와야함 그래서 findByid 해줌
                .ifPresent(a -> a.getZones().add(zones));
    }

    public void removeZone(Account account, Zones zones) {
        accountRepository.findById(account.getId()) //Account가 detached상태이기 때문에 persist상태로 가져와야함 그래서 findByid 해줌
                .ifPresent(a -> a.getZones().remove(zones));
    }
}
