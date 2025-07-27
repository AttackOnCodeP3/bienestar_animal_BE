package com.project.demo.rest.animal.dto;

import java.util.List;
import java.time.LocalDateTime;

public class AnimalRecordDTO {
    private GeneralInfoDTO generalInfo;
    private List<SanitaryControlDTO> sanitaryHistory;
    private LocationDTO location;
    private OwnerDTO ownerInfo;
    private Model3DStatusDTO model3DStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String origin;
    private String status; // completo/incompleto/pendiente

    // Getters y setters
}