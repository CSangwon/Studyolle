package com.sangwontest.studyolle.modules.account;

import com.sangwontest.studyolle.modules.account.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Notifications {

    private boolean studyCreatedByEmail; // 스터디가 만들어졌다는걸 이메일로 받을 것인가.

    private boolean studyCreatedByWeb; // 웹으로 받을 것인가

    private boolean studyEnrollmentResultByEmail; // 스터디 모임에 가입신청결과를 이멜을로 받을것인가

    private boolean studyEnrollmentResultByWeb; // 웹으로 받을 것인가

    private boolean studyUpdateByEmail; // 스터디의 갱신정보를 이메일로 받을 것인가

    private boolean studyUpdateByWeb;

    public Notifications(Account account) {
        this.studyCreatedByEmail = account.isStudyCreatedByEmail();
        this.studyCreatedByWeb = account.isStudyCreatedByWeb();
        this.studyEnrollmentResultByEmail = account.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = account.isStudyEnrollmentResultByWeb();
        this.studyUpdateByEmail = account.isStudyUpdateByEmail();
        this.studyUpdateByWeb = account.isStudyUpdateByWeb();
    }
}
