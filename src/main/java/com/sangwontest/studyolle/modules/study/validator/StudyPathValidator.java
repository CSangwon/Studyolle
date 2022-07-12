package com.sangwontest.studyolle.modules.study.validator;

import com.sangwontest.studyolle.modules.study.StudyRepository;
import com.sangwontest.studyolle.modules.study.form.StudyPathForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyPathValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(StudyPathForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyPathForm studyPathForm = (StudyPathForm) target;
        if (studyRepository.existsByPath(studyPathForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "입력하신 URL을 사용하실 수 없습니다.");
        }

    }
}
