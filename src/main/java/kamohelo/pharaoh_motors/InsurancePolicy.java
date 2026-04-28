package kamohelo.pharaoh_motors;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsurancePolicy extends BaseEntity {
    private String vehicleRegNumber;
    private String policyNumber;
    private String insurerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double premium;
    private String coverage;

    public InsurancePolicy(String vehicleRegNumber, String policyNumber, String insurerName,
                           LocalDate startDate, LocalDate endDate, double premium, String coverage) {
        this.vehicleRegNumber = vehicleRegNumber;
        this.policyNumber = policyNumber;
        this.insurerName = insurerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.premium = premium;
        this.coverage = coverage;
    }

    public InsurancePolicy(int id, String vehicleRegNumber, String policyNumber, String insurerName,
                           LocalDate startDate, LocalDate endDate, double premium, String coverage,
                           LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.vehicleRegNumber = vehicleRegNumber;
        this.policyNumber = policyNumber;
        this.insurerName = insurerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.premium = premium;
        this.coverage = coverage;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.createdBy = createdBy != null ? createdBy : "System";
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Insurance for %s: %s - %s to %s (Premium: $%.2f)",
                vehicleRegNumber, insurerName, startDate, endDate, premium);
    }

    @Override
    public String getEntityType() {
        return "INSURANCE_POLICY";
    }

    @Override
    public String toFileFormat() {
        return String.format("%d|%s|%s|%s|%s|%s|%.2f|%s|%s|%s",
                id, vehicleRegNumber, policyNumber, insurerName, startDate, endDate,
                premium, coverage, createdAt.toString(), createdBy);
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return today.isAfter(startDate) && today.isBefore(endDate);
    }

    // Getters
    public String getVehicleRegNumber() { return vehicleRegNumber; }
    public String getPolicyNumber() { return policyNumber; }
    public String getInsurerName() { return insurerName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getPremium() { return premium; }
    public String getCoverage() { return coverage; }

    // Setters
    public void setVehicleRegNumber(String vehicleRegNumber) { this.vehicleRegNumber = vehicleRegNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public void setInsurerName(String insurerName) { this.insurerName = insurerName; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setPremium(double premium) { this.premium = premium; }
    public void setCoverage(String coverage) { this.coverage = coverage; }
}