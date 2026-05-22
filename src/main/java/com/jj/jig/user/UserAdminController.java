package com.jj.jig.user;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("userLogs", userService.findRecentLogs());
        return "admin/users/list";
    }

    @GetMapping("/admin/users/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        addFormOptions(model, "Add User", "新增使用者", "/admin/users/new", true);
        return "admin/users/form";
    }

    @PostMapping("/admin/users/new")
    public String create(
            @Valid @ModelAttribute("userForm") UserForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            addFormOptions(model, "Add User", "新增使用者", "/admin/users/new", true);
            return "admin/users/form";
        }

        try {
            User user = userService.create(form, principal.getName());
            redirectAttributes.addFlashAttribute("message", "User created: " + user.getUsername());
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            rejectUserFormError(bindingResult, ex);
            addFormOptions(model, "Add User", "新增使用者", "/admin/users/new", true);
            return "admin/users/form";
        }
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("userForm", UserForm.from(user));
        addFormOptions(model, "Edit User", "修改使用者", "/admin/users/" + id + "/edit", false);
        return "admin/users/form";
    }

    @PostMapping("/admin/users/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("userForm") UserForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            addFormOptions(model, "Edit User", "修改使用者", "/admin/users/" + id + "/edit", false);
            return "admin/users/form";
        }

        try {
            User user = userService.update(id, form, principal.getName());
            redirectAttributes.addFlashAttribute("message", "User updated: " + user.getUsername());
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            rejectUserFormError(bindingResult, ex);
            addFormOptions(model, "Edit User", "修改使用者", "/admin/users/" + id + "/edit", false);
            return "admin/users/form";
        }
    }

    private void addFormOptions(Model model, String title, String titleZh, String action, boolean passwordRequired) {
        model.addAttribute("title", title);
        model.addAttribute("titleZh", titleZh);
        model.addAttribute("action", action);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("passwordRequired", passwordRequired);
    }

    private void rejectUserFormError(BindingResult bindingResult, IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message != null && message.toLowerCase().contains("password")) {
            bindingResult.rejectValue("password", "password.invalid", message);
            return;
        }
        bindingResult.rejectValue("username", "username.invalid", message);
    }
}
