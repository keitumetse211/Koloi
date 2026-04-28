package kamohelo.pharaoh_motors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseEntity {
    protected int id;
    protected LocalDateTime createdAt;
    protected String createdBy;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.createdBy = "System";
    }

    // Abstract methods - Polymorphism in action
    public abstract String getDisplayInfo();
    public abstract String getEntityType();
    public abstract String toFileFormat();

    // Concrete methods
    public String getFormattedDate() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}