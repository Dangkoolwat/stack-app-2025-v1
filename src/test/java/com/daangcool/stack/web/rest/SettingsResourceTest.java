package com.daangcool.stack.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daangcool.stack.service.GlobalSettingsService;
import com.daangcool.stack.service.dto.SettingsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SettingsResourceTest {

    @Mock
    private GlobalSettingsService globalSettingsService;

    @InjectMocks
    private SettingsResource settingsResource;

    @Test
    void getSettingsShouldReturnResponseWithBodyFromService() {
        SettingsDTO dto = new SettingsDTO(10L, 20L, 3, "desc");
        when(globalSettingsService.getSettings()).thenReturn(dto);

        ResponseEntity<SettingsDTO> response = settingsResource.getSettings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void updateSettingsShouldDelegateToServiceAndReturnOk() {
        SettingsDTO dto = new SettingsDTO(30L, 60L, 5, "new");

        ResponseEntity<Void> response = settingsResource.updateSettings(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(globalSettingsService).updateSettings(dto);
    }
}
