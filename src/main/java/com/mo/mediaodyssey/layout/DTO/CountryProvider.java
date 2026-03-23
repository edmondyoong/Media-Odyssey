package com.mo.mediaodyssey.layout.DTO;

import java.util.List;

public class CountryProvider {
    private List<Provider> flatrate;

    public CountryProvider () {}

    public CountryProvider(List<Provider> flatrate) {
        this.flatrate = flatrate; 
    }

    public List<Provider> getFlatrate() {
        return flatrate;
    }

    public void setFlatrate(List<Provider> flatrate) {
        this.flatrate = flatrate;
    }

}
