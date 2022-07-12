package com.sangwontest.studyolle.modules.study;

import com.sangwontest.studyolle.modules.account.Account;
import com.sangwontest.studyolle.modules.study.form.StudyDescriptionForm;
import com.sangwontest.studyolle.modules.study.form.StudyForm;
import com.sangwontest.studyolle.modules.study.form.StudyPathForm;
import com.sangwontest.studyolle.modules.account.Tags;
import com.sangwontest.studyolle.modules.zone.Zones;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Study createNewStudy(StudyForm studyForm, Account account) {
        Study study = new Study();
        study.addManager(account);
        study.createNewStudy(studyForm);

        return studyRepository.save(study);
    }

    public Study getStudyToUpdate(Account account, String path){
        Study study = this.getStudy(path);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudy(String path) {
        Study study = this.studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);

        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        study.updateDescription(studyDescriptionForm);
    }

    public void updateStudyImage(Study study, String image) {
        study.updateImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setBanner(false);
    }

    public Study getStudyToUpdateTags(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path); // WithTags라는 것은 jpa에서 무시가 된다. 즉 findByPath와 같은 쿼리가 발생함
        //이렇게 사용하는 이유는 다른 엔티티 그래프를 사용하기 위해서 이름을 조금 다르게 지정해줌
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;

    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public void addTag(Study study, Tags tags) {
        study.getTags().add(tags);
//        studyRepository.save(study);
    }

    public void removeTag(Study study, Tags tags) {
        study.getTags().remove(tags);
    }

    public void addZone(Study study, Zones zones) {
        study.getZones().add(zones);
    }

    public void removeZone(Study study, Zones zones) {
        study.getZones().remove(zones);
    }

    private void checkIfManager(Account account, Study study) {
        if (!study.isManagerOf(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void publish(Study study) {
        study.publish();
    }

    public void close(Study study) {
        study.close();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
//        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 종료합니다."));
    }

    public void startRecruit(Study study) {
        study.startRecruit();
//        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 시작합니다."));
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void changeTitle(Study study, String newTitle) {
        study.changeTitle(newTitle);
    }

    public void changePath(Study study, StudyPathForm studyPathForm) {
        study.changePath(studyPathForm.getPath());
    }

    public void remove(Study study) {
        if(study.isRemovable()){
            studyRepository.delete(study);
        } else{
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Study study, Account account) {
        study.addMember(account);
    }

    public void leaveMember(Study study, Account account) {
        study.removeMember(account);
    }

    public Study getStudyToEnroll(String path) {
        //관리자 권한이 아닌사람도 가져올수 잇어야함
        Study study = studyRepository.findStudyOnlyByPath(path);
        checkIfExistingStudy(path, study);

        return study;
    }
}
