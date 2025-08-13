package com.bienestar.animal.reports;

/**
 * @author nav
 */
public class IndicadoresMaltratoDTO {
    private String category;
    private String district;
    private int count;

    public IndicadoresMaltratoDTO(String category, String district, int count) {
        this.category = category;
        this.district = district;
        this.count = count;
    }
    public String getCategory() { return category; }
    public String getDistrict() { return district; }
    public int getCount() { return count; }
}
