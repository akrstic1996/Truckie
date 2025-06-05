package org.krstic.repository;

import org.krstic.model.Truck;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TruckRepository extends MongoRepository<Truck, String> {
    Optional<Truck> findByTruckNumber(String truckNumber);

    Optional<Truck> findByVin(String vin);
}