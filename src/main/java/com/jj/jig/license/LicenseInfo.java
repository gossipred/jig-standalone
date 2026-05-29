package com.jj.jig.license;

import java.time.LocalDate;

public record LicenseInfo(
    int version,
    LicenseType type,
    String customer,
    String machineId,
    LocalDate issuedAt,
    LocalDate expiresAt
) {}
