package com.sangwontest.studyolle.modules.study;

import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.account.UserAccount;
import com.sangwontest.studyolle.modules.study.form.StudyDescriptionForm;
import com.sangwontest.studyolle.modules.study.form.StudyForm;
import com.sangwontest.studyolle.modules.account.Tags;
import com.sangwontest.studyolle.modules.zone.Zones;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
//조인 그래프를 정의 -> eagerfetch하도록 entitymanagergraph 정의
// 요청이 많이 들어오는 상황에서는 쿼리 갯수를 줄이는게 유리할 수 잇음 -> 현재 front에서 조회할때 5개의 쿼리가 발생하여 시간이 늦어지는 문제 발생함
@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")})
@NamedEntityGraph(name = "Study.withTagsAndManager", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers") // tags를 입력하고 삭제할때 모든 정보를 가져올 필요없이 매니저인지(권한확인)와 tags정보만 가져오면된다. zone도 같음
})
@NamedEntityGraph(name = "Study.withZonesAndManager", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")
})
@NamedEntityGraph(name = "Study.withManager", attributeNodes = {@NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withMember", attributeNodes = {@NamedAttributeNode("members")})
@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Study{

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tags> tags = new HashSet<>();

    @ManyToMany
    private Set<Zones> zones = new HashSet<>();

    private LocalDateTime publishedDatetime;

    private LocalDateTime closeDateTime;

    private LocalDateTime recruitingUpdateDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    @Builder
    public Study(Long id, String path, String title, String shortDescription,
                 String fullDescription, String image, LocalDateTime publishedDatetime,
                 LocalDateTime closeDateTime, LocalDateTime recruitingUpdateDateTime,
                 boolean recruiting, boolean published, boolean closed, boolean useBanner) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.image = image;
        this.publishedDatetime = publishedDatetime;
        this.closeDateTime = closeDateTime;
        this.recruitingUpdateDateTime = recruitingUpdateDateTime;
        this.recruiting = recruiting;
        this.published = published;
        this.closed = closed;
        this.useBanner = useBanner;
    }


    public void createNewStudy(StudyForm studyForm) {
        this.path = studyForm.getPath();
        this.title = studyForm.getTitle();
        this.shortDescription = studyForm.getShortDescription();
        this.fullDescription = studyForm.getFullDescription();
    }

    public void addManager(Account account) {
        this.managers.add(account);
    }


    public boolean isJoinable(UserAccount userAccount) { //가입이 가능한가?
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }
    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public boolean isManagerOf(Account account) {
        return this.getManagers().contains(account);
    }

    public void updateDescription(StudyDescriptionForm studyDescriptionForm) {
        this.shortDescription = studyDescriptionForm.getShortDescription();
        this.fullDescription = studyDescriptionForm.getFullDescription();
    }

    public void updateImage(String image) {
        this.image = image;
    }

    public void setBanner(boolean useBanner) {
        this.useBanner = useBanner;
    }


    public void publish() {
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDatetime = LocalDateTime.now();
        } else{
            throw new RuntimeException("스터디를 공개할 수 없는 상태 입니다. 스터디를 이미 공개 했거나 종료했습니다.");
        }
    }

    public void close(){
        if (this.published && !this.closed){
            this.closed = true;
            this.closeDateTime = LocalDateTime.now();
        } else{
            throw new RuntimeException("스터디를 공개할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public boolean isRemovable() {
        return !this.published;
    }

    public void startRecruit(){
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        } else{
            throw new RuntimeException("인원 모임을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤에 다시 시도하세요");
        }
    }

    public void stopRecruit(){
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        } else{
            throw new RuntimeException("인원 모임을 중지 할 수 없습니다. 스터디를 공개하거나 한 시간 뒤에 다시 시도하세요");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdateDateTime == null || this.recruitingUpdateDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    public void changePath(String newPath) {
        this.path = newPath;
    }

    public String getEncodePath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public void addMember(Account account) {
        this.members.add(account);
    }

    public void removeMember(Account account) {
        this.members.remove(account);
    }
}
