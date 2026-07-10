package com.lifeline.lifeline.service;

public interface GeocodingService {
    double[] geocodeAddress(String address); // returns [longitude, latitude]
}