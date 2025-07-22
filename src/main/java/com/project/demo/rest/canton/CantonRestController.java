package com.project.demo.rest.canton;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.district.DistrictRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/cantons")
public class CantonRestController {

    @Autowired private CantonRepository cantonRepository;
    @Autowired private DistrictRepository districtRepository;

    private final Logger logger = Logger.getLogger(CantonRestController.class.getName());

    /**
     * Returns a paginated list of all cantons.
     *
     * @param page    Page number (default is 1)
     * @param size    Page size (default is 10)
     * @param request HTTP request used to construct metadata
     * @return Paginated list of cantons
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Obteniendo lista paginada de cantones. Página: " + page + ", Tamaño: " + size);

            Pageable pageable = PaginationUtils.buildPageable(page, size);
            Page<Canton> cantonPage = cantonRepository.findAll(pageable);
            Meta meta = PaginationUtils.buildMeta(request, cantonPage);

            return responseHandler.handleResponse(
                    "Cantones obtenidos correctamente.",
                    cantonPage.getContent(),
                    HttpStatus.OK,
                    meta
            );
        } catch (Exception e) {
            logger.severe("Error al obtener los cantones: " + e.getMessage());
            return responseHandler.internalError("Ocurrió un error al obtener los cantones.", request);
        }
    }

    /**
     * Returns a specific canton by its ID.
     *
     * @param cantonId ID of the canton
     * @param request  HTTP request
     * @return Canton details if found
     */
    @GetMapping("/{cantonId}")
    public ResponseEntity<?> getById(@PathVariable Long cantonId, HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            Optional<Canton> canton = cantonRepository.findById(cantonId);
            if (canton.isPresent()) {
                return responseHandler.handleResponse(
                        "Cantón obtenido correctamente.",
                        canton.get(),
                        HttpStatus.OK,
                        request
                );
            } else {
                return responseHandler.notFound("El cantón con ID " + cantonId + " no existe.", request);
            }
        } catch (Exception e) {
            logger.severe("Error al obtener cantón por ID: " + e.getMessage());
            return responseHandler.internalError("Ocurrió un error al obtener el cantón.", request);
        }
    }

    /**
     * Returns all districts for a given canton ID.
     *
     * @param cantonId Canton ID
     * @param request  HTTP request
     * @return List of districts
     */
    @GetMapping("/{cantonId}/districts")
    public ResponseEntity<?> getByCantonId(@PathVariable Long cantonId, HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Obteniendo distritos del cantón con ID: " + cantonId);
            var districts = districtRepository.findByCantonId(cantonId);
            return responseHandler.handleResponse(
                    "Distritos del cantón obtenidos correctamente.",
                    districts,
                    HttpStatus.OK,
                    request
            );
        } catch (Exception e) {
            logger.severe("Error al obtener distritos por cantón: " + e.getMessage());
            return responseHandler.internalError("Ocurrió un error al obtener los distritos.", request);
        }
    }

    /**
     * Creates a new canton.
     *
     * @param canton  Canton to create
     * @param request HTTP request
     * @return Created canton
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Canton canton, HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Creando nuevo cantón...");
            Canton saved = cantonRepository.save(canton);
            return responseHandler.created("Cantón creado exitosamente.", saved, request);
        } catch (Exception e) {
            logger.severe("Error al crear el cantón: " + e.getMessage());
            return responseHandler.internalError("Ocurrió un error al crear el cantón.", request);
        }
    }

    /**
     * Updates a canton by ID.
     *
     * @param cantonId ID of the canton to update
     * @param canton   Canton data
     * @param request  HTTP request
     * @return Updated canton
     */
    @PutMapping("/{cantonId}")
    public ResponseEntity<?> update(@PathVariable Long cantonId, @RequestBody Canton canton, HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            Optional<Canton> found = cantonRepository.findById(cantonId);
            if (found.isPresent()) {
                canton.setId(cantonId);
                Canton updated = cantonRepository.save(canton);
                return responseHandler.handleResponse("Cantón actualizado correctamente.", updated, HttpStatus.OK, request);
            } else {
                return responseHandler.notFound("El cantón con ID " + cantonId + " no existe.", request);
            }
        } catch (Exception e) {
            logger.severe("Error al actualizar cantón: " + e.getMessage());
            return responseHandler.internalError("Ocurrió un error al actualizar el cantón.", request);
        }
    }

    /**
     * Partially updates a canton.
     *
     * @param cantonId ID of the canton to patch
     * @param canton   Partial data
     * @param request  HTTP request
     * @return Patched canton
     */
    @PatchMapping("/{cantonId}")
    public ResponseEntity<?> patch(@PathVariable Long cantonId, @RequestBody Canton canton, HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            Optional<Canton> found = cantonRepository.findById(cantonId);
            if (found.isPresent()) {
                Canton existing = found.get();
                if (canton.getName() != null) existing.setName(canton.getName());
                Canton updated = cantonRepository.save(existing);
                return responseHandler.handleResponse("Cantón actualizado correctamente.", updated, HttpStatus.OK, request);
            } else {
                return responseHandler.notFound("El cantón con ID " + cantonId + " no existe.", request);
            }
        } catch (Exception e) {
            logger.severe("Error al realizar patch del cantón: " + e.getMessage());
            return responseHandler.internalError("Ocurrió un error al actualizar el cantón.", request);
        }
    }

    /**
     * Deletes a canton by ID.
     *
     * @param cantonId ID of the canton to delete
     * @param request  HTTP request
     * @return Deleted canton
     */
    @DeleteMapping("/{cantonId}")
    public ResponseEntity<?> delete(@PathVariable Long cantonId, HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            Optional<Canton> found = cantonRepository.findById(cantonId);
            if (found.isPresent()) {
                cantonRepository.deleteById(cantonId);
                return responseHandler.handleResponse("Cantón eliminado correctamente.", found.get(), HttpStatus.OK, request);
            } else {
                return responseHandler.notFound("El cantón con ID " + cantonId + " no existe.", request);
            }
        } catch (Exception e) {
            logger.severe("Error al eliminar cantón: " + e.getMessage());
            return responseHandler.internalError("Ocurrió un error al eliminar el cantón.", request);
        }
    }
}