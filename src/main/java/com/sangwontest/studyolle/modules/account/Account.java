package com.sangwontest.studyolle.modules.account;

import com.sangwontest.studyolle.modules.zone.Zones;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
//연관관계가 복잡해 질때, 여기서 서로다른 연관관계를 순환참조하느라 무한루프가 발생하고
//결국에는 stackoverflow가 발생할 수 있음 그래서 id만 사용함
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    //로그인 할 때 이메일과 닉네임으로 로그인 가능하게 하기위해 unique
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password; // 비밀번호

    private boolean emailVerified; //이메일인증 완료했는지

    private String emailCheckToken; // 이메일 인증 토큰

    private String bio; //자기소개

    private String url; // 웹사이트 url

    private String occupation; // 직업

    private String location; // 사는지역

    //프로필 사진은 varchar 기본 길이인 255보다 커질 수있기 때문에
    //lob이라고 매핑해주면 Text타입으로 매핑됨, EAGER
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage; // 프로필사진

    private boolean studyCreatedByEmail; // 스터디가 만들어졌다는걸 이메일로 받을 것인가.

    private boolean studyCreatedByWeb = true; // 웹으로 받을 것인가

    private boolean studyEnrollmentResultByEmail; // 스터디 모임에 가입신청결과를 이멜을로 받을것인가

    private boolean studyEnrollmentResultByWeb = true; // 웹으로 받을 것인가

    private boolean studyUpdateByEmail; // 스터디의 갱신정보를 이메일로 받을 것인가

    private boolean studyUpdateByWeb = true;

    private LocalDateTime emailCheckTokenGeneratedAt;

    @ManyToMany
    private Set<Tags> tags = new HashSet<>();

    @ManyToMany
    private Set<Zones> zones = new HashSet<>();

    @Builder
    public Account(Long id, String email, String nickname, String password,
                   boolean emailVerified, String emailCheckToken, String bio,
                   String url, String occupation, String location, String profileImage,
                   boolean studyCreatedByEmail, boolean studyCreatedByWeb, boolean studyEnrollmentResultByEmail,
                   boolean studyEnrollmentResultByWeb, boolean studyUpdateByEmail, boolean studyUpdateByWeb, LocalDateTime emailCheckTokenGeneratedAt) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.emailVerified = emailVerified;
        this.emailCheckToken = emailCheckToken;
        this.bio = bio;
        this.url = url;
        this.occupation = occupation;
        this.location = location;
        this.profileImage = profileImage;
        this.studyCreatedByEmail = studyCreatedByEmail;
        this.studyCreatedByWeb = studyCreatedByWeb;
        this.studyEnrollmentResultByEmail = studyEnrollmentResultByEmail;
        this.studyEnrollmentResultByWeb = studyEnrollmentResultByWeb;
        this.studyUpdateByEmail = studyUpdateByEmail;
        this.studyUpdateByWeb = studyUpdateByWeb;
        this.emailCheckTokenGeneratedAt = emailCheckTokenGeneratedAt;
    }

    public void updateProfile(Profile profile) {
        this.bio = profile.getBio();
        this.url = profile.getUrl();
        this.occupation = profile.getOccupation();
        this.location = profile.getLocation();
        this.profileImage = profile.getProfileImage();
    }

    public void updateNotifications(Notifications notifications) {
        this.studyCreatedByEmail = notifications.isStudyCreatedByEmail();
        this.studyCreatedByWeb = notifications.isStudyCreatedByWeb();
        this.studyEnrollmentResultByEmail = notifications.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = notifications.isStudyEnrollmentResultByWeb();
        this.studyUpdateByEmail = notifications.isStudyUpdateByEmail();
        this.studyUpdateByWeb = notifications.isStudyUpdateByWeb();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateEmailVerified(boolean verified, LocalDateTime regDate){
        this.emailVerified = verified;
        this.regDate = regDate;
    }

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
//        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusMinutes(1));
    }
}
