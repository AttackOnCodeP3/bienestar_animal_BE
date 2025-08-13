
package com.project.demo.rest.report;

import com.project.demo.logic.entity.animal.AbandonedAnimal;
import com.project.demo.logic.entity.animal.AbandonedAnimalRepository;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.complaint.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    @Autowired
    private com.project.demo.logic.entity.sanitary_control.SanitaryControlRepository sanitaryControlRepository;
    @Autowired
    private com.project.demo.logic.entity.municipality.MunicipalityRepository municipalityRepository;
    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private ComplaintRepository complaintRepository;
    @Autowired
    private AbandonedAnimalRepository abandonedAnimalRepository;
    @Autowired
    private CommunityAnimalRepository communityAnimalRepository;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestParam String type) {
        switch (type) {
            case "ANIMALES_ABANDONADOS": {
                List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                for (var animal : abandoned) {
                    String cantonName = (animal.getCanton() != null && animal.getCanton().getName() != null)
                            ? animal.getCanton().getName()
                            : "Sin cantón";
                    String especie = (animal.getSpecies() != null && animal.getSpecies().getName() != null)
                            ? animal.getSpecies().getName()
                            : "Sin especie";
                    String sexo = (animal.getSex() != null && animal.getSex().getName() != null)
                            ? animal.getSex().getName()
                            : "Sin sexo";
                    grouped.computeIfAbsent(cantonName, c -> new HashMap<>())
                            .computeIfAbsent(especie, e -> new HashMap<>())
                            .merge(sexo, 1L, Long::sum);
                }
                List<Map<String, Object>> rows = new ArrayList<>();
                for (var cantonEntry : grouped.entrySet()) {
                    for (var especieEntry : cantonEntry.getValue().entrySet()) {
                        for (var sexoEntry : especieEntry.getValue().entrySet()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("canton", cantonEntry.getKey());
                            row.put("especie", especieEntry.getKey());
                            row.put("sexo", sexoEntry.getKey());
                            row.put("cantidad", sexoEntry.getValue());
                            rows.add(row);
                        }
                    }
                }
                return ResponseEntity.ok(Map.of(
                        "type", type,
                        "data", rows
                ));
            }
            case "ESTERILIZACION_MUNICIPIO": {
                List<com.project.demo.logic.entity.animal.Animal> pets = animalRepository.findAll();
                List<com.project.demo.logic.entity.sanitary_control.SanitaryControl> controls = sanitaryControlRepository.findAll();
                System.out.println("[DEBUG] Total animales: " + pets.size());
                System.out.println("[DEBUG] Total controles sanitarios: " + controls.size());
                Set<Long> sterilizedAnimalIds = new HashSet<>();
                for (var control : controls) {
                    if (control.getSanitaryControlType() != null &&
                            control.getSanitaryControlType().getId() == 3 &&
                            control.getAnimal() != null) {
                        sterilizedAnimalIds.add(control.getAnimal().getId());
                    }
                }
                System.out.println("[DEBUG] Total animales esterilizados: " + sterilizedAnimalIds.size());
                Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                for (var animal : pets) {
                    String municipality = "Sin municipalidad";
                    if (animal.getOwner() != null && animal.getOwner().getMunicipality() != null) {
                        var muni = animal.getOwner().getMunicipality();
                        if (muni.getName() != null && !muni.getName().isBlank()) {
                            municipality = muni.getName();
                        }
                    }
                    String sex = animal.getSex() != null && animal.getSex().getName() != null ? animal.getSex().getName() : "";
                    String status = sterilizedAnimalIds.contains(animal.getId()) ? "Esterilizado" : "No esterilizado";
                    grouped.computeIfAbsent(municipality, m -> new HashMap<>())
                            .computeIfAbsent(sex, s -> new HashMap<>())
                            .merge(status, 1L, Long::sum);
                }
                System.out.println("[DEBUG] Agrupaciones encontradas: " + grouped.size());
                List<Map<String, Object>> rows = new ArrayList<>();
                for (var muniEntry : grouped.entrySet()) {
                    for (var sexEntry : muniEntry.getValue().entrySet()) {
                        for (var statusEntry : sexEntry.getValue().entrySet()) {
                            rows.add(Map.of(
                                    "municipality", muniEntry.getKey(),
                                    "sex", sexEntry.getKey(),
                                    "status", statusEntry.getKey(),
                                    "count", statusEntry.getValue()
                            ));
                        }
                    }
                }
                System.out.println("[DEBUG] Filas generadas: " + rows.size());
                if (rows.isEmpty()) {
                    System.out.println("[DEBUG] No hay datos para el criterio seleccionado");
                    return ResponseEntity.ok(Map.of("type", type, "data", List.of(), "message", "No hay datos"));
                }
                return ResponseEntity.ok(Map.of("type", type, "data", rows));
            }
            case "ANIMALES_CALLEJEROS":
                long abandonados = animalRepository.countAbandonedAnimals();
                return ResponseEntity.ok(Map.of("type", type, "total", abandonados));
            case "MASCOTAS_POR_TIPO":
                return ResponseEntity.ok(Map.of("type", type, "data", List.of(), "message", "No hay datos"));
            case "DENUNCIAS_ABIERTAS":
                long abiertas = complaintRepository.countOpenComplaints();
                return ResponseEntity.ok(Map.of("type", type, "total", abiertas));
            case "DENUNCIAS_POR_TIPO":
                return ResponseEntity.ok(Map.of("type", type, "data", List.of(), "message", "No hay datos"));
            case "MASCOTAS_POR_MUNICIPALIDAD": {
                List<com.project.demo.logic.entity.animal.Animal> pets = animalRepository.findAll();
                List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                List<CommunityAnimal> community = communityAnimalRepository.findAll();
                boolean hasData = (!pets.isEmpty()) || (!abandoned.isEmpty()) || (!community.isEmpty());
                if (!hasData) {
                    return ResponseEntity.ok(Map.of("type", type, "data", List.of(), "message", "No hay datos"));
                }
                Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                for (var animal : pets) {
                    String municipality = "Sin municipalidad";
                    if (animal.getOwner() != null && animal.getOwner().getMunicipality() != null) {
                        var muni = animal.getOwner().getMunicipality();
                        if (muni.getName() != null && !muni.getName().isBlank()) {
                            municipality = muni.getName();
                        }
                    }
                    String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                    String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                    grouped
                            .computeIfAbsent(municipality, m -> new HashMap<>())
                            .computeIfAbsent(species, s -> new HashMap<>())
                            .merge(sex, 1L, Long::sum);
                }
                for (AbandonedAnimal animal : abandoned) {
                    String municipality = "Sin municipalidad";
                    if (animal.getCreatedBy() != null && animal.getCreatedBy().getMunicipality() != null) {
                        var muni = animal.getCreatedBy().getMunicipality();
                        if (muni.getName() != null && !muni.getName().isBlank()) {
                            municipality = muni.getName();
                        }
                    }
                    String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                    String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                    grouped
                            .computeIfAbsent(municipality, m -> new HashMap<>())
                            .computeIfAbsent(species, s -> new HashMap<>())
                            .merge(sex, 1L, Long::sum);
                }
                for (CommunityAnimal animal : community) {
                    String municipality = "Sin municipalidad";
                    var muni = animal.getMunicipality();
                    if (muni != null && muni.getName() != null && !muni.getName().isBlank()) {
                        municipality = muni.getName();
                    }
                    String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                    String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                    grouped
                            .computeIfAbsent(municipality, m -> new HashMap<>())
                            .computeIfAbsent(species, s -> new HashMap<>())
                            .merge(sex, 1L, Long::sum);
                }
                List<Map<String, Object>> rows = new ArrayList<>();
                for (var municipalityEntry : grouped.entrySet()) {
                    for (var speciesEntry : municipalityEntry.getValue().entrySet()) {
                        for (var sexEntry : speciesEntry.getValue().entrySet()) {
                            rows.add(Map.of(
                                    "municipality", municipalityEntry.getKey(),
                                    "species", speciesEntry.getKey(),
                                    "sex", sexEntry.getKey(),
                                    "count", sexEntry.getValue()
                            ));
                        }
                    }
                }
                return ResponseEntity.ok(Map.of(
                        "type", type,
                        "data", rows
                ));
            }
            case "ANIMALES_CON_HOGAR": {
                List<com.project.demo.logic.entity.animal.Animal> pets = animalRepository.findAll();
                List<com.project.demo.logic.entity.sanitary_control.SanitaryControl> controls = sanitaryControlRepository.findAll();
                Set<Long> sterilizedAnimalIds = new HashSet<>();
                for (var control : controls) {
                    if (control.getSanitaryControlType() != null &&
                            control.getSanitaryControlType().getId() == 3 &&
                            control.getAnimal() != null) {
                        sterilizedAnimalIds.add(control.getAnimal().getId());
                    }
                }
                Map<String, Map<String, Long>> grouped = new HashMap<>();
                for (var animal : pets) {
                    if (animal.getOwner() != null && animal.getOwner().getMunicipality() != null) {
                        String municipality = animal.getOwner().getMunicipality().getName();
                        String status = sterilizedAnimalIds.contains(animal.getId()) ? "Esterilizado" : "No esterilizado";
                        grouped.computeIfAbsent(municipality, m -> new HashMap<>())
                                .merge(status, 1L, Long::sum);
                    }
                }
                List<Map<String, Object>> rows = new ArrayList<>();
                for (var muniEntry : grouped.entrySet()) {
                    for (var statusEntry : muniEntry.getValue().entrySet()) {
                        rows.add(Map.of(
                                "municipality", muniEntry.getKey(),
                                "status", statusEntry.getKey(),
                                "count", statusEntry.getValue()
                        ));
                    }
                }
                if (rows.isEmpty()) {
                    return ResponseEntity.ok(Map.of("type", type, "data", List.of(), "message", "No hay datos"));
                }
                return ResponseEntity.ok(Map.of("type", type, "data", rows));
            }
            case "INDICADORES_ABANDONO": {
                List<Object[]> results = complaintRepository.countComplaintsByType();
                List<Map<String, Object>> rows = new ArrayList<>();
                for (Object[] row : results) {
                    String tipo = row[0] != null ? row[0].toString() : "Sin tipo";
                    Long cantidad = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    rows.add(Map.of("tipo", tipo, "cantidad", cantidad));
                }
                return ResponseEntity.ok(Map.of("type", type, "data", rows));
            }
            default:
                return ResponseEntity.ok(Map.of("type", type, "data", List.of(), "message", "No hay datos"));
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/download/csv")
    public ResponseEntity<ByteArrayResource> downloadCsv(@RequestParam String type) {
        StringBuilder csv = new StringBuilder();
        switch (type) {
            case "ANIMALES_ABANDONADOS": {
                List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                for (var animal : abandoned) {
                    String cantonName = (animal.getCanton() != null && animal.getCanton().getName() != null)
                            ? animal.getCanton().getName()
                            : "Sin cantón";
                    String especie = (animal.getSpecies() != null && animal.getSpecies().getName() != null)
                            ? animal.getSpecies().getName()
                            : "Sin especie";
                    String sexo = (animal.getSex() != null && animal.getSex().getName() != null)
                            ? animal.getSex().getName()
                            : "Sin sexo";
                    grouped.computeIfAbsent(cantonName, c -> new HashMap<>())
                            .computeIfAbsent(especie, e -> new HashMap<>())
                            .merge(sexo, 1L, Long::sum);
                }
                csv.append("Cantón,Especie,Sexo,Cantidad\n");
                for (var cantonEntry : grouped.entrySet()) {
                    for (var especieEntry : cantonEntry.getValue().entrySet()) {
                        for (var sexoEntry : especieEntry.getValue().entrySet()) {
                            csv.append(cantonEntry.getKey()).append(",")
                                    .append(especieEntry.getKey()).append(",")
                                    .append(sexoEntry.getKey()).append(",")
                                    .append(sexoEntry.getValue()).append("\n");
                        }
                    }
                }
                break;
            }
                case "ESTERILIZACION_MUNICIPIO": {
                    List<com.project.demo.logic.entity.animal.Animal> pets = animalRepository.findAll();
                    List<com.project.demo.logic.entity.sanitary_control.SanitaryControl> controls = sanitaryControlRepository.findAll();
                    Set<Long> sterilizedAnimalIds = new HashSet<>();
                    for (var control : controls) {
                        if (control.getSanitaryControlType() != null &&
                                control.getSanitaryControlType().getId() == 3 &&
                                control.getAnimal() != null) {
                            sterilizedAnimalIds.add(control.getAnimal().getId());
                        }
                    }
                    Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                    for (var animal : pets) {
                        String municipality = "Sin municipalidad";
                        if (animal.getOwner() != null && animal.getOwner().getMunicipality() != null) {
                            var muni = animal.getOwner().getMunicipality();
                            if (muni.getName() != null && !muni.getName().isBlank()) {
                                municipality = muni.getName();
                            }
                        }
                        String sex = animal.getSex() != null && animal.getSex().getName() != null ? animal.getSex().getName() : "";
                        String status = sterilizedAnimalIds.contains(animal.getId()) ? "Esterilizado" : "No esterilizado";
                        grouped.computeIfAbsent(municipality, m -> new HashMap<>())
                                .computeIfAbsent(sex, s -> new HashMap<>())
                                .merge(status, 1L, Long::sum);
                    }
                    csv.append("Municipalidad,Sexo,Estatus,Cantidad\n");
                    for (var muniEntry : grouped.entrySet()) {
                        for (var sexEntry : muniEntry.getValue().entrySet()) {
                            for (var statusEntry : sexEntry.getValue().entrySet()) {
                                csv.append(muniEntry.getKey()).append(",")
                                        .append(sexEntry.getKey()).append(",")
                                        .append(statusEntry.getKey()).append(",")
                                        .append(statusEntry.getValue()).append("\n");
                            }
                        }
                    }
                    break;
                }
                case "ESTERILIZACION_CANTON": {
                    List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                    List<com.project.demo.logic.entity.sanitary_control.SanitaryControl> controls = sanitaryControlRepository.findAll();
                    Set<Long> sterilizedAnimalIds = new HashSet<>();
                    for (var control : controls) {
                        if (control.getSanitaryControlType() != null &&
                                control.getSanitaryControlType().getId() == 3 &&
                                control.getAnimal() != null) {
                            sterilizedAnimalIds.add(control.getAnimal().getId());
                        }
                    }
                    Map<String, Map<String, Long>> grouped = new HashMap<>();
                    for (var animal : abandoned) {
                        String canton = "Sin cantón";
                        if (animal.getCanton() != null && animal.getCanton().getName() != null && !animal.getCanton().getName().isBlank()) {
                            canton = animal.getCanton().getName();
                        }
                        String status = (animal.getId() != null && sterilizedAnimalIds.contains(animal.getId())) ? "Esterilizado" : "No esterilizado";
                        grouped.computeIfAbsent(canton, m -> new HashMap<>())
                                .merge(status, 1L, Long::sum);
                    }
                    csv.append("Cantón,Estatus,Cantidad\n");
                    for (var cantonEntry : grouped.entrySet()) {
                        for (var statusEntry : cantonEntry.getValue().entrySet()) {
                            csv.append(cantonEntry.getKey()).append(",")
                                    .append(statusEntry.getKey()).append(",")
                                    .append(statusEntry.getValue()).append("\n");
                        }
                    }
                    break;
                }
                case "ANIMALES_CALLEJEROS": {
                    List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                    if (abandoned.isEmpty()) {
                        csv.append("No hay datos para este tipo de reporte\n");
                        break;
                    }
                    Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                    for (AbandonedAnimal animal : abandoned) {
                        String canton = "Sin cantón";
                        if (animal.getCanton() != null && animal.getCanton().getName() != null && !animal.getCanton().getName().isBlank()) {
                            canton = animal.getCanton().getName();
                        }
                        String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                        String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                        grouped
                                .computeIfAbsent(canton, c -> new HashMap<>())
                                .computeIfAbsent(species, s -> new HashMap<>())
                                .merge(sex, 1L, Long::sum);
                    }
                    csv.append("Cantón,Especie,Sexo,Cantidad\n");
                    for (var cantonEntry : grouped.entrySet()) {
                        for (var speciesEntry : cantonEntry.getValue().entrySet()) {
                            for (var sexEntry : speciesEntry.getValue().entrySet()) {
                                csv.append(cantonEntry.getKey()).append(",")
                                        .append(speciesEntry.getKey()).append(",")
                                        .append(sexEntry.getKey()).append(",")
                                        .append(sexEntry.getValue()).append("\n");
                            }
                        }
                    }
                    break;
                }
                case "MASCOTAS_POR_TIPO":
                    csv.append("Tipo,Total\n");
                    for (Object[] row : animalRepository.countAnimalsByType()) {
                        csv.append(row[0]).append(",").append(row[1]).append("\n");
                    }
                    break;
                case "DENUNCIAS_ABIERTAS":
                    csv.append("Total Denuncias Abiertas\n");
                    long abiertas = complaintRepository.countOpenComplaints();
                    csv.append(abiertas).append("\n");
                    break;
                case "DENUNCIAS_POR_TIPO":
                    csv.append("Tipo,Total\n");
                    for (Object[] row : complaintRepository.countComplaintsByType()) {
                        csv.append(row[0]).append(",").append(row[1]).append("\n");
                    }
                    break;
                case "MASCOTAS_POR_MUNICIPALIDAD": {
                    List<com.project.demo.logic.entity.animal.Animal> pets = animalRepository.findAll();
                    List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                    List<CommunityAnimal> community = communityAnimalRepository.findAll();
                    boolean hasData = (!pets.isEmpty()) || (!abandoned.isEmpty()) || (!community.isEmpty());
                    if (!hasData) {
                        csv.append("No hay datos para este tipo de reporte\n");
                        break;
                    }
                    Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                    for (var animal : pets) {
                        String municipality = "Sin municipalidad";
                        if (animal.getOwner() != null && animal.getOwner().getMunicipality() != null) {
                            var muni = animal.getOwner().getMunicipality();
                            if (muni.getName() != null && !muni.getName().isBlank()) {
                                municipality = muni.getName();
                            }
                        }
                        String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                        String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                        grouped
                                .computeIfAbsent(municipality, m -> new HashMap<>())
                                .computeIfAbsent(species, s -> new HashMap<>())
                                .merge(sex, 1L, Long::sum);
                    }
                    for (AbandonedAnimal animal : abandoned) {
                        String municipality = "Sin municipalidad";
                        if (animal.getCreatedBy() != null && animal.getCreatedBy().getMunicipality() != null) {
                            var muni = animal.getCreatedBy().getMunicipality();
                            if (muni.getName() != null && !muni.getName().isBlank()) {
                                municipality = muni.getName();
                            }
                        }
                        String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                        String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                        grouped
                                .computeIfAbsent(municipality, m -> new HashMap<>())
                                .computeIfAbsent(species, s -> new HashMap<>())
                                .merge(sex, 1L, Long::sum);
                    }
                    for (CommunityAnimal animal : community) {
                        String municipality = "Sin municipalidad";
                        var muni = animal.getMunicipality();
                        if (muni != null && muni.getName() != null && !muni.getName().isBlank()) {
                            municipality = muni.getName();
                        }
                        String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                        String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                        grouped
                                .computeIfAbsent(municipality, m -> new HashMap<>())
                                .computeIfAbsent(species, s -> new HashMap<>())
                                .merge(sex, 1L, Long::sum);
                    }
                    csv.append("Municipalidad,Especie,Sexo,Cantidad\n");
                    for (var municipalityEntry : grouped.entrySet()) {
                        for (var speciesEntry : municipalityEntry.getValue().entrySet()) {
                            for (var sexEntry : speciesEntry.getValue().entrySet()) {
                                csv.append(municipalityEntry.getKey()).append(",")
                                        .append(speciesEntry.getKey()).append(",")
                                        .append(sexEntry.getKey()).append(",")
                                        .append(sexEntry.getValue()).append("\n");
                            }
                        }
                    }
                    break;
                }
                default:
                    csv.append("No hay datos para este tipo de reporte\n");
            }

            byte[] data = csv.toString().getBytes();
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-" + type + ".csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(data.length)
                    .body(resource);
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @GetMapping("/download/pdf")
        public ResponseEntity<ByteArrayResource> downloadPdf(@RequestParam String type) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, baos);
                document.open();
                switch (type) {
                    case "ESTERILIZACION_MUNICIPIO": {
                        List<com.project.demo.logic.entity.animal.Animal> pets = animalRepository.findAll();
                        List<com.project.demo.logic.entity.sanitary_control.SanitaryControl> controls = sanitaryControlRepository.findAll();
                        Set<Long> sterilizedAnimalIds = new HashSet<>();
                        for (var control : controls) {
                            if (control.getSanitaryControlType() != null &&
                                    control.getSanitaryControlType().getId() == 3 &&
                                    control.getAnimal() != null) {
                                sterilizedAnimalIds.add(control.getAnimal().getId());
                            }
                        }
                        Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                        for (var animal : pets) {
                            String municipality = "Sin municipalidad";
                            if (animal.getOwner() != null && animal.getOwner().getMunicipality() != null) {
                                var muni = animal.getOwner().getMunicipality();
                                if (muni.getName() != null && !muni.getName().isBlank()) {
                                    municipality = muni.getName();
                                }
                            }
                            String sex = animal.getSex() != null && animal.getSex().getName() != null ? animal.getSex().getName() : "";
                            String status = sterilizedAnimalIds.contains(animal.getId()) ? "Esterilizado" : "No esterilizado";
                            grouped.computeIfAbsent(municipality, m -> new HashMap<>())
                                    .computeIfAbsent(sex, s -> new HashMap<>())
                                    .merge(status, 1L, Long::sum);
                        }
                        document.add(new Paragraph("Municipalidad | Sexo | Estatus | Cantidad"));
                        for (var muniEntry : grouped.entrySet()) {
                            for (var sexEntry : muniEntry.getValue().entrySet()) {
                                for (var statusEntry : sexEntry.getValue().entrySet()) {
                                    document.add(new Paragraph(
                                            muniEntry.getKey() + " | " +
                                                    sexEntry.getKey() + " | " +
                                                    statusEntry.getKey() + " | " +
                                                    statusEntry.getValue()
                                    ));
                                }
                            }
                        }
                        break;
                    }
                    case "ESTERILIZACION_CANTON": {
                        List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                        List<com.project.demo.logic.entity.sanitary_control.SanitaryControl> controls = sanitaryControlRepository.findAll();
                        Set<Long> sterilizedAnimalIds = new HashSet<>();
                        for (var control : controls) {
                            if (control.getSanitaryControlType() != null &&
                                    control.getSanitaryControlType().getId() == 3 &&
                                    control.getAnimal() != null) {
                                sterilizedAnimalIds.add(control.getAnimal().getId());
                            }
                        }
                        Map<String, Map<String, Long>> grouped = new HashMap<>();
                        for (var animal : abandoned) {
                            String canton = "Sin cantón";
                            if (animal.getCanton() != null && animal.getCanton().getName() != null && !animal.getCanton().getName().isBlank()) {
                                canton = animal.getCanton().getName();
                            }
                            String status = (animal.getId() != null && sterilizedAnimalIds.contains(animal.getId())) ? "Esterilizado" : "No esterilizado";
                            grouped.computeIfAbsent(canton, m -> new HashMap<>())
                                    .merge(status, 1L, Long::sum);
                        }
                        document.add(new Paragraph("Cantón | Estatus | Cantidad"));
                        for (var cantonEntry : grouped.entrySet()) {
                            for (var statusEntry : cantonEntry.getValue().entrySet()) {
                                document.add(new Paragraph(
                                        cantonEntry.getKey() + " | " +
                                                statusEntry.getKey() + " | " +
                                                statusEntry.getValue()
                                ));
                            }
                        }
                        break;
                    }
                    case "ANIMALES_CALLEJEROS": {
                        long abandonados = animalRepository.countAbandonedAnimals();
                        document.add(new Paragraph("Total Animales Callejeros: " + abandonados));
                        break;
                    }
                    case "MASCOTAS_POR_TIPO": {
                        document.add(new Paragraph("Tipo - Total"));
                        for (Object[] row : animalRepository.countAnimalsByType()) {
                            document.add(new Paragraph(row[0] + " - " + row[1]));
                        }
                        break;
                    }
                    case "DENUNCIAS_ABIERTAS": {
                        long abiertas = complaintRepository.countOpenComplaints();
                        document.add(new Paragraph("Total Denuncias Abiertas: " + abiertas));
                        break;
                    }
                    case "DENUNCIAS_POR_TIPO": {
                        document.add(new Paragraph("Tipo - Total"));
                        for (Object[] row : complaintRepository.countComplaintsByType()) {
                            document.add(new Paragraph(row[0] + " - " + row[1]));
                        }
                        break;
                    }
                    case "MASCOTAS_POR_MUNICIPALIDAD": {
                        List<com.project.demo.logic.entity.animal.Animal> pets = animalRepository.findAll();
                        List<AbandonedAnimal> abandoned = abandonedAnimalRepository.findAll();
                        List<CommunityAnimal> community = communityAnimalRepository.findAll();
                        boolean hasData = (!pets.isEmpty()) || (!abandoned.isEmpty()) || (!community.isEmpty());
                        if (!hasData) {
                            document.add(new Paragraph("No hay datos para este tipo de reporte"));
                            break;
                        }
                        Map<String, Map<String, Map<String, Long>>> grouped = new HashMap<>();
                        for (var animal : pets) {
                            String municipality = "Sin municipalidad";
                            if (animal.getOwner() != null && animal.getOwner().getMunicipality() != null) {
                                var muni = animal.getOwner().getMunicipality();
                                if (muni.getName() != null && !muni.getName().isBlank()) {
                                    municipality = muni.getName();
                                }
                            }
                            String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                            String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                            grouped
                                    .computeIfAbsent(municipality, m -> new HashMap<>())
                                    .computeIfAbsent(species, s -> new HashMap<>())
                                    .merge(sex, 1L, Long::sum);
                        }
                        for (AbandonedAnimal animal : abandoned) {
                            String municipality = "Sin municipalidad";
                            if (animal.getCreatedBy() != null && animal.getCreatedBy().getMunicipality() != null) {
                                var muni = animal.getCreatedBy().getMunicipality();
                                if (muni.getName() != null && !muni.getName().isBlank()) {
                                    municipality = muni.getName();
                                }
                            }
                            String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                            String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                            grouped
                                    .computeIfAbsent(municipality, m -> new HashMap<>())
                                    .computeIfAbsent(species, s -> new HashMap<>())
                                    .merge(sex, 1L, Long::sum);
                        }
                        for (CommunityAnimal animal : community) {
                            String municipality = "Sin municipalidad";
                            var muni = animal.getMunicipality();
                            if (muni != null && muni.getName() != null && !muni.getName().isBlank()) {
                                municipality = muni.getName();
                            }
                            String species = animal.getSpecies() != null ? animal.getSpecies().getName() : "";
                            String sex = animal.getSex() != null ? animal.getSex().getName() : "";
                            grouped
                                    .computeIfAbsent(municipality, m -> new HashMap<>())
                                    .computeIfAbsent(species, s -> new HashMap<>())
                                    .merge(sex, 1L, Long::sum);
                        }
                        document.add(new Paragraph("Municipalidad | Especie | Sexo | Cantidad"));
                        for (var municipalityEntry : grouped.entrySet()) {
                            for (var speciesEntry : municipalityEntry.getValue().entrySet()) {
                                for (var sexEntry : speciesEntry.getValue().entrySet()) {
                                    document.add(new Paragraph(
                                            municipalityEntry.getKey() + " | " +
                                                    speciesEntry.getKey() + " | " +
                                                    sexEntry.getKey() + " | " +
                                                    sexEntry.getValue()
                                    ));
                                }
                            }
                        }
                        break;
                    }
                    default:
                        document.add(new Paragraph("No hay datos para este tipo de reporte"));
                }
                document.close();
            } catch (DocumentException e) {
                return ResponseEntity.internalServerError().body(null);
            }
            byte[] data = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-" + type + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(data.length)
                    .body(resource);
        }
    }
