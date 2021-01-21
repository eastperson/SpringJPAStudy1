package com.ep.studyplatform.settings.validator;

import com.ep.studyplatform.settings.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PasswordFormValidator implements Validator {

    // 어떤 종류의 객체를 검증할 것이냐.
    @Override
    public boolean supports(Class<?> clazz) {
        // 할당 가능한지
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
            errors.rejectValue("newPassword","wrong.value","입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
