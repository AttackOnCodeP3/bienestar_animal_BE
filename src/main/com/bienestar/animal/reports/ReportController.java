package com.bienestar.animal.reports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<List<?>> getSummary(@RequestParam ReportType type) {
        List<?> result = reportService.getReportSummary(type);
        return ResponseEntity.ok()
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(result);
    }

    @GetMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCsv(@RequestParam ReportType type) {
        byte[] csv = "header1,header2\nvalue1,value2".getBytes(); 
        String filename = String.format("reporte-%s-%s.csv", type.name().toLowerCase(), LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam ReportType type) {
        byte[] pdf = new byte[0]; 
        String filename = String.format("reporte-%s-%s.pdf", type.name().toLowerCase(), LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
