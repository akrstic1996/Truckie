package org.krstic.helper;

import jakarta.annotation.PostConstruct;
import org.krstic.model.Truck;
import org.krstic.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class TruckImportHelper {

    @Autowired
    private TruckRepository truckRepository;

    private static final String CSV_FILE = "/trucks.csv"; // located in src/main/resources

    @PostConstruct
    public void importTrucks() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(CSV_FILE)))) {

            String line;
            boolean isFirstLine = true;

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            int updated = 0;
            int inserted = 0;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }

                String[] columns = line.split(",", -1);
                if (columns.length < 9) continue;

                String truckNumber = columns[0].trim();
                int year = Integer.parseInt(columns[1].trim());
                String make = columns[2].trim();
                String model = columns[3].trim();
                String vin = columns[4].trim();

                String iftaStr = columns[5].trim();
                String regStr = columns[6].trim();
                String insStr = columns[7].trim();
                String faiStr = columns[8].trim();

                Optional<Truck> optionalTruck = truckRepository.findByTruckNumber(truckNumber);
                Truck truck;

                if (optionalTruck.isPresent()) {
                    truck = optionalTruck.get();
                    updated++;
                } else {
                    truck = new Truck();
                    truck.setTruckNumber(truckNumber);
                    truck.setIsInspected(false);
                    truck.setNeedsInspection(true);
                    inserted++;
                }

                truck.setYear(year);
                truck.setMake(make);
                truck.setModel(model);
                truck.setVin(vin);
                truck.setIftaExpiry(parseDateSafe(dateFormat, iftaStr));
                truck.setRegistrationExpiry(parseDateSafe(dateFormat, regStr));
                truck.setInsuranceExpiry(parseDateSafe(dateFormat, insStr));
                truck.setFaiDue(parseDateSafe(dateFormat, faiStr));

                truckRepository.save(truck);
            }

            System.out.printf("✅ Imported: %d new, %d updated trucks from CSV%n", inserted, updated);

        } catch (Exception e) {
            System.err.println("❌ Failed to import trucks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Date parseDateSafe(SimpleDateFormat sdf, String input) {
        try {
            return (input == null || input.isEmpty()) ? null : sdf.parse(input);
        } catch (Exception e) {
            System.err.println("⚠️ Invalid date format: " + input);
            return null;
        }
    }
}
