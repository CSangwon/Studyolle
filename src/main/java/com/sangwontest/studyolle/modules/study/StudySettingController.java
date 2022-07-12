package com.sangwontest.studyolle.modules.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangwontest.studyolle.modules.account.CurrentUser;
import com.sangwontest.studyolle.modules.account.TagForm;
import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.account.ZoneForm;
import com.sangwontest.studyolle.modules.account.repository.TagsRepository;
import com.sangwontest.studyolle.modules.study.form.StudyDescriptionForm;
import com.sangwontest.studyolle.modules.study.form.StudyPathForm;
import com.sangwontest.studyolle.modules.study.validator.StudyPathValidator;
import com.sangwontest.studyolle.modules.account.TagService;
import com.sangwontest.studyolle.modules.account.Tags;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
    @RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingController {

    private final StudyService studyService;
    private final TagService tagService;
    private final TagsRepository tagsRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;
    private final StudyPathValidator studyPathValidator;

    @InitBinder("studyPathForm") // <- 이러한 값이 들어오면
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        // 스터디를 추가할 때 studyFormValidator를 먼저 사용해서 존재하는 url 인지 검사를 먼저 함
        webDataBinder.addValidators(studyPathValidator);
    }

    @GetMapping("/study")
    public String studySettingForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new StudyPathForm());
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes redirectAttributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        redirectAttributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }

    @PostMapping("/study/close")
    public String CloseStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes redirectAttributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        redirectAttributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }

    @PostMapping("/study/title")
    public String changeStudyTitle(@CurrentUser Account account, @PathVariable String path, @RequestParam String newTitle, Model model, RedirectAttributes redirectAttributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute("studyTitleError", "스터디 이름을 다시 입력하세요");
            return "redirect:/study/" + encode(path) + "/settings/study";
        }
        studyService.changeTitle(study, newTitle);
        redirectAttributes.addFlashAttribute("message", "스터디 이름을 수정했습니다.");

        return "redirect:/study/" + encode(path) + "/settings/study";
    }

    @PostMapping("/study/path")
    public String changeStudyPath(@CurrentUser Account account, @Valid StudyPathForm studyPathForm, Errors errors, @PathVariable String path,  Model model, RedirectAttributes redirectAttributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute("studyPathError", "스터디 경로를 다시 입력하세요");
            return "redirect:/study/" + encode(studyPathForm.getPath()) + "/settings/study";
        }
        studyService.changePath(study, studyPathForm);
        redirectAttributes.addFlashAttribute("message", "스터디 경로를 수정했습니다.");

        return "redirect:/study/" + encode(studyPathForm.getPath()) + "/settings/study";
    }

    @PostMapping("/study/remove")
    public String removeStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.remove(study);
        return "redirect:/";
    }

    //팀원 모집
    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes redirectAttributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(!study.canUpdateRecruiting()){
            redirectAttributes.addFlashAttribute("message", "1시간 안에 인원모집 설정을 여러번 변경 할 수 없습니다.");
            return "redirect:/study/" + encode(path) + "/settings/study";
        }

        studyService.startRecruit(study);
        redirectAttributes.addFlashAttribute("message", "인원모집을 시작합니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes redirectAttributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        if(!study.canUpdateRecruiting()){
            redirectAttributes.addFlashAttribute("message", "1시간 안에 인원모집 설정을 여러번 변경 할 수 없습니다.");
            return "redirect:/study/" + encode(path) + "/settings/study";
        }

        studyService.stopRecruit(study);
        redirectAttributes.addFlashAttribute("message", "인원모집을 종료합니다.");
        return "redirect:/study/" + encode(path) + "/settings/study";
    }


    @GetMapping("/tags")
    public String studyTagsForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {

        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream()
                .map(Tags::getTitle).collect(Collectors.toList())); //지금 스터디가 가지고잇는 태그들

        List<String> allTags = tagsRepository.findAll().stream()
                .map(Tags::getTitle).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(allTags));
        //디비에 저장된 전체 태그

        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity studyAddTags(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagform) {
        Study study = studyService.getStudyToUpdateTags(account, path);

        log.info(tagform.getTagTitle());
        Tags tags = tagService.findOrCreateNew(tagform.getTagTitle());
        studyService.addTag(study, tags);

        return ResponseEntity.ok().build();

    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity studyRemoveTags(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagform) {
        Study study = studyService.getStudyToUpdateTags(account, path);
        Tags tags = tagsRepository.findByTitle(tagform.getTagTitle()); // 태그가 없으면 새로생성
        if (tags == null) {
            return ResponseEntity.badRequest().build();
        }
        studyService.removeTag(study, tags);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/zones")
    public String studyZonesForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("zones", study.getZones().stream()
                .map(Zones::toString).collect(Collectors.toList()));

        List<String> allZone = zoneRepository.findAll().stream()
                .map(Zones::toString).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(allZone));

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    public ResponseEntity studyZonesAdd(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zones zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    public ResponseEntity studyZonesRemove(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zones zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/banner")
    public String studyImageForm(@CurrentUser Account account, @PathVariable String path, Model model) {

        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String studyImageSubmit(@CurrentUser Account account, @PathVariable String path,
                                   String image, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");

        return "redirect:/study/" + encode(path) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);

        return "redirect:/study/" + encode(path) + "/settings/banner";

    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + encode(path) + "/settings/banner";
    }


    @GetMapping("/description")
    public String viewStudyMembers(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(StudyDescriptionForm.builder()
                .shortDescription(study.getShortDescription())
                .fullDescription(study.getFullDescription())
                .build());

        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudy(@CurrentUser Account account, @PathVariable String path, @Valid StudyDescriptionForm studyDescriptionForm,
                              Errors errors, Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }
        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + encode(path) + "/settings/description";
    }

    private String encode(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
