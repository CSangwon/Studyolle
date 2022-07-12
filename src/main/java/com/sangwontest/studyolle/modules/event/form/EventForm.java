package com.sangwontest.studyolle.modules.event.form;

import com.sangwontest.studyolle.modules.event.Event;
import com.sangwontest.studyolle.modules.event.EventType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class EventForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    private String description;

    private EventType eventType = EventType.FCFS;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endEnrollmentDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    @Min(2)
    private Integer limitOfEnrollments = 2;

    public static EventForm from(Event event) {
        EventForm eventForm = new EventForm();
        eventForm.title = event.getTitle();
        eventForm.description = event.getDescription();
        eventForm.eventType = event.getEventType();
        eventForm.endEnrollmentDateTime = event.getEndEnrollmentDateTime();
        eventForm.startDateTime = event.getStartDateTime();
        eventForm.endDateTime = event.getEndDateTime();
        eventForm.limitOfEnrollments = event.getLimitOfEnrollment();
        return eventForm;
    }
}