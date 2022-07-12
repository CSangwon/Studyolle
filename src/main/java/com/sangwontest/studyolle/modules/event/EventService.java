package com.sangwontest.studyolle.modules.event;

import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.event.form.EventForm;
import com.sangwontest.studyolle.modules.study.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Event createEvent(EventForm eventForm, Study study, Account account) {
        log.info(eventForm.getEventType().toString());
        Event event = Event.builder()
                .title(eventForm.getTitle())
                .description(eventForm.getDescription())
                .eventType(eventForm.getEventType())
                .endEnrollmentDateTime(eventForm.getEndEnrollmentDateTime())
                .startDateTime(eventForm.getStartDateTime())
                .endDateTime(eventForm.getEndDateTime())
                .limitOfEnrollment(eventForm.getLimitOfEnrollments())
                .createdDateTime(LocalDateTime.now())
                .createdBy(account)
                .study(study)
                .build();


        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        event.updateEvent(eventForm);
        event.acceptWaitingList(); // 기다리는 리스트를 추가함 늘어난 숫자만큼 자동으로 늘려줌
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.newEnrollment(LocalDateTime.now(), event.isAbleToAcceptWaitingEnrollment(), account); // setAccepted의 값을 그때 상황에 맞게 바꿔주는 것이 isabletoaccept~~이다.
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment); // 관계를 끊어준다
            enrollmentRepository.delete(enrollment);

            event.acceptNextWaitingEnrollment();
        }
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
    }


    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
