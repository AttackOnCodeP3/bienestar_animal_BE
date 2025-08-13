package com.bienestar.animal.reports;

/*@author nav */

public class AnimalesCallejerosDTO {
    private String month;
    private String area;
    private int totalStreetAnimals;

    public AnimalesCallejerosDTO(String month, String area, int totalStreetAnimals) {
        this.month = month;
        this.area = area;
        this.totalStreetAnimals = totalStreetAnimals;
    }
    public String getMonth() { return month; }
    public String getArea() { return area; }
    public int getTotalStreetAnimals() { return totalStreetAnimals; }
}
