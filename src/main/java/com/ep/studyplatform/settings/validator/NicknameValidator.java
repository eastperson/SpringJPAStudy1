package com.ep.studyplatform.settings.validator;

import com.ep.studyplatform.account.AccountRepository;
import com.ep.studyplatform.domain.Account;
import com.ep.studyplatform.settings.form.NicknameForm;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {

        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;
        Account byNickName = accountRepository.findByNickname(nicknameForm.getNickname());
        if(byNickName != null) {
            errors.rejectValue("nickname","wrong.value","입력하신 닉네임은 사용하실 수 없습니다.");
        }

    }
}
