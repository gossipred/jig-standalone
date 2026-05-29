package com.jj.jig.license;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LicenseService {

    private final LicenseValidator licenseValidator;
    private final TrialService trialService;
    private final MachineIdService machineIdService;

    private AuthStatus cachedStatus;

    public LicenseService(LicenseValidator licenseValidator,
                          TrialService trialService,
                          MachineIdService machineIdService) {
        this.licenseValidator = licenseValidator;
        this.trialService = trialService;
        this.machineIdService = machineIdService;
    }

    public AuthStatus checkAuthorization() {
        if (cachedStatus != null) return cachedStatus;
        cachedStatus = evaluate();
        return cachedStatus;
    }

    /** Call after loading a new .lic file so the next check re-evaluates. */
    public void invalidateCache() {
        cachedStatus = null;
    }

    public String getMachineId() {
        return machineIdService.getMachineId();
    }

    private AuthStatus evaluate() {
        Optional<LicenseInfo> lic = licenseValidator.loadAndValidate();
        if (lic.isPresent()) {
            LicenseInfo info = lic.get();
            return switch (info.type()) {
                case MASTER -> AuthStatus.master(info);

                case PERPETUAL -> {
                    if (machineIdService.getMachineId().equals(info.machineId()))
                        yield AuthStatus.licensed(info);
                    yield AuthStatus.invalid();
                }

                case SUBSCRIPTION -> {
                    if (!machineIdService.getMachineId().equals(info.machineId()))
                        yield AuthStatus.invalid();
                    if (info.expiresAt() != null && LocalDate.now().isAfter(info.expiresAt()))
                        yield AuthStatus.expired();
                    yield AuthStatus.licensed(info);
                }

                default -> AuthStatus.invalid();
            };
        }

        return trialService.checkTrial();
    }
}
