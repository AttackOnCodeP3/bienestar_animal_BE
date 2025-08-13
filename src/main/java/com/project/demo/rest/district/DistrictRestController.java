package com.project.demo.rest.district;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.district.District;
import com.project.demo.logic.entity.district.DistrictRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/districts")
public class DistrictRestController {

    private static final Logger logger = LoggerFactory.getLogger(DistrictRestController.class);

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        var globalResponseHandler = new GlobalResponseHandler();
        logger.info("Invocando getAll - obteniendo todos los distritos. Página: {}, Tamaño: {}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<District> districtPage = districtRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, districtPage);

        return globalResponseHandler.handleResponse(
                "Distritos obtenidos correctamente",
                districtPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{districtId}")
    public ResponseEntity<?> getById(@PathVariable Long districtId, HttpServletRequest request) {
        logger.info("Invocando getById - buscando distrito con ID: {}", districtId);
        var globalResponseHandler = new GlobalResponseHandler();
        Optional<District> district = districtRepository.findById(districtId);
        if (district.isPresent()) {
            return globalResponseHandler.handleResponse(
                    "Distrito obtenido correctamente",
                    district.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Distrito con ID {} no fue encontrado", districtId);
            return globalResponseHandler.notFound(
                    "El distrito con ID " + districtId + " no fue encontrado",
                    request
            );
        }
    }

    @GetMapping("/{districtId}/neighborhoods")
    public ResponseEntity<?> getByDistrictId(@PathVariable Long districtId, HttpServletRequest request) {
        logger.info("Invocando getByDistrictId - obteniendo barrios del distrito con ID: {}", districtId);
        var globalResponseHandler = new GlobalResponseHandler();
        var neighborhoods = neighborhoodRepository.findByDistrictId(districtId);
        return globalResponseHandler.handleResponse(
                "Barrios del distrito obtenidos correctamente",
                neighborhoods,
                HttpStatus.OK,
                request
        );
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody District district, HttpServletRequest request) {
        logger.info("Invocando create - creando nuevo distrito: {}", district.getName());
        var globalResponseHandler = new GlobalResponseHandler();
        District saved = districtRepository.save(district);
        return globalResponseHandler.created(
                "Distrito creado correctamente",
                saved,
                request
        );
    }

    @PutMapping("/{districtId}")
    public ResponseEntity<?> update(@PathVariable Long districtId, @RequestBody District district, HttpServletRequest request) {
        logger.info("Invocando update - actualizando distrito con ID: {}", districtId);
        Optional<District> found = districtRepository.findById(districtId);
        var globalResponseHandler = new GlobalResponseHandler();
        if (found.isPresent()) {
            district.setId(districtId);
            District updated = districtRepository.save(district);
            return globalResponseHandler.success(
                    "Distrito actualizado correctamente",
                    updated,
                    request
            );
        } else {
            logger.warn("Distrito con ID {} no fue encontrado para actualización", districtId);
            return globalResponseHandler.notFound(
                    "El distrito con ID " + districtId + " no fue encontrado",
                    request
            );
        }
    }

    @PatchMapping("/{districtId}")
    public ResponseEntity<?> patch(@PathVariable Long districtId, @RequestBody District district, HttpServletRequest request) {
        logger.info("Invocando patch - actualización parcial del distrito con ID: {}", districtId);
        Optional<District> found = districtRepository.findById(districtId);
        var globalResponseHandler = new GlobalResponseHandler();
        if (found.isPresent()) {
            District existing = found.get();
            if (district.getName() != null) existing.setName(district.getName());
            if (district.getCanton() != null) existing.setCanton(district.getCanton());
            District patched = districtRepository.save(existing);
            return globalResponseHandler.success(
                    "Distrito actualizado correctamente",
                    patched,
                    request
            );
        } else {
            logger.warn("Distrito con ID {} no fue encontrado para parche", districtId);
            return globalResponseHandler.notFound(
                    "El distrito con ID " + districtId + " no fue encontrado",
                    request
            );
        }
    }

    @DeleteMapping("/{districtId}")
    public ResponseEntity<?> delete(@PathVariable Long districtId, HttpServletRequest request) {
        logger.info("Invocando delete - eliminando distrito con ID: {}", districtId);
        Optional<District> found = districtRepository.findById(districtId);
        var globalResponseHandler = new GlobalResponseHandler();
        if (found.isPresent()) {
            districtRepository.deleteById(districtId);
            return globalResponseHandler.success(
                    "Distrito eliminado correctamente",
                    found.get(),
                    request
            );
        } else {
            logger.warn("Distrito con ID {} no fue encontrado para eliminación", districtId);
            return globalResponseHandler.notFound(
                    "El distrito con ID " + districtId + " no fue encontrado",
                    request
            );
        }
    }
}