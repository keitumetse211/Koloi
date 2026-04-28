package kamohelo.pharaoh_motors;

import java.time.LocalDate;

public class PoliceReport extends BaseEntity {
    private String vehicleRegNumber;
    private LocalDate reportDate;
    private String reportType;
    private String description;
    private String officerName;
    private String status;

    public PoliceReport(String vehicleRegNumber, LocalDate reportDate,
                        String reportType, String description, String officerName) {
        this.vehicleRegNumber = vehicleRegNumber;
        this.reportDate = reportDate;
        this.reportType = reportType;
        this.description = description;
        this.officerName = officerName;
        this.status = "Under Investigation";
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Police Report: %s on %s - %s (Officer: %s)",
                reportType, vehicleRegNumber, status, officerName);
    }

    @Override
    public String getEntityType() {
        return "POLICE_REPORT";
    }

    @Override
    public String toFileFormat() {
        return String.format("%d|%s|%s|%s|%s|%s|%s|%s",
                id, vehicleRegNumber, reportDate, reportType, description, officerName, status, getFormattedDate());
    }

    // Getters and Setters
    public String getVehicleRegNumber() { return vehicleRegNumber; }
    public LocalDate getReportDate() { return reportDate; }
    public String getReportType() { return reportType; }
    public String getDescription() { return description; }
    public String getOfficerName() { return officerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}