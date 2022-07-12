package com.sangwontest.studyolle.modules.event;


import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.account.UserAccount;
import com.sangwontest.studyolle.modules.event.form.EventForm;
import com.sangwontest.studyolle.modules.study.Study;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = true)
    private Integer limitOfEnrollment;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAttended(userAccount) && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAttended(userAccount) && isAlreadyEnrolled(userAccount);
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e: this.enrollments) {
            if (e.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e: this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }
        return false;
    }

    public int numberOfRemainSpots() {
        return this.limitOfEnrollment - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public void updateEvent(EventForm eventForm) {
        this.title = eventForm.getTitle();
        this.description = eventForm.getDescription();
        this.eventType = eventForm.getEventType();
        this.endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();
        this.startDateTime = eventForm.getStartDateTime();
        this.endDateTime = eventForm.getEndDateTime();
        this.limitOfEnrollment = eventForm.getLimitOfEnrollments();

    }

    public boolean canAccept(Enrollment enrollment) {
        //관리자가 확인을 해야하는 모임이고 enrollment에 있으며 참석하지않고 수락하지 않은경우 accept가능
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    public boolean canReject(Enrollment enrollment) {
        //관리자가 확인을 해야하는 모임이고 enrollment에 있으며 참석하지않고 수락한경우 reject가능
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);

    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    public boolean isAbleToAcceptWaitingEnrollment() {
        return this.eventType == EventType.FCFS && this.limitOfEnrollment > this.getNumberOfAcceptedEnrollments();
    }

    private Enrollment getTheFirstWaitingEnrollment() { // 가장 첫번째로 대기중인것을 가져온다
        for (Enrollment e : this.enrollments) {
            if (!e.isAccepted()) {
                return e;
            }
        }
        return null;
    }

    //이벤트에 한명이 확정상태였던 사용자가 빠지면, 새로운 유저를 확정상태를 만들어주는 로직
    public void acceptNextWaitingEnrollment() {
        if (this.isAbleToAcceptWaitingEnrollment()) { // 추가적으로 인원을 받을 수 잇는 지확인하고
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if (enrollmentToAccept != null) {
                enrollmentToAccept.builder().accepted(true);
            }
        }
    }

    public void acceptWaitingList() {
        //늘어난 숫자만큼 자동으로 늘려줌
        if (this.isAbleToAcceptWaitingEnrollment()) {
            var waitingList = getWaitingList();
            int numberToAccept = (int) Math.min(this.limitOfEnrollment - this.getNumberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0, numberToAccept).forEach(e -> e.setAccepted(true));
        }
    }
    private List<Enrollment> getWaitingList() {
        return this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
    }

    public void accept(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE && this.limitOfEnrollment > this.getNumberOfAcceptedEnrollments()) {
            enrollment.setAccepted(true);
        }
    }

    public void reject(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE) {
            enrollment.setAccepted(false);
        }
    }
}
