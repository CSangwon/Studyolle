package com.sangwontest.studyolle.modules.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    @InitBinder("signUpForm") //signUpForm 이라는 데이터를 받을 때 Binder를 설정할 수잇음
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
        // 이렇게 해놓으면 signUpForm 을 받을 때 404 검증과 validator 검증까지 가능
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {

        model.addAttribute("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) { // 에러가 발생하면 그 회원가입화면에 남아있고
            return "account/sign-up";
        }
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        // TODO 회원 가입 처리
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "account/login";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 한시간에 한번 만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {

        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        // 이메일이 존재해지않을때
        if (account == null) {
            model.addAttribute("error", "wrong email");
            log.info("emailError");
            return view;
        }
        // 토큰이 부정확할떄
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong token");
            log.info("tokenError");
            return view;
        }
        // 위에 모든걸 다 통과하면
        accountService.completeSignUp(account);

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;

    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if (nickname == null) {
            new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute("account", byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유요한 이메일 주소가 아닙니다.");
            return "redirect:/email-login";
        }

        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간에 한번 가능합니다.");
            return "redirect:/email-login";
        }

        accountService.sendLoginLink(account);
        redirectAttributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");

        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if (account == null || !account.isValidToken(token)) {
            model.addAttribute("error", "로그인 할 수 없습니다.");
            return view;
        }
        accountService.login(account);
        return view;
    }
}
