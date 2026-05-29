package com.jj.jig.license;

public record AuthStatus(
    AuthState state,
    int trialDaysLeft,
    LicenseInfo license
) {
    public boolean isAllowed() {
        return state == AuthState.MASTER
            || state == AuthState.LICENSED
            || state == AuthState.TRIAL;
    }

    public static AuthStatus master(LicenseInfo info) {
        return new AuthStatus(AuthState.MASTER, 0, info);
    }

    public static AuthStatus licensed(LicenseInfo info) {
        return new AuthStatus(AuthState.LICENSED, 0, info);
    }

    public static AuthStatus trial(int daysLeft) {
        return new AuthStatus(AuthState.TRIAL, daysLeft, null);
    }

    public static AuthStatus expired() {
        return new AuthStatus(AuthState.EXPIRED, 0, null);
    }

    public static AuthStatus invalid() {
        return new AuthStatus(AuthState.INVALID, 0, null);
    }
}
