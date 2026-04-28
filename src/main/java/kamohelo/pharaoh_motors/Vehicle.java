package kamohelo.pharaoh_motors;

public class Vehicle {
    private int id;
    private String registrationNumber;
    private String make;
    private String model;
    private int year;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;

    // Existing constructor
    public Vehicle(String registrationNumber, String make, String model, int year, String ownerName) {
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.ownerName = ownerName;
    }

    // New constructor with phone and email
    public Vehicle(String registrationNumber, String make, String model, int year,
                   String ownerName, String ownerPhone, String ownerEmail) {
        this(registrationNumber, make, model, year, ownerName);
        this.ownerPhone = ownerPhone;
        this.ownerEmail = ownerEmail;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}