package com.jj.jig.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserForm {

    @NotBlank
    @Size(max = 50)
    private String username;

    @Size(max = 50)
    private String password;

    @NotNull
    private UserRole role = UserRole.OPERATOR;

    private boolean enabled = true;

    public static UserForm from(User user) {
        UserForm form = new UserForm();
        form.setUsername(user.getUsername());
        form.setRole(user.getRole());
        form.setEnabled(user.isEnabled());
        return form;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
