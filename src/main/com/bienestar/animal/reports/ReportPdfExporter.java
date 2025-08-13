package com.bienestar.animal.reports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportPdfExporter {
    public static byte[] export(ReportType type, List<?> data) {
        String content = "Reporte de " + type.name() + "\nGenerado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n\n";
        content += "(Tabla de datos aquí)\n";
        return content.getBytes(); 
    }
}
