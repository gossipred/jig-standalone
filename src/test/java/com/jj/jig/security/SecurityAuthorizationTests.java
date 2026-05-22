package com.jj.jig.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jj.jig.jig.Jig;
import com.jj.jig.jig.JigFile;
import com.jj.jig.jig.JigFileRepository;
import com.jj.jig.jig.JigNumber;
import com.jj.jig.jig.JigRepository;
import com.jj.jig.jig.JigStatus;
import com.jj.jig.log.JigLogActionType;
import com.jj.jig.log.JigLogRepository;
import com.jj.jig.user.User;
import com.jj.jig.user.UserLogActionType;
import com.jj.jig.user.UserLogRepository;
import com.jj.jig.user.UserRepository;
import com.jj.jig.user.UserRole;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JigRepository jigRepository;

    @Autowired
    private JigFileRepository jigFileRepository;

    @Autowired
    private JigLogRepository jigLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLogRepository userLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanOpenUserManagement() throws Exception {
        mockMvc.perform(get("/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void engineerCannotOpenUserManagement() throws Exception {
        mockMvc.perform(get("/admin/users").with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anyLoggedInUserCanOpenOwnPasswordForm() throws Exception {
        mockMvc.perform(get("/account/password").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Change Password")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("readonly")));
    }

    @Test
    void userCanChangeOwnPasswordAndAuditLogIsRecorded() throws Exception {
        saveUser("password_engineer", UserRole.ENGINEER);

        mockMvc.perform(post("/account/password")
                        .with(user("password_engineer").roles("ENGINEER"))
                        .with(csrf())
                        .param("currentPassword", "123456")
                        .param("newPassword", "abc12345")
                        .param("confirmPassword", "abc12345"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        User updatedUser = userRepository.findByUsername("password_engineer").orElseThrow();
        assertThat(passwordEncoder.matches("abc12345", updatedUser.getPassword())).isTrue();
        assertThat(userLogRepository.findAll())
                .anySatisfy(log -> {
                    assertThat(log.getTargetUser().getUsername()).isEqualTo("password_engineer");
                    assertThat(log.getActorUser().getUsername()).isEqualTo("password_engineer");
                    assertThat(log.getActionType()).isEqualTo(UserLogActionType.PASSWORD_CHANGE);
                });
    }

    @Test
    void userCannotChangeUsernameFromPasswordForm() throws Exception {
        saveUser("locked_name", UserRole.OPERATOR);

        mockMvc.perform(post("/account/password")
                        .with(user("locked_name").roles("OPERATOR"))
                        .with(csrf())
                        .param("username", "spoofed_name")
                        .param("currentPassword", "123456")
                        .param("newPassword", "abc12345")
                        .param("confirmPassword", "abc12345"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        assertThat(userRepository.findByUsername("locked_name")).isPresent();
        assertThat(userRepository.findByUsername("spoofed_name")).isEmpty();
    }

    @Test
    void wrongCurrentPasswordReturnsPasswordError() throws Exception {
        saveUser("wrong_password_user", UserRole.OPERATOR);

        mockMvc.perform(post("/account/password")
                        .with(user("wrong_password_user").roles("OPERATOR"))
                        .with(csrf())
                        .param("currentPassword", "wrong")
                        .param("newPassword", "abc12345")
                        .param("confirmPassword", "abc12345"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Current password is incorrect.")));
    }

    @Test
    void adminUserListShowsAccountChangeLogs() throws Exception {
        saveUser("admin", UserRole.ADMIN);
        User target = saveUser("logged_user", UserRole.OPERATOR);
        userLogRepository.save(userLog(target, userRepository.findByUsername("admin").orElseThrow(), UserLogActionType.PASSWORD_RESET));

        mockMvc.perform(get("/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Account Change Logs")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PASSWORD_RESET")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("logged_user")));
    }

    @Test
    void adminEditDoesNotChangeUsernameAndRecordsPasswordReset() throws Exception {
        saveUser("admin", UserRole.ADMIN);
        User target = saveUser("fixed_account", UserRole.OPERATOR);

        mockMvc.perform(post("/admin/users/{id}/edit", target.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("username", "changed_account")
                        .param("password", "abc12345")
                        .param("role", "ENGINEER")
                        .param("enabled", "true"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/users"));

        User updatedUser = userRepository.findById(target.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("fixed_account");
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.ENGINEER);
        assertThat(userRepository.findByUsername("changed_account")).isEmpty();
        assertThat(userLogRepository.findAll())
                .extracting(log -> log.getActionType())
                .contains(UserLogActionType.ROLE_CHANGE, UserLogActionType.PASSWORD_RESET);
    }

    @Test
    void operatorCannotCreateJigs() throws Exception {
        mockMvc.perform(get("/jigs/new").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void engineerCanCreateJigs() throws Exception {
        mockMvc.perform(get("/jigs/new").with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Add Jig / Tooling")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("On Process / 在線使用中")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Maintain / 保養")));
    }

    @Test
    void newJigFormShowsNextJigNo() throws Exception {
        saveJig("AM-ME-916");

        mockMvc.perform(get("/jigs/new").with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"jigNo\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"AM-ME-")));
    }

    @Test
    void newJigFormAutoFillsDriFromLoginAccount() throws Exception {
        mockMvc.perform(get("/jigs/new").with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"dri\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"engineer\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("readonly")));
    }

    @Test
    void nextJigNoUsesFlexiblePrefixAndHighestSequence() throws Exception {
        saveJig("BM-QA-004");
        saveJig("BM-QA-007-02");

        mockMvc.perform(get("/jigs/next-no")
                        .param("prefix", "bm-qa")
                        .with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isOk())
                .andExpect(content().string("BM-QA-008"));
    }

    @Test
    void nextJigNoRejectsInvalidPrefix() throws Exception {
        mockMvc.perform(get("/jigs/next-no")
                        .param("prefix", "bad")
                        .with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Jig No. prefix must be like AM-ME."));
    }

    @Test
    void engineerCanOpenJigEditForm() throws Exception {
        Jig jig = saveJig("AM-ME-901");

        mockMvc.perform(get("/jigs/{id}/edit", jig.getId()).with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isOk());
    }

    @Test
    void engineerEditFormShowsExistingDates() throws Exception {
        Jig jig = saveJig("AM-ME-907");

        mockMvc.perform(get("/jigs/{id}/edit", jig.getId()).with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"startDate\" type=\"date\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"2024-11-26\"")));
    }

    @Test
    void operatorCannotOpenJigEditForm() throws Exception {
        Jig jig = saveJig("AM-ME-902");

        mockMvc.perform(get("/jigs/{id}/edit", jig.getId()).with(user("operator").roles("OPERATOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void engineerCanUpdateJigContent() throws Exception {
        Jig jig = saveJig("AM-ME-903");

        mockMvc.perform(post("/jigs/{id}/edit", jig.getId())
                        .with(user("engineer").roles("ENGINEER"))
                        .with(csrf())
                        .param("classification", "Mouse")
                        .param("modelName", "Updated Mouse")
                        .param("jigName", "Updated Jig")
                        .param("customer", "Demo Customer")
                        .param("jigNo", jig.getJigNo())
                        .param("assemblyLine", "Line 1")
                        .param("quantity", "1")
                        .param("status", "Repair")
                        .param("dri", "Spoofed User")
                        .param("mroNo", "MRO-1")
                        .param("prNo", "PR-1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jigs"));

        assertThat(jigRepository.findById(jig.getId()).orElseThrow().getDri()).isEqualTo("engineer");
    }

    @Test
    void editJigStatusChangeWritesOldAndNewStatusLog() throws Exception {
        Jig jig = saveJig("AM-ME-931", "engineer", JigStatus.Normal);

        mockMvc.perform(post("/jigs/{id}/edit", jig.getId())
                        .with(user("engineer").roles("ENGINEER"))
                        .with(csrf())
                        .param("classification", "Mouse")
                        .param("modelName", "Updated Mouse")
                        .param("jigName", "Updated Jig")
                        .param("customer", "Demo Customer")
                        .param("jigNo", jig.getJigNo())
                        .param("assemblyLine", "Line 1")
                        .param("quantity", "1")
                        .param("status", "Hold")
                        .param("dri", "Spoofed User")
                        .param("mroNo", "MRO-1")
                        .param("prNo", "PR-1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jigs"));

        assertThat(jigLogRepository.findByJigIdOrderByCreatedAtDesc(jig.getId()))
                .anySatisfy(log -> {
                    assertThat(log.getActionType()).isEqualTo(JigLogActionType.STATUS_CHANGE);
                    assertThat(log.getOldStatus()).isEqualTo(JigStatus.Normal);
                    assertThat(log.getNewStatus()).isEqualTo(JigStatus.Hold);
                });
        assertThat(jigLogRepository.findByJigIdOrderByCreatedAtDesc(jig.getId()))
                .noneSatisfy(log -> assertThat(log.getActionType()).isEqualTo(JigLogActionType.UPDATE));
    }

    @Test
    void editJigDueDateChangeWritesOldAndNewDueDateLog() throws Exception {
        Jig jig = saveJig("AM-ME-932", "engineer", JigStatus.Normal);

        mockMvc.perform(post("/jigs/{id}/edit", jig.getId())
                        .with(user("engineer").roles("ENGINEER"))
                        .with(csrf())
                        .param("classification", "Mouse")
                        .param("modelName", "Updated Mouse")
                        .param("jigName", "Updated Jig")
                        .param("customer", "Demo Customer")
                        .param("jigNo", jig.getJigNo())
                        .param("assemblyLine", "Line 1")
                        .param("quantity", "1")
                        .param("status", "Normal")
                        .param("dri", "Spoofed User")
                        .param("startDate", "2024-11-26")
                        .param("dueDate", "2026-06-30")
                        .param("mroNo", "MRO-1")
                        .param("prNo", "PR-1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jigs"));

        assertThat(jigLogRepository.findByJigIdOrderByCreatedAtDesc(jig.getId()))
                .anySatisfy(log -> {
                    assertThat(log.getActionType()).isEqualTo(JigLogActionType.DUE_DATE_CHANGE);
                    assertThat(log.getOldDueDate()).isEqualTo(LocalDate.of(2024, 11, 26));
                    assertThat(log.getNewDueDate()).isEqualTo(LocalDate.of(2026, 6, 30));
                });
        assertThat(jigLogRepository.findByJigIdOrderByCreatedAtDesc(jig.getId()))
                .noneSatisfy(log -> assertThat(log.getActionType()).isEqualTo(JigLogActionType.UPDATE));
    }

    @Test
    void engineerCannotOpenOtherUsersJigEditForm() throws Exception {
        Jig jig = saveJig("AM-ME-912", "other_engineer");

        mockMvc.perform(get("/jigs/{id}/edit", jig.getId()).with(user("engineer").roles("ENGINEER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void supervisorCanOpenOtherUsersJigEditForm() throws Exception {
        Jig jig = saveJig("AM-ME-913", "engineer");

        mockMvc.perform(get("/jigs/{id}/edit", jig.getId()).with(user("supervisor").roles("SUPERVISOR")))
                .andExpect(status().isOk());
    }

    @Test
    void operatorCannotUpdateJigContent() throws Exception {
        Jig jig = saveJig("AM-ME-904");

        mockMvc.perform(post("/jigs/{id}/edit", jig.getId())
                        .with(user("operator").roles("OPERATOR"))
                        .with(csrf())
                        .param("modelName", "Operator Update")
                        .param("jigName", "Should Not Save")
                        .param("jigNo", jig.getJigNo())
                        .param("quantity", "1")
                        .param("status", "Normal"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanScrapJigs() throws Exception {
        Jig jig = saveJig("AM-ME-905");

        mockMvc.perform(post("/jigs/{id}/status", jig.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("status", "Scrap")
                        .param("note", "Approved by admin"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jigs/" + jig.getId()));
    }

    @Test
    void adminCanDeleteJigFiles() throws Exception {
        Jig jig = saveJig("AM-ME-908");
        JigFile file = saveFile(jig, "admin-delete.pdf");

        mockMvc.perform(post("/jigs/files/{id}/delete", file.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jigs/" + jig.getId() + "/edit"));
    }

    @Test
    void engineerCannotDeleteJigFiles() throws Exception {
        Jig jig = saveJig("AM-ME-909");
        JigFile file = saveFile(jig, "engineer-delete.pdf");

        mockMvc.perform(post("/jigs/files/{id}/delete", file.getId())
                        .with(user("engineer").roles("ENGINEER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void engineerCanReplaceJigFiles() throws Exception {
        Jig jig = saveJig("AM-ME-910");
        JigFile file = saveFile(jig, "old-drawing.pdf");
        MockMultipartFile replacementFile = new MockMultipartFile(
                "replacementFile",
                "new-drawing.pdf",
                "application/pdf",
                "new drawing".getBytes()
        );

        mockMvc.perform(multipart("/jigs/files/{id}/replace", file.getId())
                        .file(replacementFile)
                        .with(user("engineer").roles("ENGINEER"))
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jigs/" + jig.getId() + "/edit"));
    }

    @Test
    void operatorCannotReplaceJigFiles() throws Exception {
        Jig jig = saveJig("AM-ME-911");
        JigFile file = saveFile(jig, "operator-replace.pdf");
        MockMultipartFile replacementFile = new MockMultipartFile(
                "replacementFile",
                "blocked.pdf",
                "application/pdf",
                "blocked".getBytes()
        );

        mockMvc.perform(multipart("/jigs/files/{id}/replace", file.getId())
                        .file(replacementFile)
                        .with(user("operator").roles("OPERATOR"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void supervisorCanDeleteJigs() throws Exception {
        Jig jig = saveJig("AM-ME-914", "engineer");

        mockMvc.perform(post("/jigs/{id}/delete", jig.getId())
                        .with(user("supervisor").roles("SUPERVISOR"))
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jigs"));
    }

    @Test
    void engineerCannotDeleteJigs() throws Exception {
        Jig jig = saveJig("AM-ME-915", "engineer");

        mockMvc.perform(post("/jigs/{id}/delete", jig.getId())
                        .with(user("engineer").roles("ENGINEER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void engineerCannotScrapJigs() throws Exception {
        Jig jig = saveJig("AM-ME-906");

        mockMvc.perform(post("/jigs/{id}/status", jig.getId())
                        .with(user("engineer").roles("ENGINEER"))
                        .with(csrf())
                        .param("status", "Scrap")
                        .param("note", "Need admin approval"))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCanOpenJigList() throws Exception {
        mockMvc.perform(get("/jigs").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk());
    }

    @Test
    void jigListLoadsSharedIndustrialUiStyles() throws Exception {
        mockMvc.perform(get("/jigs").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/app.css")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("app-industrial-bg")));
    }

    @Test
    void homeShowsUserWorkflowInsteadOfDevelopmentPhases() throws Exception {
        mockMvc.perform(get("/").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Jig &amp; Toolings Management System")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("治具及模具管理系統")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Daily Workflow")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Role Guide")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Phase 1"))));
    }

    @Test
    void loginPageUsesIndustrialBackground() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Jig &amp; Toolings Management System")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("治具及模具管理系統")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("industrial-jig-pattern.svg")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fixture control / maintenance records")));
    }

    @Test
    void jigListShowsStatusColorClass() throws Exception {
        saveJig("AM-ME-920", "engineer", JigStatus.Repair);

        mockMvc.perform(get("/jigs").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status-repair")));
    }

    @Test
    void jigListShowsNewStatusLabelsAndColorClasses() throws Exception {
        saveJig("AM-ME-927", "engineer", JigStatus.OnProcess);
        saveJig("AM-ME-928", "engineer", JigStatus.Maintain);

        mockMvc.perform(get("/jigs").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("On Process")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status-onprocess")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Maintain")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status-maintain")));
    }

    @Test
    void jigListShowsAssemblyLineAfterModel() throws Exception {
        saveJig("AM-ME-922", "engineer", JigStatus.Normal, "C542");

        mockMvc.perform(get("/jigs").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Classification")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ass'y Line")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("C542")));
    }

    @Test
    void jigListKeywordSearchIncludesClassification() throws Exception {
        saveJig("TM-ME-001", "engineer", JigStatus.Normal, "Line 7", "Injection Mold", "Tooling");

        mockMvc.perform(get("/jigs").param("q", "Tooling").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("TM-ME-001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tooling")));
    }

    @Test
    void jigListKeywordSearchIncludesAssemblyLine() throws Exception {
        saveJig("AM-ME-923", "engineer", JigStatus.Normal, "C542");

        mockMvc.perform(get("/jigs").param("q", "C542").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AM-ME-923")));
    }

    @Test
    void jigListKeywordSearchIncludesJigName() throws Exception {
        saveJig("AM-ME-924", "engineer", JigStatus.Normal, "C543", "Lens Press Jig");

        mockMvc.perform(get("/jigs").param("q", "Lens Press").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AM-ME-924")));
    }

    @Test
    void jigListKeywordSearchIncludesStatus() throws Exception {
        saveJig("AM-ME-925", "engineer", JigStatus.Hold, "C544");

        mockMvc.perform(get("/jigs").param("q", "Hold").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AM-ME-925")));
    }

    @Test
    void jigListKeywordSearchIncludesNewStatusLabels() throws Exception {
        saveJig("AM-ME-929", "engineer", JigStatus.OnProcess, "C546");
        saveJig("AM-ME-930", "engineer", JigStatus.Maintain, "C547");

        mockMvc.perform(get("/jigs").param("q", "On Process").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AM-ME-929")));

        mockMvc.perform(get("/jigs").param("q", "保養").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AM-ME-930")));
    }

    @Test
    void jigListKeywordSearchIncludesOwner() throws Exception {
        saveJig("AM-ME-926", "line_owner", JigStatus.Normal, "C545");

        mockMvc.perform(get("/jigs").param("q", "line_owner").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AM-ME-926")));
    }

    @Test
    void jigDetailShowsStatusColorClass() throws Exception {
        Jig jig = saveJig("AM-ME-921", "engineer", JigStatus.Hold);

        mockMvc.perform(get("/jigs/{id}", jig.getId()).with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status-hold")));
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/jigs"))
                .andExpect(status().isFound());
    }

    private Jig saveJig(String jigNo) {
        return saveJig(jigNo, "engineer");
    }

    private Jig saveJig(String jigNo, String ownerUsername) {
        return saveJig(jigNo, ownerUsername, JigStatus.Normal);
    }

    private Jig saveJig(String jigNo, String ownerUsername, JigStatus status) {
        return saveJig(jigNo, ownerUsername, status, "Line 1");
    }

    private Jig saveJig(String jigNo, String ownerUsername, JigStatus status, String assemblyLine) {
        return saveJig(jigNo, ownerUsername, status, assemblyLine, "Function Test Jig");
    }

    private Jig saveJig(String jigNo, String ownerUsername, JigStatus status, String assemblyLine, String jigName) {
        return saveJig(jigNo, ownerUsername, status, assemblyLine, jigName, "Mouse");
    }

    private Jig saveJig(
            String jigNo,
            String ownerUsername,
            JigStatus status,
            String assemblyLine,
            String jigName,
            String classification
    ) {
        Jig jig = new Jig();
        User owner = saveUser(ownerUsername, UserRole.ENGINEER);
        JigNumber parsedJigNo = JigNumber.parse(jigNo);
        jig.setClassification(classification);
        jig.setModelName("Demo Mouse");
        jig.setJigName(jigName);
        jig.setCustomer("Demo Customer");
        jig.setAssemblyLine(assemblyLine);
        jig.setJigNo(parsedJigNo.jigNo());
        jig.setJigBaseNo(parsedJigNo.jigBaseNo());
        jig.setSetNo(parsedJigNo.setNo());
        jig.setQuantity(1);
        jig.setStatus(status);
        jig.setStartDate(LocalDate.of(2024, 11, 26));
        jig.setDueDate(LocalDate.of(2024, 11, 26));
        jig.setCreatedBy(owner);
        jig.setUpdatedBy(owner);
        return jigRepository.save(jig);
    }

    private User saveUser(String username, UserRole role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(role);
            user.setEnabled(true);
            return userRepository.save(user);
        });
    }

    private com.jj.jig.user.UserLog userLog(User targetUser, User actorUser, UserLogActionType actionType) {
        com.jj.jig.user.UserLog log = new com.jj.jig.user.UserLog();
        log.setTargetUser(targetUser);
        log.setActorUser(actorUser);
        log.setActionType(actionType);
        log.setNote("Test account log.");
        return log;
    }

    private JigFile saveFile(Jig jig, String originalFilename) {
        JigFile file = new JigFile();
        file.setJig(jig);
        file.setOriginalFilename(originalFilename);
        file.setStoredPath("uploads/jigs/" + originalFilename);
        file.setContentType("application/pdf");
        file.setFileSize(100L);
        return jigFileRepository.save(file);
    }
}
