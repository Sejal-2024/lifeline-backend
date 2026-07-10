package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.dto.request.RegisterHospitalRequest;
import com.lifeline.lifeline.dto.response.HospitalResponse;
import com.lifeline.lifeline.exception.ResourceNotFoundException;
import com.lifeline.lifeline.model.Hospital;
import com.lifeline.lifeline.model.HospitalStatus;
import com.lifeline.lifeline.repository.HospitalRepository;
import com.lifeline.lifeline.repository.UserRepository;
import com.lifeline.lifeline.service.GeocodingService;
import com.lifeline.lifeline.service.HospitalService;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.geo.Distance;
//import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final GeocodingService geocodingService;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public HospitalResponse registerHospital(RegisterHospitalRequest request, String adminEmail) {

        String adminUserId = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"))
                .getId();

        double[] coordinates = geocodingService.geocodeAddress(request.getAddress());
        GeoJsonPoint location = new GeoJsonPoint(coordinates[0], coordinates[1]);

        Hospital hospital = Hospital.builder()
                .name(request.getName())
                .address(request.getAddress())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .location(location)
                .totalBeds(request.getTotalBeds())
                .availableBeds(request.getTotalBeds())
                .adminUserId(adminUserId)
                .status(HospitalStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        hospitalRepository.save(hospital);

        return toResponse(hospital);
    }

    @Override
    public List<HospitalResponse> getMyHospitals(String adminEmail) {
        String adminUserId = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"))
                .getId();

        return hospitalRepository.findByAdminUserId(adminUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public HospitalResponse getHospitalById(String id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return toResponse(hospital);
    }

    private HospitalResponse toResponse(Hospital hospital) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .contactPhone(hospital.getContactPhone())
                .contactEmail(hospital.getContactEmail())
                .latitude(hospital.getLocation().getY())
                .longitude(hospital.getLocation().getX())
                .totalBeds(hospital.getTotalBeds())
                .availableBeds(hospital.getAvailableBeds())
                .status(hospital.getStatus())
                .build();
    }

    @Override
    public List<HospitalResponse> findNearbyHospitals(double latitude, double longitude, double radiusKm) {

        GeoJsonPoint point = new GeoJsonPoint(longitude, latitude);
        double radiusInMeters = radiusKm * 1000;

        Criteria criteria = Criteria.where("location")
                .nearSphere(point)
                .maxDistance(radiusInMeters)
                .and("status").is(HospitalStatus.ACTIVE);

        Query query = new Query(criteria);

        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);

        return hospitals.stream()
                .map(this::toResponse)
                .toList();
    }
}