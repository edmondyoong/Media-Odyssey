package com.mo.mediaodyssey.layout.DTO.MoviesTMDB;

public class Provider {
    private String provider_name;

    public Provider() {}

    public Provider( String provider_name) {
        this.provider_name = provider_name;
    }

    public String getProvider_name() {
        return provider_name;
    }

    public void setProvider_name(String provider_name) {
        this.provider_name = provider_name;
    }

}
