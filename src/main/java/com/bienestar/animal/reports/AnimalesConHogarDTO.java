package com.bienestar.animal.reports;

/**
 * @author nav
 */
public class AnimalesConHogarDTO {
    private String district;
    private int withHome;
    private int medicalAttention;
    private int sterilized;

    public AnimalesConHogarDTO(String district, int withHome, int medicalAttention, int sterilized) {
        this.district = district;
        this.withHome = withHome;
        this.medicalAttention = medicalAttention;
        this.sterilized = sterilized;
    }
    public String getDistrict() { return district; }
    public int getWithHome() { return withHome; }
    public int getMedicalAttention() { return medicalAttention; }
    public int getSterilized() { return sterilized; }
}
