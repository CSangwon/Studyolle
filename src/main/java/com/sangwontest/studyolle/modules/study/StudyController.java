package com.sangwontest.studyolle.modules.study;

import com.sangwontest.studyolle.modules.account.CurrentUser;
import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.study.form.StudyForm;
import com.sangwontest.studyolle.modules.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final StudyRepository studyRepository;
    private final StudyFormValidator studyFormValidator;

    @InitBinder("studyForm") // <- 이러한 값이 들어오면
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        // 스터디를 추가할 때 studyFormValidator를 먼저 사용해서 존재하는 url 인지 검사를 먼저 함
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentUser Account account, @Valid StudyForm studyForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(studyForm, account);

        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);

        model.addAttribute(account);
        model.addAttribute(study);

        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);

        model.addAttribute(account);
        model.addAttribute(study);

        return "study/members";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentUser Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.addMember(study, account);
        return "redirect:/study/" + study.getEncodePath() + "members";

    }

    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentUser Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.leaveMember(study, account);
        return "redirect:/study/" + study.getEncodePath() + "members";
    }

}
