package com.bienestar.animal.reports;

import org.springframework.stereotype.Service;
import java.util.*;

/*
 * author nav
 */

@Service
public class ReportService {

    public List<?> getReportSummary(ReportType type) {
        switch (type) {
            case MASCOTAS_POR_MUNICIPALIDAD:
                return List.of(new MascotasPorDistritoDTO("001", "Centro", 120), new MascotasPorDistritoDTO("002", "Norte", 85));
            case ANIMALES_CALLEJEROS:
                return List.of(new AnimalesCallejerosDTO("2025-07", "Centro", 15), new AnimalesCallejerosDTO("2025-07", "Norte", 8));
            case ANIMALES_CON_HOGAR:
                return List.of(new AnimalesConHogarDTO("Centro", 100, 80, 60), new AnimalesConHogarDTO("Norte", 70, 50, 40));
            case INDICADORES_ABANDONO:
                return List.of(new IndicadoresMaltratoDTO("Abandono", "Centro", 5), new IndicadoresMaltratoDTO("Maltrato", "Norte", 3));
            default:
                return Collections.emptyList();
        }
    }

}
