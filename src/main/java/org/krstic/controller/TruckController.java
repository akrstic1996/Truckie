package org.krstic.controller;

import org.krstic.model.Truck;
import org.krstic.repository.TruckRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/truckie")
public class TruckController {

    private final TruckRepository truckRepository;

    public TruckController(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    @PostMapping("/listAll")
    public ResponseEntity<Map<String, String>> listAll(@RequestParam Map<String, String> params) {
        List<Truck> allTrucks = truckRepository.findAll();

        if (allTrucks.isEmpty()) {
            return ResponseEntity.ok(Map.of("text", "No trucks found in the system."));
        }

        StringBuilder toInspect = new StringBuilder("*Trucks left to inspect:*\n");
        StringBuilder omitted = new StringBuilder("*Trucks omitted from inspection:*\n");
        StringBuilder inspectedResult = new StringBuilder("*Trucks inspected:*\n");

        for (Truck truck : allTrucks) {
            String notInspected = String.format("%s\n", truck.getTruckNumber());
            String doesntNeedInspection = String.format("%s\n", truck.getTruckNumber());
            String inspected = String.format("%s - %s - %s - %s\n",
                    truck.getTruckNumber(),
                    truck.getMileage() != null ? truck.getMileage() : "N/A",
                    truck.getLastComment() != null ? truck.getLastComment() : "None",
                    truck.getUserLastInspected() != null ? "<@" + truck.getUserLastInspected() + ">" : "N/A"
            );

            if (!truck.needsInspection()) {
                omitted.append(doesntNeedInspection);
            } else if (truck.isInspected()) {
                inspectedResult.append(inspected);
            } else {
                toInspect.append(notInspected);
            }
        }

        String finalText = String.format("%s\n%s\n%s", toInspect, omitted, inspectedResult);

        return ResponseEntity.ok(Map.of(
                "response_type", "in_channel",
                "text", finalText
        ));
    }

    @PostMapping("/details")
    public ResponseEntity<Map<String, String>> details(@RequestParam Map<String, String> params) {
        String text = params.get("text");

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("text", "Usage: /details <truckNumber>"));
        }

        String truckNumber = text.trim();
        Optional<Truck> optionalTruck = truckRepository.findByTruckNumber(truckNumber);

        if (optionalTruck.isEmpty()) {
            return ResponseEntity.ok(Map.of("text", "Truck not found: " + truckNumber));
        }

        Truck truck = optionalTruck.get();

        return ResponseEntity.ok(Map.of(
                "response_type", "in_channel",
                "text", buildTruckDetailMessage(truck)
        ));
    }

    @PostMapping("/find")
    public ResponseEntity<Map<String, String>> findByVinEnding(
            @RequestParam Map<String, String> params) {
        String text = params.get("text");

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("text", "Usage: /find <last 6 of VIN>"));
        }

        String lastSix = text.trim().toUpperCase();
        if (lastSix.length() < 6) {
            return ResponseEntity.ok(Map.of("text", "Please provide at least 6 characters of the VIN."));
        }

        Optional<Truck> optionalTruck = truckRepository.findAll().stream()
                .filter(t -> t.getVin() != null && t.getVin().toUpperCase().endsWith(lastSix))
                .findFirst();

        if (optionalTruck.isEmpty()) {
            return ResponseEntity.ok(Map.of("text", "No truck found ending in VIN: " + lastSix));
        }

        return ResponseEntity.ok(Map.of(
                "response_type", "in_channel",
                "text", buildTruckDetailMessage(optionalTruck.get())
        ));
    }


    private String buildTruckDetailMessage(Truck truck) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        String inspectedBy = truck.getUserLastInspected() != null ? "<@" + truck.getUserLastInspected() + ">" : "N/A";
        String comments = truck.getLastComment() != null ? truck.getLastComment() : "None";

        String ifta = formatDate(truck.getIftaExpiry(), sdf);
        String insurance = formatDate(truck.getInsuranceExpiry(), sdf);
        String registration = formatDate(truck.getRegistrationExpiry(), sdf);
        String fai = formatDate(truck.getFaiDue(), sdf);

        StringBuilder sb = new StringBuilder();
        sb.append("*🛻 ").append(truck.getTruckNumber()).append("*\n");
        sb.append("🆔 *VIN:* ").append(truck.getVin() != null ? truck.getVin() : "N/A").append("\n");
        sb.append("🏷️ *Make:* ").append(truck.getMake()).append("\n");
        sb.append("🏷️ *Model:* ").append(truck.getModel()).append("\n");
        sb.append("🏷️ *Year:* ").append(truck.getYear()).append("\n");
        sb.append("💬 *Last Comment:* ").append(comments).append("\n");
        sb.append("👷 *Last Inspected By:* ").append(inspectedBy).append("\n\n");
        sb.append("🧾 *IFTA Expiration Date:* ").append(ifta).append("\n");
        sb.append("🛡️ *Insurance Expiration Date:* ").append(insurance).append("\n");
        sb.append("📝 *Registration Expiration Date:* ").append(registration).append("\n");
        sb.append("🔍 *Annual Inspection Due:* ").append(fai);

        return sb.toString();
    }


    private String formatDate(Date date, SimpleDateFormat sdf) {
        return (date == null) ? "N/A" : sdf.format(date);
    }

    @PostMapping("/addNew")
    public ResponseEntity<Map<String, String>> addNew(@RequestParam Map<String, String> params) {
        String text = params.get("text");
        String[] parts = text.split(" ", 4);
        if (parts.length < 4) {
            return ResponseEntity.ok(Map.of("text", "Usage: /addNew <truckNumber> <make> <model> <year>"));
        }

        String truckNumber = parts[0];
        String make = parts[1];
        String model = parts[2];
        int year;

        try {
            year = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return ResponseEntity.ok(Map.of("text", "Invalid year format. Please use a 4-digit number."));
        }

        if (truckRepository.findByTruckNumber(truckNumber).isPresent()) {
            return ResponseEntity.ok(Map.of("text", "Truck with this number already exists."));
        }

        Truck truck = new Truck();
        truck.setTruckNumber(truckNumber);
        truck.setMake(make);
        truck.setModel(model);
        truck.setYear(year);
        truck.setIsInspected(false);
        truck.setNeedsInspection(true);

        truckRepository.save(truck);

        return ResponseEntity.ok(Map.of("text", "Truck added successfully."));
    }

    @PostMapping("/inspect")
    public ResponseEntity<Map<String, String>> inspect(@RequestParam Map<String, String> params) {
        String text = params.get("text");
        String userId = params.get("user_id");
        String[] parts = text.split(" ", 3);

        if (parts.length < 3) {
            return ResponseEntity.ok(Map.of("text", "Usage: /inspect <truckNumber> <mileage> <comments>"));
        }

        String truckNumber = parts[0];
        int mileage;
        try {
            mileage = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return ResponseEntity.ok(Map.of("text", "Mileage must be a number."));
        }
        String comments = parts[2];

        Optional<Truck> optionalTruck = truckRepository.findByTruckNumber(truckNumber);
        if (optionalTruck.isEmpty()) {
            return ResponseEntity.ok(Map.of("text", "Truck not found."));
        }

        Truck truck = optionalTruck.get();
        truck.setIsInspected(true);
        truck.setMileage(mileage);
        truck.setLastComment(comments);
        truck.setUserLastInspected(userId);

        truckRepository.save(truck);

        return ResponseEntity.ok(Map.of("text", "Truck inspection updated successfully."));
    }

    @PostMapping("/startInspection")
    public ResponseEntity<Map<String, String>> startInspection(@RequestParam Map<String, String> params) {
        String text = params.get("text");
        Set<String> omitTruckNumbers = new HashSet<>();

        if (text != null && !text.trim().isEmpty()) {
            String[] parts = text.split(",");
            for (String number : parts) {
                omitTruckNumbers.add(number.trim());
            }
        }

        List<Truck> allTrucks = truckRepository.findAll();
        for (Truck truck : allTrucks) {
            truck.setIsInspected(false);

            if (omitTruckNumbers.contains(truck.getTruckNumber())) {
                truck.setNeedsInspection(false);
            } else {
                truck.setNeedsInspection(true);
            }
        }

        truckRepository.saveAll(allTrucks);

        String omittedText = omitTruckNumbers.isEmpty()
                ? "None"
                : String.join(", ", omitTruckNumbers);

        String message = String.format(
                "✅ Inspection started!\n" +
                        "Use `/inspect` to log an inspection.\n" +
                        "Use `/listAll` to view the status of the entire fleet.\n\n" +
                        "*The following trucks do not need to be inspected:* %s",
                omittedText
        );

        return ResponseEntity.ok(Map.of(
                "response_type", "in_channel",
                "text", message
        ));
    }
}