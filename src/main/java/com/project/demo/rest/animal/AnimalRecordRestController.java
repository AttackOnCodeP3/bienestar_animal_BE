package com.project.demo.rest.animal;

import com.project.demo.rest.animal.dto.AnimalRecordDTO;
import com.project.demo.service.model.AnimalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/animals")
public class AnimalRecordRestController {

    @Autowired
    private AnimalRecordService animalRecordService;

    @GetMapping("/{animalId}/record")
    public ResponseEntity<?> getAnimalRecord(
            @PathVariable Long animalId,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        AnimalRecordDTO dto = animalRecordService.getAnimalRecord(animalId, token);
        if (dto == null) {
            return ResponseEntity.status(403).body("No tiene permisos para ver este expediente.");
        }
        return ResponseEntity.ok(dto);
    }
}