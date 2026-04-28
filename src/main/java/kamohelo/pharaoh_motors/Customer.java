package kamohelo.pharaoh_motors;

import java.time.LocalDateTime;

public class Customer extends BaseEntity {
    private String name;
    private String address;
    private String phone;
    private String email;

    public Customer(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public Customer(int id, String name, String address, String phone, String email, LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.createdBy = createdBy != null ? createdBy : "System";
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Customer: %s (%s)", name, email);
    }

    @Override
    public String getEntityType() {
        return "CUSTOMER";
    }

    @Override
    public String toFileFormat() {
        return String.format("%d|%s|%s|%s|%s|%s|%s", id, name, address, phone, email, createdAt.toString(), createdBy);
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}