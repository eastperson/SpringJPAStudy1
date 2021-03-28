package com.ep.studyplatform.modules.study.event;

import com.ep.studyplatform.infra.config.AppProperties;
import com.ep.studyplatform.infra.mail.EmailMessage;
import com.ep.studyplatform.infra.mail.EmailService;
import com.ep.studyplatform.modules.account.Account;
import com.ep.studyplatform.modules.account.AccountPredicates;
import com.ep.studyplatform.modules.account.AccountRepository;
import com.ep.studyplatform.modules.notification.Notification;
import com.ep.studyplatform.modules.notification.NotificationRepository;
import com.ep.studyplatform.modules.notification.NotificationType;
import com.ep.studyplatform.modules.study.Study;
import com.ep.studyplatform.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Slf4j
@Async // 메서드에서 사용해도 된다.
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent){
        // 스터디 정보가 들어있긴 하지만, 스터디 정보를 사용할 수 없다.
        // 스터디 객체는 detached 상태이기 때문이다.
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(),study.getZones()));

        log.info("created event");
        accounts.forEach(account -> {
            log.info("account email : "+account.getEmail());
            log.info("account : " + account);
            if(account.isStudyCreatedByEmail()){
                sendStudyCreatedEmail(study, account,"새로운 스터디가 있습니다.","스터디플랫폼, '" + study.getTitle() +"' 스터디가 생겼습니다.");
            }
            if(account.isStudyCreatedByWeb()){
                saveStudyCreatedNotification(study, account,study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });

    }

    @EventListener
    public void handleStudyUpdateEven(StudyUpdateEvent studyUpdateEvent){
        Study study = studyRepository.findStudyWithMangersAndMembersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getMembers());
        accounts.addAll(study.getManagers());

        accounts.forEach(account -> {
            if(account.isStudyCreatedByEmail()){
                sendStudyCreatedEmail(study,account,studyUpdateEvent.getMessage(),"스터디플랫폼, '"+study.getTitle()+"'스터디에 새 소식이 있습니다.");
            }
            if(account.isStudyCreatedByWeb()){
                saveStudyCreatedNotification(study,account,studyUpdateEvent.getMessage(),NotificationType.STUDY_UPDATED);
            }
        });
    }

    private void saveStudyCreatedNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendStudyCreatedEmail(Study study, Account account,String contextMessage, String subjectMessage) {
        Context context = new Context();
        context.setVariable("link","/study/" + study.getEncodedPath());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message",contextMessage);
        context.setVariable("host",appProperties.getHost());
        String message = templateEngine.process("mail/simple-link",context);
        EmailMessage emailMessage = EmailMessage.builder()
                .subject(subjectMessage)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}
