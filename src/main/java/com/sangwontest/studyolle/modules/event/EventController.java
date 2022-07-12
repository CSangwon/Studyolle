package com.sangwontest.studyolle.modules.event;

import com.sangwontest.studyolle.modules.account.CurrentUser;
import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.study.Study;
import com.sangwontest.studyolle.modules.event.form.EventForm;
import com.sangwontest.studyolle.modules.event.validator.EventValidator;
import com.sangwontest.studyolle.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;


    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }


    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);// 매니저만 가져옴
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(new EventForm());

        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentUser Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Model model, Errors errors) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(eventForm, study, account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(id).orElseThrow());
        model.addAttribute(studyService.getStudy(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "study/events";

    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow();

        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(EventForm.from(event));

        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id
            , @Valid EventForm eventForm, Model model, Errors errors) {
        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, errors, event);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/delete")
    public String cancelEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        eventService.deleteEvent(eventRepository.findById(id).orElseThrow());
        return "redirect:/study/" + study.getEncodePath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(id).orElseThrow(), account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + id;
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(eventRepository.findById(id).orElseThrow(), account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + id;
    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/accept") // 아래 네개 post로 바꿔야함!!
    public String acceptEnrollment(@CurrentUser Account account, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment,
                                   @PathVariable String path) {
        //DB 에 저장되어있는 ID값으로 가져오는 경우, spring data jpa가 제공해주는 entity converter를 사용하여 코드를 줄일 수 있음
        //@PathVariable Long eventId, @PathVariable Long enrollmentId,원래 이렇게 적었던 것을
        //@PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment
        // 이렇게 되면 repository 로 부터 findbyId를 굳이 안해와도 해온 것 처럼 동작함
        Study study = studyService.getStudyToUpdate(account, path); // 관리자만 할 수 있으니까 관리자만 하게끔
        eventService.acceptEnrollment(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();

    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUser Account account, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment,
                                   @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.rejectEnrollment(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUser Account account, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment,
                                   @PathVariable String path) {
        //DB 에 저장되어있는 ID값으로 가져오는 경우, spring data jpa가 제공해주는 entity converter를 사용하여 코드를 줄일 수 있음
        //@PathVariable Long eventId, @PathVariable Long enrollmentId,원래 이렇게 적었던 것을
        //@PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment
        // 이렇게 되면 repository 로 부터 findbyId를 굳이 안해와도 해온 것 처럼 동작함

        Study study = studyService.getStudyToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/event/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String CancelCheckInEnrollment(@CurrentUser Account account, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment,
                                    @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }
}
