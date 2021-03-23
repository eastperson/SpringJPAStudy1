package com.ep.studyplatform.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {

        Account eastperson = new Account();
        eastperson.setNickname(nickname);
        eastperson.setEmail(nickname+"@email.com");
        accountRepository.save(eastperson);

        return eastperson;
    }

}
