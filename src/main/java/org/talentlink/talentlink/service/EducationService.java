package org.talentlink.talentlink.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.dto.EducationForm;
import org.talentlink.talentlink.entity.Education;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.EducationRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EducationService {

    private final EducationRepository educationRepository;
    private final ProfileService profileService;

    public EducationService(EducationRepository educationRepository, ProfileService profileService) {
        this.educationRepository = educationRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<Education> getEducationsForUser(String email) {
        User currentUser = profileService.getCurrentUserByEmail(email);
        return educationRepository.findByUserIdOrderByStartDateDescIdDesc(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Education getOwnedEducation(String email, Long educationId) {
        User currentUser = profileService.getCurrentUserByEmail(email);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thông tin học vấn."));

        if (education.getUser() == null || !education.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên học vấn của người khác.");
        }
        return education;
    }

    @Transactional(readOnly = true)
    public EducationForm getEducationForm(String email, Long educationId) {
        Education education = getOwnedEducation(email, educationId);
        EducationForm form = new EducationForm();
        form.setSchoolName(education.getSchoolName());
        form.setMajor(education.getMajor());
        form.setStartDate(education.getStartDate());
        form.setEndDate(education.getEndDate());
        form.setGpa(education.getGpa());
        return form;
    }

    @Transactional
    public void createEducation(String email, EducationForm form) {
        User currentUser = profileService.getCurrentUserByEmail(email);
        Education education = new Education();
        education.setUser(currentUser);
        mapFormToEntity(form, education);
        educationRepository.save(education);
    }

    @Transactional
    public void updateEducation(String email, Long educationId, EducationForm form) {
        Education education = getOwnedEducation(email, educationId);
        mapFormToEntity(form, education);
        educationRepository.save(education);
    }

    @Transactional
    public void deleteEducation(String email, Long educationId) {
        Education education = getOwnedEducation(email, educationId);
        educationRepository.delete(education);
    }

    private void mapFormToEntity(EducationForm form, Education education) {
        education.setSchoolName(form.getSchoolName().trim());
        education.setMajor(form.getMajor().trim());
        education.setStartDate(form.getStartDate());
        education.setEndDate(form.getEndDate());
        education.setGpa(form.getGpa());
    }
}
