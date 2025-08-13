package com.bienestar.animal.reports;

/**
 * @author nav
 */
public class MascotasPorDistritoDTO {
    private String districtCode;
    private String districtName;
    private int totalPets;

    public MascotasPorDistritoDTO(String districtCode, String districtName, int totalPets) {
        this.districtCode = districtCode;
        this.districtName = districtName;
        this.totalPets = totalPets;
    }
    public String getDistrictCode() { return districtCode; }
    public String getDistrictName() { return districtName; }
    public int getTotalPets() { return totalPets; }
}
