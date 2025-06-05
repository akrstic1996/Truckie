package org.krstic.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Date;

@Data
@Document("trucks")
public class Truck {

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getUserLastInspected() {
        return userLastInspected;
    }

    public void setUserLastInspected(String userLastInspected) {
        this.userLastInspected = userLastInspected;
    }

    public boolean needsInspection() {
        return needsInspection;
    }

    public void setNeedsInspection(boolean needsInspection) {
        this.needsInspection = needsInspection;
    }

    public boolean isInspected() {
        return isInspected;
    }

    public void setIsInspected(boolean isInspected) {
        this.isInspected = isInspected;
    }

    public String getLastComment() {
        return lastComment;
    }

    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }


    @Id
    private String id;
    private String truckNumber;
    private String make;
    private String model;
    private Integer year;
    private String vin;
    private Integer mileage;
    private String userLastInspected;
    private boolean needsInspection = true;
    private boolean isInspected = false;
    private String lastComment;
    private Date iftaExpiry;
    private Date insuranceExpiry;
    private Date registrationExpiry;
    private Date faiDue;

}
