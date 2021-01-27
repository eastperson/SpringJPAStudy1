package com.ep.studyplatform.modules.study;

import com.ep.studyplatform.modules.account.Account;
import com.ep.studyplatform.modules.account.CurrentUser;
import com.ep.studyplatform.modules.account.form.TagForm;
import com.ep.studyplatform.modules.account.form.ZoneForm;
import com.ep.studyplatform.modules.study.form.StudyDescriptionForm;
import com.ep.studyplatform.modules.tag.Tag;
import com.ep.studyplatform.modules.tag.TagRepository;
import com.ep.studyplatform.modules.tag.TagService;
import com.ep.studyplatform.modules.zone.Zone;
import com.ep.studyplatform.modules.zone.ZoneRepository;
import com.ep.studyplatform.modules.zone.ZoneService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/{path}/settings")
@Log4j2
public class StudySettingController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;
    private final TagService tagService;
    private final ZoneService zoneService;

    @GetMapping("/description")
    public String updateDescription(@CurrentUser Account account, @PathVariable String path, Model model){

        // 계정확인을 위해 service로 뺐다.
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study,StudyDescriptionForm.class));

        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateDescription(@CurrentUser Account account, @PathVariable String path, @Valid StudyDescriptionForm studyDescriptionForm
                                    , Errors errors, Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdate(account,path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study,studyDescriptionForm);
        attributes.addFlashAttribute("message","스터디 소개를 수정했습니다.");

        return "redirect:/study/" + study.getEncodedPath() + "/settings/description";
    }

    @GetMapping("/banner")
    public String updateImageForm(@CurrentUser Account account, @PathVariable String path, Model model){

        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String studyImageSubmit(@CurrentUser Account account,@PathVariable String path, String image, RedirectAttributes attributes) {

        Study study = studyService.getStudyToUpdate(account,path);
        studyService.updateStudyImage(study,image);
        attributes.addFlashAttribute("message","스터디 이미지를 수정했습니다.");
        return "redirect:/study/"+study.getEncodedPath()+"/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account,@PathVariable String path) {

        Study study = studyService.getStudyToUpdate(account,path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/"+study.getEncodedPath()+"/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account,@PathVariable String path) {

        Study study = studyService.getStudyToUpdate(account,path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/"+study.getEncodedPath()+"/settings/banner";
    }

    @GetMapping("/tags")
    public String StudyTags(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {


        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));

        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    public ResponseEntity StudyAddTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm, Model model) {
        String title = tagForm.getTagTitle();

        log.info("post.....................add tag title : "+title);
        Tag tag = tagService.findOrCreateNew(title);

        Study study = studyService.getStudyToUpdateTag(account,path);

        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    public ResponseEntity StudyRemoveTag(@CurrentUser Account account, @PathVariable String path,@RequestBody TagForm tagForm, Model model) {

        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);
        if(tag == null) {
            return ResponseEntity.badRequest().build();
        }

        Study study = studyService.getStudyToUpdateTag(account,path);

        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String StudyZones(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(study);

        Set<Zone> zones = study.getZones();
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allZones));

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    public ResponseEntity StudyAddZone(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm, Model model) {

        Zone zone = zoneService.findOrCreateNew(zoneForm.getCityName(),zoneForm.getProvinceName());
        Study study = studyService.getStudyToUpdateZone(account,path);
        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    public ResponseEntity StudyRemoveZone(@CurrentUser Account account, @PathVariable String path,@RequestBody ZoneForm zoneForm, Model model) {

        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }
        Study study = studyService.getStudyToUpdateZone(account,path);
        studyService.removeZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studySettingForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes,Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.publish(study);
        attributes.addFlashAttribute("message","스터디를 공개했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes,Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.close(study);
        attributes.addFlashAttribute("message","스터디를 종료했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("/recurit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
        }
        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdate(account,path);
        if(!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";

    }

    // 보통 form object를 만들어서 사용했는데 다양한 방법을 보여주기 위해서 사용을 안했다. 보통은 일관적으로 작성한다.
    // validator가 없는 경우에 직접 vailidate를 만들어주기도 한다.
    @PostMapping("/study/path")
    public String updateStudyPath(@CurrentUser Account account, @PathVariable String path, @RequestParam String newPath,
                                  Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "해당 스터디 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
            return "study/settings/study";
        }
        studyService.updateStudyPath(study,newPath);
        attributes.addFlashAttribute("message","스터디 이름을 수정했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("/study/title")
    public String updateStudyTitle(@CurrentUser Account account, @PathVariable String path, @RequestParam String newTitle, Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "스터디 이름을 다시 입력하세요.");
            return "study/settings/study";
        }
        studyService.updateStudyTitle(study,newTitle);
        attributes.addFlashAttribute("message","스터디 이름을 수정했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("/study/remove")
    public String removeStudy(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.remove(study);
        return "redirect:/";
    }

//   private String getPath(String path) {
//        return URLEncoder.encode(path, StandardCharsets.UTF_8);
//    }


}
