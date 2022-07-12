package com.sangwontest.studyolle.modules.event;

import com.sangwontest.studyolle.modules.account.Account;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

    public void newEnrollment(LocalDateTime enrolledAt, boolean accepted, Account account) {
        this.enrolledAt = enrolledAt;
        this.account = account;
        this.accepted = accepted;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setAccepted(boolean b) {
        this.accepted = b;
    }

    public void setAttended(boolean b) {
        this.attended = b;
    }
}
