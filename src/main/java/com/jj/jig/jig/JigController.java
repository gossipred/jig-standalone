package com.jj.jig.jig;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class JigController {

    private final JigService jigService;
    private final JigCsvExportService jigCsvExportService;

    public JigController(JigService jigService, JigCsvExportService jigCsvExportService) {
        this.jigService = jigService;
        this.jigCsvExportService = jigCsvExportService;
    }

    @GetMapping("/jigs")
    public String list(@RequestParam(name = "q", required = false) String keyword, Model model, Authentication authentication) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("jigs", jigService.findJigs(keyword));
        model.addAttribute("currentUsername", authentication == null ? null : authentication.getName());
        model.addAttribute("canManageAllJigs", hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_SUPERVISOR"));
        return "jigs/list";
    }

    @GetMapping("/jigs/export")
    public ResponseEntity<byte[]> export(@RequestParam(name = "q", required = false) String keyword) {
        byte[] csv = jigCsvExportService.export(jigService.findJigs(keyword));
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("jigs.csv", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(csv);
    }

    @GetMapping("/jigs/next-no")
    public ResponseEntity<String> nextJigNo(@RequestParam(name = "prefix", defaultValue = "AM-ME") String prefix) {
        try {
            return ResponseEntity.ok(jigService.nextJigNo(prefix));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/jigs/new")
    public String createForm(Model model, Authentication authentication) {
        JigForm form = new JigForm();
        form.setJigNo(jigService.nextJigNo("AM-ME"));
        applyCurrentDri(form, authentication);
        model.addAttribute("jigForm", form);
        addFormOptions(model, "Add Jig / Tooling", "新增治具/模具", "/jigs/new", null, authentication);
        return "jigs/form";
    }

    @PostMapping("/jigs/new")
    public String create(
            @Valid @ModelAttribute("jigForm") JigForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ) {
        applyCurrentDri(form, authentication);
        if (bindingResult.hasErrors()) {
            addFormOptions(model, "Add Jig / Tooling", "新增治具/模具", "/jigs/new", null, authentication);
            return "jigs/form";
        }

        assertScrapRequiresAdmin(form.getStatus(), authentication);
        try {
            Jig jig = jigService.create(form, authentication == null ? null : authentication.getName());
            redirectAttributes.addFlashAttribute("message", "Jig created: " + jig.getJigNo());
            return "redirect:/jigs";
        } catch (IllegalArgumentException ex) {
            rejectFormError(bindingResult, ex);
            addFormOptions(model, "Add Jig / Tooling", "新增治具/模具", "/jigs/new", null, authentication);
            return "jigs/form";
        }
    }

    @GetMapping("/jigs/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {
        Jig jig = jigService.findById(id);
        assertCanModifyJig(jig, authentication);
        JigForm form = JigForm.from(jig);
        applyCurrentDri(form, authentication);
        model.addAttribute("jigForm", form);
        addFormOptions(model, "Edit Jig / Tooling", "修改治具/模具", "/jigs/" + id + "/edit", id, authentication);
        return "jigs/form";
    }

    @GetMapping("/jigs/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Jig jig = jigService.findById(id);
        JigStatusUpdateForm statusForm = new JigStatusUpdateForm();
        statusForm.setStatus(jig.getStatus());
        JigDueDateUpdateForm dueDateForm = new JigDueDateUpdateForm();
        dueDateForm.setDueDate(jig.getDueDate());

        model.addAttribute("jig", jig);
        model.addAttribute("statusForm", statusForm);
        model.addAttribute("dueDateForm", dueDateForm);
        model.addAttribute("statuses", statusOptions(authentication));
        model.addAttribute("currentFiles", jigService.findFiles(id));
        model.addAttribute("logs", jigService.findLogs(id));
        model.addAttribute("canModifyJig", canModifyJig(jig, authentication));
        return "jigs/detail";
    }

    @PostMapping("/jigs/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @Valid @ModelAttribute("statusForm") JigStatusUpdateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal,
            Authentication authentication
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Status update failed. Please check the input.");
            return "redirect:/jigs/" + id;
        }

        assertScrapRequiresAdmin(form.getStatus(), authentication);
        assertCanModifyJig(jigService.findById(id), authentication);
        jigService.updateStatus(id, form.getStatus(), form.getNote(), principal == null ? null : principal.getName());
        redirectAttributes.addFlashAttribute("message", "Status updated.");
        return "redirect:/jigs/" + id;
    }

    @PostMapping("/jigs/{id}/due-date")
    public String updateDueDate(
            @PathVariable Long id,
            @Valid @ModelAttribute("dueDateForm") JigDueDateUpdateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal,
            Authentication authentication
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Due Date update failed. Please check the input.");
            return "redirect:/jigs/" + id;
        }

        assertCanModifyJig(jigService.findById(id), authentication);
        jigService.updateDueDate(id, form.getDueDate(), form.getNote(), principal == null ? null : principal.getName());
        redirectAttributes.addFlashAttribute("message", "Due Date updated.");
        return "redirect:/jigs/" + id;
    }

    @PostMapping("/jigs/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("jigForm") JigForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ) {
        applyCurrentDri(form, authentication);
        if (bindingResult.hasErrors()) {
            addFormOptions(model, "Edit Jig / Tooling", "修改治具/模具", "/jigs/" + id + "/edit", id, authentication);
            return "jigs/form";
        }

        assertScrapRequiresAdmin(form.getStatus(), authentication);
        assertCanModifyJig(jigService.findById(id), authentication);
        try {
            Jig jig = jigService.update(id, form, authentication == null ? null : authentication.getName());
            redirectAttributes.addFlashAttribute("message", "Jig updated: " + jig.getJigNo());
            return "redirect:/jigs";
        } catch (IllegalArgumentException ex) {
            rejectFormError(bindingResult, ex);
            addFormOptions(model, "Edit Jig / Tooling", "修改治具/模具", "/jigs/" + id + "/edit", id, authentication);
            return "jigs/form";
        }
    }

    @GetMapping("/jigs/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        JigFile jigFile = jigService.findFileById(fileId);
        Resource resource = jigService.loadFileResource(jigFile);

        String contentType = jigFile.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : jigFile.getContentType();

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(jigFile.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @PostMapping("/jigs/files/{fileId}/delete")
    public String deleteFile(@PathVariable Long fileId, RedirectAttributes redirectAttributes, Principal principal) {
        Long jigId = jigService.deleteFile(fileId, principal == null ? null : principal.getName());
        redirectAttributes.addFlashAttribute("message", "File deleted.");
        return "redirect:/jigs/" + jigId + "/edit";
    }

    @PostMapping("/jigs/files/{fileId}/replace")
    public String replaceFile(
            @PathVariable Long fileId,
            @RequestParam("replacementFile") MultipartFile replacementFile,
            RedirectAttributes redirectAttributes,
            Principal principal,
            Authentication authentication
    ) {
        try {
            JigFile existingFile = jigService.findFileById(fileId);
            assertCanModifyJig(existingFile.getJig(), authentication);
            Long jigId = jigService.replaceFile(fileId, replacementFile, principal == null ? null : principal.getName());
            redirectAttributes.addFlashAttribute("message", "File replaced.");
            return "redirect:/jigs/" + jigId + "/edit";
        } catch (IllegalArgumentException ex) {
            JigFile jigFile = jigService.findFileById(fileId);
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return "redirect:/jigs/" + jigFile.getJig().getId() + "/edit";
        }
    }

    @PostMapping("/jigs/{id}/delete")
    public String deleteJig(@PathVariable Long id, RedirectAttributes redirectAttributes, Principal principal) {
        jigService.deleteJig(id, principal == null ? null : principal.getName());
        redirectAttributes.addFlashAttribute("message", "Jig deleted.");
        return "redirect:/jigs";
    }

    private void addFormOptions(Model model, String title, String titleZh, String action, Long jigId, Authentication authentication) {
        model.addAttribute("title", title);
        model.addAttribute("titleZh", titleZh);
        model.addAttribute("action", action);
        model.addAttribute("statuses", statusOptions(authentication));
        model.addAttribute("currentFiles", jigId == null ? java.util.List.of() : jigService.findFiles(jigId));
    }

    private void applyCurrentDri(JigForm form, Authentication authentication) {
        if (authentication != null) {
            form.setDri(authentication.getName());
        }
    }

    private List<JigStatus> statusOptions(Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return Arrays.asList(JigStatus.values());
        }
        return Arrays.stream(JigStatus.values())
                .filter(status -> status != JigStatus.Scrap)
                .toList();
    }

    private void assertScrapRequiresAdmin(JigStatus status, Authentication authentication) {
        if (status == JigStatus.Scrap && !hasRole(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Only ADMIN can scrap a jig.");
        }
    }

    private void assertCanModifyJig(Jig jig, Authentication authentication) {
        if (canModifyJig(jig, authentication)) {
            return;
        }
        throw new AccessDeniedException("You can only edit jigs that you created.");
    }

    private boolean canModifyJig(Jig jig, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_SUPERVISOR")) {
            return true;
        }
        if (hasRole(authentication, "ROLE_ENGINEER")
                && jig.getCreatedBy() != null
                && authentication != null
                && jig.getCreatedBy().getUsername().equals(authentication.getName())) {
            return true;
        }
        return false;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private void rejectFormError(BindingResult bindingResult, IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message != null && message.toLowerCase().contains("file")) {
            bindingResult.rejectValue("jigFiles", "jigFiles.invalid", message);
            return;
        }
        bindingResult.rejectValue("jigNo", "jigNo.invalid", message);
    }
}
