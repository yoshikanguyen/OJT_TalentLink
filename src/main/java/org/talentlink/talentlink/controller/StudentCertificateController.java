package org.talentlink.talentlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.talentlink.talentlink.dto.StudentCertificateForm;
import org.talentlink.talentlink.service.ProfileService;
import org.talentlink.talentlink.service.StudentCertificateService;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/student/my-certificates")
@RequiredArgsConstructor
public class StudentCertificateController {

    private final StudentCertificateService certificateService;
    private final ProfileService profileService;

    @GetMapping
    public String listCertificates(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long studentProfileId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());
        model.addAttribute("certificates", certificateService.getCertificatesByStudentProfileId(studentProfileId));
        if (!model.containsAttribute("certificateForm")) {
            model.addAttribute("certificateForm", new StudentCertificateForm());
        }
        return "student/my-certificates";
    }

    @PostMapping
    public String createCertificate(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @ModelAttribute("certificateForm") StudentCertificateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long studentProfileId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("certificates", certificateService.getCertificatesByStudentProfileId(studentProfileId));
            return "student/my-certificates";
        }

        try {
            certificateService.createCertificate(studentProfileId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm chứng chỉ và tải file minh chứng thành công.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("certificates", certificateService.getCertificatesByStudentProfileId(studentProfileId));
            model.addAttribute("errorMessage", ex.getMessage());
            return "student/my-certificates";
        }

        return "redirect:/student/my-certificates";
    }

    @PostMapping("/{id}/delete")
    public String deleteCertificate(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        Long studentProfileId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());
        try {
            certificateService.deleteCertificate(id, studentProfileId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xoá chứng chỉ và file minh chứng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/my-certificates";
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> viewOrDownloadCertificateFile(
            @PathVariable Long id,
            @RequestParam(name = "download", defaultValue = "false") boolean download,
            Authentication authentication
    ) throws MalformedURLException {
        StudentCertificateService.CertificateFileDownload item =
                certificateService.getCertificateFileForViewer(id, authentication);

        Resource resource = new UrlResource(item.filePath().toUri());
        MediaType mediaType = MediaType.parseMediaType(item.certificate().getContentType());
        String dispositionType = download ? "attachment" : "inline";
        String encodedName = UriUtils.encode(item.certificate().getOriginalFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename*=UTF-8''" + encodedName)
                .body(resource);
    }
}
