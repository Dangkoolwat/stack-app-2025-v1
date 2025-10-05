package com.daangcool.stack.web.rest;

import com.daangcool.stack.security.AuthoritiesConstants;
import com.daangcool.stack.service.GlobalSettingsService;
import com.daangcool.stack.service.dto.SettingsDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SettingsResource {

    private final GlobalSettingsService globalSettingsService;

    public SettingsResource(GlobalSettingsService globalSettingsService) {
        this.globalSettingsService = globalSettingsService;
    }

    /**
     * {@code GET  /settings} : get the global settings.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the settings in body.
     */
    @GetMapping("/settings")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<SettingsDTO> getSettings() {
        return ResponseEntity.ok(globalSettingsService.getSettings());
    }

    /**
     * {@code PUT  /settings} : update the global settings.
     *
     * @param settingsDTO the settings to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PutMapping("/settings")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> updateSettings(@Valid @RequestBody SettingsDTO settingsDTO) {
        globalSettingsService.updateSettings(settingsDTO);
        return ResponseEntity.ok().build();
    }
}
