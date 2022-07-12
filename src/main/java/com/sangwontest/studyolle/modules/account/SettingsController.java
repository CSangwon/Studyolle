package com.sangwontest.studyolle.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sangwontest.studyolle.modules.account.repository.TagsRepository;
import com.sangwontest.studyolle.modules.account.validator.NicknameValidator;
import com.sangwontest.studyolle.modules.account.validator.PasswordFormValidator;
import com.sangwontest.studyolle.modules.zone.ZoneRepository;
import com.sangwontest.studyolle.modules.zone.Zones;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SettingsController {

    public static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    public static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
    public static final String SETTINGS_NOTIFICATION_VIEW_NAME = "settings/notifications";
    public static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";
    public static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";
    public static final String SETTINGS_ZONE_NAME = "settings/zones";

    private final AccountService accountService;
    private final TagService tagService;
    private final NicknameValidator nicknameValidator;
    private final TagsRepository tagsRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;


    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Profile(account));

        return SETTINGS_PROFILE_VIEW_NAME;
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/" + SETTINGS_PROFILE_VIEW_NAME;
    }

    @GetMapping("/settings/password")
    public String passwordUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    @PostMapping("/settings/password")
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("message", "패스워드를 변경했습니다.");

        return "redirect:/" + SETTINGS_PASSWORD_VIEW_NAME;
    }

    @GetMapping("/settings/notifications")
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Notifications(account));

        return SETTINGS_NOTIFICATION_VIEW_NAME;
    }

    @PostMapping("/settings/notifications")
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_NOTIFICATION_VIEW_NAME;
        }

        accountService.updateNotifications(account, notifications);
        return "redirect:/" + SETTINGS_NOTIFICATION_VIEW_NAME;
    }


    @GetMapping("/settings/account")
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new NicknameForm(account.getNickname()));

        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping("/settings/account")
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        redirectAttributes.addFlashAttribute("message", "닉네임을 수정하였습니다.");

        return "redirect:/" + SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @GetMapping("/settings/tags")
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tags> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tags::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagsRepository.findAll().stream().map(Tags::getTitle).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(allTags));

        return SETTINGS_TAGS_VIEW_NAME;
    }

    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTags(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tags tags = tagService.findOrCreateNew(title);

        accountService.addTag(account, tags);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTags(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tags tags = tagsRepository.findByTitle(title);
        if (tags == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tags);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/zones")
    public String updateZone(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Zones> zone = accountService.getZone(account);
        model.addAttribute("zones", zone.stream().map(Zones::toString).collect(Collectors.toList()));

        List<String> allZone = zoneRepository.findAll().stream().map(Zones::toString).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(allZone));

        return SETTINGS_ZONE_NAME;
    }

    @PostMapping("/settings/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zones zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zones zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }
}
