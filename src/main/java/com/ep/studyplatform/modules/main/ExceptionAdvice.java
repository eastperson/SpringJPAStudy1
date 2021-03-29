package com.ep.studyplatform.modules.main;

import com.ep.studyplatform.modules.account.Account;
import com.ep.studyplatform.modules.account.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest request, RuntimeException e){
        log.info("[==============================================]");
        if(account != null){
            log.info("'{}' requested '{}'",account.getNickname(), request.getRequestURI());
        } else {
            log.info("requested '{}'",request.getRequestURI());
            // 레퍼럴 체크도 하자
        }
        log.error("bad request", e);
        return "error";
    }

}
