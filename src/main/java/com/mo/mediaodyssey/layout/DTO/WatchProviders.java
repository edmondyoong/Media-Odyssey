package com.mo.mediaodyssey.layout.DTO;

import java.util.Map;

public class WatchProviders {
    private Map<String, CountryProvider> results;

    public WatchProviders (){}

    public WatchProviders(Map<String, CountryProvider> results){
        this.results = results; 
    }

    public Map<String, CountryProvider> getResults() {
        return results;
    }

    public void setResults(Map<String, CountryProvider> results) {
        this.results = results;
    }
    
}
