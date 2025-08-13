package com.bienestar.animal.reports;

import java.util.List;

/**
 * @author nav
 */

public class ReportCsvExporter {
    public static byte[] export(ReportType type, List<?> data) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case MASCOTAS_POR_MUNICIPALIDAD:
                sb.append("districtCode,districtName,totalPets\n");
                for (MascotasPorDistritoDTO dto : (List<MascotasPorDistritoDTO>) data) {
                    sb.append(String.format("%s,%s,%d\n", dto.getDistrictCode(), dto.getDistrictName(), dto.getTotalPets()));
                }
                break;
            case ANIMALES_CALLEJEROS:
                sb.append("month,area,totalStreetAnimals\n");
                for (AnimalesCallejerosDTO dto : (List<AnimalesCallejerosDTO>) data) {
                    sb.append(String.format("%s,%s,%d\n", dto.getMonth(), dto.getArea(), dto.getTotalStreetAnimals()));
                }
                break;
            case ANIMALES_CON_HOGAR:
                sb.append("district,withHome,medicalAttention,sterilized\n");
                for (AnimalesConHogarDTO dto : (List<AnimalesConHogarDTO>) data) {
                    sb.append(String.format("%s,%d,%d,%d\n", dto.getDistrict(), dto.getWithHome(), dto.getMedicalAttention(), dto.getSterilized()));
                }
                break;
            case INDICADORES_ABANDONO:
                sb.append("category,district,count\n");
                for (IndicadoresMaltratoDTO dto : (List<IndicadoresMaltratoDTO>) data) {
                    sb.append(String.format("%s,%s,%d\n", dto.getCategory(), dto.getDistrict(), dto.getCount()));
                }
                break;
        }
        return sb.toString().getBytes();
    }
}
