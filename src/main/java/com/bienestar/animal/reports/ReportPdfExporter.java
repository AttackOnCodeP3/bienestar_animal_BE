
package com.bienestar.animal.reports;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * author nav
 */

public class ReportPdfExporter {
    public static byte[] export(ReportType type, List<?> data) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float y = yStart;
            float leading = 18;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Reporte de " + type.name());
            contentStream.endText();
            y -= leading;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            contentStream.endText();
            y -= leading * 2;

            String[] headers;
            List<String[]> rows = new java.util.ArrayList<>();
            switch (type) {
                case MASCOTAS_POR_MUNICIPALIDAD:
                    headers = new String[]{"Código Distrito", "Nombre Distrito", "Total Mascotas"};
                    for (MascotasPorDistritoDTO dto : (List<MascotasPorDistritoDTO>) data) {
                        rows.add(new String[]{dto.getDistrictCode(), dto.getDistrictName(), String.valueOf(dto.getTotalPets())});
                    }
                    break;
                case ANIMALES_CALLEJEROS:
                    headers = new String[]{"Mes", "Área", "Total Callejeros"};
                    for (AnimalesCallejerosDTO dto : (List<AnimalesCallejerosDTO>) data) {
                        rows.add(new String[]{dto.getMonth(), dto.getArea(), String.valueOf(dto.getTotalStreetAnimals())});
                    }
                    break;
                case ANIMALES_CON_HOGAR:
                    headers = new String[]{"Distrito", "Con Hogar", "Atención Médica", "Esterilizados"};
                    for (AnimalesConHogarDTO dto : (List<AnimalesConHogarDTO>) data) {
                        rows.add(new String[]{dto.getDistrict(), String.valueOf(dto.getWithHome()), String.valueOf(dto.getMedicalAttention()), String.valueOf(dto.getSterilized())});
                    }
                    break;
                case INDICADORES_ABANDONO:
                    headers = new String[]{"Categoría", "Distrito", "Cantidad"};
                    for (IndicadoresMaltratoDTO dto : (List<IndicadoresMaltratoDTO>) data) {
                        rows.add(new String[]{dto.getCategory(), dto.getDistrict() != null ? dto.getDistrict() : "", String.valueOf(dto.getCount())});
                    }
                    break;
                default:
                    headers = new String[]{"No hay datos para el reporte seleccionado."};
            }

            float tableX = margin;
            float tableY = y;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(tableX, tableY);
            for (int i = 0; i < headers.length; i++) {
                contentStream.showText(headers[i] + (i < headers.length - 1 ? "    " : ""));
            }
            contentStream.endText();
            tableY -= leading;

            contentStream.setFont(PDType1Font.HELVETICA, 11);
            for (String[] row : rows) {
                contentStream.beginText();
                contentStream.newLineAtOffset(tableX, tableY);
                for (int i = 0; i < row.length; i++) {
                    contentStream.showText(row[i] + (i < row.length - 1 ? "    " : ""));
                }
                contentStream.endText();
                tableY -= leading;
            }

            contentStream.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
