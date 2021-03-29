package com.ep.studyplatform.modules.main;


import com.ep.studyplatform.modules.account.Account;
import com.ep.studyplatform.modules.account.AccountRepository;
import com.ep.studyplatform.modules.account.CurrentUser;
import com.ep.studyplatform.modules.event.EnrollmentRepository;
import com.ep.studyplatform.modules.study.Study;
import com.ep.studyplatform.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));
            model.addAttribute("studyList", studyRepository.findByAccount(
                    accountLoaded.getTags(),
                    accountLoaded.getZones()));
            model.addAttribute("studyManagerOf",
                    studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            model.addAttribute("studyMemberOf",
                    studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            return "index-after-login";
        }

        model.addAttribute("studyList", studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/search/study")
    public String searchStudy(@PageableDefault(size = 9,page = 0
            ,sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable, String keyword, Model model) {
        // Pageable은 size, page, sort를 담고 있다.
        Page<Study> studyPage = studyRepository.findByKeyword(keyword,pageable);
        // 컬렉션의 경우 이름을 주지 않으면 화면에서 에러가 날 수 있다.
        model.addAttribute("studyPage",studyPage);
        model.addAttribute("keyword",keyword);
        model.addAttribute("sortProperty",pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }

}
