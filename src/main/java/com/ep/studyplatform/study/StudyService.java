package com.ep.studyplatform.study;

import com.ep.studyplatform.domain.Account;
import com.ep.studyplatform.domain.Study;
import com.ep.studyplatform.domain.Tag;
import com.ep.studyplatform.domain.Zone;
import com.ep.studyplatform.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    public Study createNewStsudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }


    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);
        checkIfManager(account,study);
        return study;
    }

    public Study getStudy(String path) {
        Study study = this.studyRepository.findByPath(path);
        if(study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm){
        modelMapper.map(studyDescriptionForm,study);
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }
    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }
    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    private void checkIfManager(Account account, Study study) {
        if(!account.isManagerOf(study)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findAccountWithTagsByPath(path);
        checkIfExistingStudy(path,study);
        checkIfManager(account,study);
        return study;
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findAccountWithZonesByPath(path);
        checkIfExistingStudy(path,study);
        checkIfManager(account,study);
        return study;

    }

    private void checkIfExistingStudy(String path, Study study) {
        if(study == null) {
            throw new IllegalArgumentException(path +"에 해당하는 스터디가 없습니다.");
        }
    }
}
