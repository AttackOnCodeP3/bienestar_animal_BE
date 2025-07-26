package com.project.demo.rest.municipal_preventive_care_configuration;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfiguration;
import com.project.demo.logic.entity.municipal_preventive_care_configuration.MunicipalPreventiveCareConfigurationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/municipal-preventive-care-configurations")
public class MunicipalPreventiveCareConfigurationRestController {

    private static final Logger logger = LoggerFactory.getLogger(MunicipalPreventiveCareConfigurationRestController.class);

    @Autowired
    private MunicipalPreventiveCareConfigurationRepository configurationRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        logger.info("Invocando getAll - obteniendo configuraciones preventivas municipales. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<MunicipalPreventiveCareConfiguration> configPage = configurationRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, configPage);

        return globalResponseHandler.handleResponse(
                "Configuraciones preventivas municipales obtenidas correctamente",
                configPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando getById - obteniendo configuración preventiva con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<MunicipalPreventiveCareConfiguration> opt = configurationRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Configuración con ID {} no fue encontrada", id);
            return globalResponseHandler.notFound(
                    "La configuración con ID " + id + " no fue encontrada",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Configuración preventiva municipal obtenida correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN, MUNICIPAL_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MunicipalPreventiveCareConfiguration config, HttpServletRequest request) {
        logger.info("Invocando update - actualizando configuración preventiva municipal con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<MunicipalPreventiveCareConfiguration> opt = configurationRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Configuración con ID {} no fue encontrada", id);
            return globalResponseHandler.notFound(
                    "La configuración con ID " + id + " no fue encontrada",
                    request
            );
        }

        MunicipalPreventiveCareConfiguration current = opt.get();
        current.setVaccinationFrequencyMonths(config.getVaccinationFrequencyMonths());
        current.setDewormingFrequencyMonths(config.getDewormingFrequencyMonths());
        current.setFleaFrequencyMonths(config.getFleaFrequencyMonths());
        current.setMunicipality(config.getMunicipality());

        configurationRepository.save(current);
        return globalResponseHandler.success(
                "Configuración preventiva municipal actualizada correctamente",
                current,
                request
        );
    }
}
