package com.jj.jig.user;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account/password")
    public String passwordForm(Model model, Principal principal) {
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        model.addAttribute("username", principal.getName());
        return "account/password";
    }

    @PostMapping("/account/password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", principal.getName());
            return "account/password";
        }

        try {
            userService.changeOwnPassword(principal.getName(), form);
            redirectAttributes.addFlashAttribute("message", "Password changed successfully.");
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            rejectPasswordError(bindingResult, ex);
            model.addAttribute("username", principal.getName());
            return "account/password";
        }
    }

    private void rejectPasswordError(BindingResult bindingResult, IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message != null && message.toLowerCase().contains("current")) {
            bindingResult.rejectValue("currentPassword", "currentPassword.invalid", message);
            return;
        }
        bindingResult.rejectValue("confirmPassword", "confirmPassword.invalid", message);
    }
}
