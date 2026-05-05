package kamohelo.pharaoh_motors;

import javafx.animation.FadeTransition;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Pharaohmotors implements Initializable {

    // Tab Pane
    @FXML private TabPane mainTabPane;

    // TableView and Columns
    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String> regColumn;
    @FXML private TableColumn<Vehicle, String> makeColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;
    @FXML private TableColumn<Vehicle, String> ownerColumn;
    @FXML private TableColumn<Vehicle, String> phoneColumn;

    // Progress Indicators
    @FXML private ProgressBar progressBar;
    @FXML private ProgressIndicator progressIndicator;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button deleteButton;

    // Demo Components
    @FXML private VBox scrollContent;
    @FXML private Pagination pagination;

    // Sidebar Components
    @FXML private Pane sidebarPane;
    @FXML private Button toggleSidebarBtn;
    @FXML private Button revealSidebarBtn;
    @FXML private Pane contentPane;
    @FXML private AnchorPane rootPane;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;

    // Registration Form Fields
    @FXML private TextField regNumberField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField ownerField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> makeComboBox;

    // Dashboard Labels
    @FXML private Label totalVehiclesLabel;
    @FXML private Label totalOwnersLabel;
    @FXML private Label serviceCountLabel;
    @FXML private Label violationCountLabel;
    @FXML private Label timeLabel;
    @FXML private Label insuranceCountLabel;

    // Workshop Module Fields
    @FXML private ComboBox<String> serviceVehicleCombo;
    @FXML private ComboBox<String> historyVehicleCombo;
    @FXML private ListView<String> serviceHistoryList;
    @FXML private Label totalServiceCost;
    @FXML private DatePicker serviceDatePicker;
    @FXML private ComboBox<String> serviceTypeCombo;
    @FXML private TextArea serviceDescriptionArea;
    @FXML private TextField serviceCostField;

    // Insurance Module Fields
    @FXML private ComboBox<String> insuranceVehicleCombo;
    @FXML private TextField policyNumberField;
    @FXML private TextField insurerNameField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField premiumField;
    @FXML private TextArea coverageArea;
    @FXML private ComboBox<String> viewInsuranceVehicleCombo;
    @FXML private TextArea insuranceDetailsArea;
    @FXML private ListView<String> expiringInsuranceList;

    // Police Module Fields
    @FXML private ComboBox<String> violationVehicleCombo;
    @FXML private ListView<String> unpaidViolationsList;
    @FXML private DatePicker violationDatePicker;
    @FXML private ComboBox<String> violationTypeCombo;
    @FXML private TextField fineAmountField;
    @FXML private TextField officerNameField;

    // Customer Support Fields
    @FXML private ComboBox<String> queryVehicleCombo;
    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private TextArea queryTextArea;
    @FXML private ListView<String> customerQueriesList;

    // Reports Fields
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private TextArea reportPreviewArea;

    // Database and User
    private DatabaseConnection db;
    private User currentUser;
    private ObservableList<Vehicle> vehicleData;
    private Timer clockTimer;
    private boolean isSidebarVisible = true;
    private TranslateTransition sidebarAnimation;
    private PauseTransition hideSidebarTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vehicleData = FXCollections.observableArrayList();
        setupTableColumns();
        setupMakeComboBox();
        setupServiceTypeCombo();
        setupViolationTypeCombo();
        loadScrollContent();
        startClock();
        System.out.println("=== Pharaoh Motors Initialized ===");
    }

    // Set Database Connection
    public void setDatabaseConnection(DatabaseConnection db) {
        this.db = db;
        System.out.println("Database connection set");
    }

    // Set Current User
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUIForUserRole();

        if (statusLabel != null) {
            statusLabel.setText("Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");
        }
        if (userLabel != null) userLabel.setText(user.getUsername());
        if (roleLabel != null) roleLabel.setText(user.getRole());

        loadDataFromDatabase();
        updateDashboardStats();
        loadUnpaidViolations();
        loadExpiringInsurance();
        setupAdditionalCombos();
        setupAnimations();
        setupPagination();
        setupCollapsibleSidebar();
    }

    // Setup Table Columns
    private void setupTableColumns() {
        regColumn.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        makeColumn.setCellValueFactory(new PropertyValueFactory<>("make"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        if (phoneColumn != null) {
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("ownerPhone"));
        }
    }

    // Setup Make ComboBox
    private void setupMakeComboBox() {
        if (makeComboBox != null) {
            makeComboBox.getItems().addAll("Toyota", "Honda", "Ford", "BMW", "Mercedes",
                    "Audi", "Nissan", "Hyundai", "Kia", "Volkswagen", "Mazda", "Subaru",
                    "Lexus", "Volvo", "Jeep", "Tesla", "Chevrolet", "Mitsubishi");
            makeComboBox.setEditable(true);
            makeComboBox.setPromptText("Select or type vehicle make");
        }
    }

    // Setup Service Type ComboBox
    private void setupServiceTypeCombo() {
        if (serviceTypeCombo != null) {
            serviceTypeCombo.getItems().clear();
            serviceTypeCombo.getItems().addAll(
                    "Oil Change",
                    "Tire Rotation",
                    "Brake Service",
                    "Engine Tune-up",
                    "Transmission Service",
                    "Battery Replacement",
                    "Air Filter Change",
                    "Wheel Alignment",
                    "Annual Inspection",
                    "Major Service",
                    "Diagnostic Test",
                    "Exhaust Repair",
                    "Coolant Flush",
                    "Fuel System Cleaning",
                    "AC Service",
                    "Spark Plug Replacement"
            );
            serviceTypeCombo.setPromptText("Select service type");
            serviceTypeCombo.setValue("Oil Change");
            System.out.println("✓ Service Type ComboBox populated with " + serviceTypeCombo.getItems().size() + " items");
        } else {
            System.out.println("✗ serviceTypeCombo is NULL - check FXML fx:id");
        }

        if (serviceDatePicker != null) {
            serviceDatePicker.setValue(LocalDate.now());
        }
    }

    // Setup Violation Type ComboBox
    private void setupViolationTypeCombo() {
        if (violationTypeCombo != null) {
            violationTypeCombo.getItems().clear();
            violationTypeCombo.getItems().addAll(
                    "Speeding",
                    "Parking Violation",
                    "Expired License",
                    "No Insurance",
                    "Running Red Light",
                    "Drunk Driving",
                    "Reckless Driving",
                    "Illegal Parking",
                    "Expired Registration",
                    "Window Tint Violation",
                    "Seatbelt Violation",
                    "Phone Use While Driving",
                    "Wrong Way Driving",
                    "Stop Sign Violation"
            );
            violationTypeCombo.setPromptText("Select violation type");
            violationTypeCombo.setValue("Speeding");
            System.out.println("✓ Violation Type ComboBox populated with " + violationTypeCombo.getItems().size() + " items");
        } else {
            System.out.println("✗ violationTypeCombo is NULL - check FXML fx:id");
        }

        if (violationDatePicker != null) {
            violationDatePicker.setValue(LocalDate.now());
        }
    }

    // Setup Animations
    private void setupAnimations() {
        if (addButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), addButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.5);
            fadeTransition.setCycleCount(Animation.INDEFINITE);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
        }
    }

    // Setup Pagination
    private void setupPagination() {
        if (pagination == null) return;

        pagination.setPageCount(5);
        pagination.setPageFactory(pageIndex -> {
            VBox pageContent = new VBox(8);
            pageContent.setPadding(new Insets(15));
            pageContent.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10;");

            for (int i = 1; i <= 10; i++) {
                int recordNum = (pageIndex * 10) + i;
                HBox recordBox = new HBox(10);
                recordBox.setStyle("-fx-padding: 8; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;");
                Label numLabel = new Label(String.format("%02d", recordNum));
                numLabel.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
                Label item = new Label("Demo Vehicle Record #" + recordNum);
                item.setStyle("-fx-text-fill: white;");
                recordBox.getChildren().addAll(numLabel, item);
                pageContent.getChildren().add(recordBox);
            }
            return pageContent;
        });
    }

    // Setup Collapsible Sidebar
    private void setupCollapsibleSidebar() {
        if (sidebarPane == null) return;

        hideSidebarTimer = new PauseTransition(Duration.seconds(60));
        hideSidebarTimer.setOnFinished(e -> {
            if (isSidebarVisible) {
                hideSidebar();
            }
        });

        if (rootPane != null) {
            rootPane.setOnMouseMoved(e -> {
                if (isSidebarVisible) {
                    hideSidebarTimer.playFromStart();
                }
                if (e.getX() < 15 && !isSidebarVisible) {
                    showSidebar();
                }
            });
        }
        hideSidebarTimer.playFromStart();
    }

    private void showSidebar() {
        if (!isSidebarVisible && sidebarPane != null) {
            sidebarAnimation = new TranslateTransition(Duration.millis(300), sidebarPane);
            sidebarAnimation.setToX(0);
            sidebarAnimation.play();
            isSidebarVisible = true;
            if (toggleSidebarBtn != null) toggleSidebarBtn.setText("◀");
            if (revealSidebarBtn != null) revealSidebarBtn.setVisible(false);
        }
    }

    private void hideSidebar() {
        if (isSidebarVisible && sidebarPane != null) {
            sidebarAnimation = new TranslateTransition(Duration.millis(300), sidebarPane);
            sidebarAnimation.setToX(-260);
            sidebarAnimation.play();
            isSidebarVisible = false;
            if (toggleSidebarBtn != null) toggleSidebarBtn.setText("▶");
            if (revealSidebarBtn != null) revealSidebarBtn.setVisible(true);
        }
    }

    // Load Data from Database
    private void loadDataFromDatabase() {
        if (db == null) {
            loadSampleData();
            return;
        }

        try {
            vehicleData.clear();
            String query = "SELECT * FROM vehicles ORDER BY vehicle_id";
            db.setPstmt(query);
            ResultSet rs = db.executeQuery();

            while (rs.next()) {
                Vehicle v = new Vehicle(
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("owner_name"),
                        rs.getString("owner_phone"),
                        rs.getString("owner_email")
                );
                v.setId(rs.getInt("vehicle_id"));
                vehicleData.add(v);
            }
            vehicleTable.setItems(vehicleData);
            if (statusLabel != null) {
                statusLabel.setText("Loaded " + vehicleData.size() + " vehicles from database");
            }
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
            loadSampleData();
        }
    }

    // Load Sample Data (Fallback)
    private void loadSampleData() {
        vehicleData.clear();
        vehicleData.addAll(
                new Vehicle("ABC123", "Toyota", "Camry", 2020, "John Doe", "+27 12 345 6789", "john@example.com"),
                new Vehicle("XYZ789", "Honda", "Civic", 2019, "Jane Smith", "+27 23 456 7890", "jane@example.com"),
                new Vehicle("DEF456", "Ford", "Mustang", 2021, "Bob Johnson", "+27 34 567 8901", "bob@example.com")
        );
        vehicleTable.setItems(vehicleData);
        if (statusLabel != null) {
            statusLabel.setText("Showing sample data (Database not connected)");
        }
    }

    // Load Scroll Content
    private void loadScrollContent() {
        if (scrollContent == null) return;
        scrollContent.getChildren().clear();
        for (int i = 1; i <= 35; i++) {
            HBox itemBox = new HBox(10);
            itemBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;");
            Label numberLabel = new Label(String.format("%03d", i));
            numberLabel.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
            Label itemLabel = new Label("Demo Vehicle Record #" + i);
            itemLabel.setStyle("-fx-text-fill: white;");
            itemBox.getChildren().addAll(numberLabel, itemLabel);
            scrollContent.getChildren().add(itemBox);
        }
    }

    // Setup Additional Combos
    private void setupAdditionalCombos() {
        ObservableList<String> vehicleList = FXCollections.observableArrayList();
        for (Vehicle v : vehicleData) {
            vehicleList.add(v.getRegistrationNumber() + " - " + v.getMake() + " " + v.getModel());
        }

        if (serviceVehicleCombo != null) serviceVehicleCombo.setItems(vehicleList);
        if (historyVehicleCombo != null) historyVehicleCombo.setItems(vehicleList);
        if (insuranceVehicleCombo != null) insuranceVehicleCombo.setItems(vehicleList);
        if (viewInsuranceVehicleCombo != null) viewInsuranceVehicleCombo.setItems(vehicleList);
        if (violationVehicleCombo != null) violationVehicleCombo.setItems(vehicleList);
        if (queryVehicleCombo != null) queryVehicleCombo.setItems(vehicleList);

        if (startDatePicker != null) startDatePicker.setValue(LocalDate.now());
        if (endDatePicker != null) endDatePicker.setValue(LocalDate.now().plusYears(1));
        if (serviceDatePicker != null) serviceDatePicker.setValue(LocalDate.now());
        if (violationDatePicker != null) violationDatePicker.setValue(LocalDate.now());
    }

    // Start Clock
    private void startClock() {
        clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (timeLabel != null) {
                        timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                });
            }
        }, 0, 1000);
    }

    // Update Dashboard Stats
    private void updateDashboardStats() {
        if (totalVehiclesLabel != null) totalVehiclesLabel.setText(String.valueOf(vehicleData.size()));

        long uniqueOwners = vehicleData.stream().map(Vehicle::getOwnerName).distinct().count();
        if (totalOwnersLabel != null) totalOwnersLabel.setText(String.valueOf(uniqueOwners));

        if (serviceCountLabel != null) serviceCountLabel.setText("0");
        if (violationCountLabel != null) violationCountLabel.setText("0");
        if (insuranceCountLabel != null) insuranceCountLabel.setText("0");

        if (progressBar != null && vehicleData.size() > 0) {
            double progress = Math.min(1.0, vehicleData.size() / 100.0);
            progressBar.setProgress(progress);
            if (progressIndicator != null) progressIndicator.setProgress(progress);
        }
    }

    // Load Unpaid Violations
    private void loadUnpaidViolations() {
        if (unpaidViolationsList == null) return;

        try {
            if (db != null) {
                unpaidViolationsList.getItems().clear();
                String query = "SELECT violation_id, v.registration_number, vi.violation_type, vi.fine_amount " +
                        "FROM violations vi JOIN vehicles v ON vi.vehicle_id = v.vehicle_id " +
                        "WHERE vi.status = 'UNPAID'";
                db.setPstmt(query);
                ResultSet rs = db.executeQuery();

                while (rs.next()) {
                    unpaidViolationsList.getItems().add(
                            String.format("[ID:%d] %s - %s ($%.2f) - UNPAID",
                                    rs.getInt("violation_id"),
                                    rs.getString("registration_number"),
                                    rs.getString("violation_type"),
                                    rs.getDouble("fine_amount")
                            )
                    );
                }
            }

            if (unpaidViolationsList.getItems().isEmpty()) {
                unpaidViolationsList.getItems().addAll(
                        "[1] ABC123 - Speeding ($500) - UNPAID",
                        "[2] XYZ789 - Parking Violation ($100) - UNPAID"
                );
            }
        } catch (Exception e) {
            unpaidViolationsList.getItems().clear();
            unpaidViolationsList.getItems().addAll(
                    "[1] ABC123 - Speeding ($500) - UNPAID",
                    "[2] XYZ789 - Parking Violation ($100) - UNPAID"
            );
        }
    }

    // Load Expiring Insurance
    private void loadExpiringInsurance() {
        if (expiringInsuranceList == null) return;

        try {
            if (db != null) {
                expiringInsuranceList.getItems().clear();
                String query = "SELECT vehicle_reg_number, end_date FROM insurance_policy " +
                        "WHERE end_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'";
                db.setPstmt(query);
                ResultSet rs = db.executeQuery();

                while (rs.next()) {
                    expiringInsuranceList.getItems().add(
                            rs.getString("vehicle_reg_number") + " - Insurance expires on " + rs.getDate("end_date")
                    );
                }
            }

            if (expiringInsuranceList.getItems().isEmpty()) {
                expiringInsuranceList.getItems().addAll(
                        "ABC123 - Insurance expires in 15 days",
                        "DEF456 - Insurance expires in 30 days"
                );
            }
        } catch (Exception e) {
            expiringInsuranceList.getItems().clear();
            expiringInsuranceList.getItems().addAll(
                    "ABC123 - Insurance expires in 15 days",
                    "DEF456 - Insurance expires in 30 days"
            );
        }
    }

    // Update UI for User Role
    private void updateUIForUserRole() {
        if (currentUser == null) return;
        boolean isAdmin = currentUser.isAdmin();
        if (deleteButton != null) {
            deleteButton.setDisable(!isAdmin);
        }
    }

    // ============================================
    // WORKSHOP MODULE METHODS
    // ============================================

    @FXML
    private void handleViewServiceHistory() {
        String selection = historyVehicleCombo.getValue();
        if (selection == null || selection.isEmpty()) {
            showWarning("No Selection", "Please select a vehicle to view service history.");
            return;
        }

        String regNumber = selection.split(" - ")[0];

        try {
            if (db != null) {
                String query = "SELECT service_date, service_type, description, cost FROM service_records sr " +
                        "JOIN vehicles v ON sr.vehicle_id = v.vehicle_id " +
                        "WHERE v.registration_number = ? ORDER BY service_date DESC";
                db.setPstmt(query);
                db.setParameter(1, regNumber);
                ResultSet rs = db.executeQuery();

                serviceHistoryList.getItems().clear();
                double totalCost = 0;

                while (rs.next()) {
                    String record = String.format("📅 %s | 🔧 %s | 💰 $%.2f\n   📝 %s",
                            rs.getDate("service_date"),
                            rs.getString("service_type"),
                            rs.getDouble("cost"),
                            rs.getString("description") != null ? rs.getString("description") : ""
                    );
                    serviceHistoryList.getItems().add(record);
                    totalCost += rs.getDouble("cost");
                }

                if (serviceHistoryList.getItems().isEmpty()) {
                    serviceHistoryList.getItems().add("No service records found for this vehicle.");
                }

                totalServiceCost.setText(String.format("USD %.2f", totalCost));
            } else {
                serviceHistoryList.getItems().clear();
                serviceHistoryList.getItems().add("📅 2024-03-15 | 🔧 Oil Change | 💰 $89.99\n   📝 Regular maintenance");
                serviceHistoryList.getItems().add("📅 2024-01-10 | 🔧 Tire Rotation | 💰 $45.00\n   📝 Rotated all tires");
                serviceHistoryList.getItems().add("📅 2023-11-05 | 🔧 Brake Service | 💰 $199.99\n   📝 Replaced brake pads");
                totalServiceCost.setText("USD 334.98");
            }
        } catch (Exception e) {
            showError("Error", "Failed to load service history: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddServiceRecord() {
        // Get selected vehicle from table
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            showWarning("No Selection", "Please click on a vehicle row in the table to select it first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Service Record");
        dialog.setHeaderText("Add Service for: " + selectedVehicle.getRegistrationNumber());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(LocalDate.now());

        // Create service type combo box with options
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
                "Oil Change",
                "Tire Rotation",
                "Brake Service",
                "Engine Tune-up",
                "Transmission Service",
                "Battery Replacement",
                "Air Filter Change",
                "Wheel Alignment",
                "Annual Inspection",
                "Major Service",
                "Diagnostic Test",
                "Exhaust Repair",
                "Coolant Flush",
                "Fuel System Cleaning",
                "AC Service",
                "Spark Plug Replacement"
        );
        typeCombo.setPromptText("Select service type");
        typeCombo.setValue("Oil Change");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);
        TextField costField = new TextField();
        costField.setPromptText("Cost");

        content.getChildren().addAll(
                new Label("Service Date:"), datePicker,
                new Label("Service Type:"), typeCombo,
                new Label("Description:"), descArea,
                new Label("Cost:"), costField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Validate inputs
                if (typeCombo.getValue() == null) {
                    showError("Error", "Please select a service type.");
                    return;
                }
                if (costField.getText().isEmpty()) {
                    showError("Error", "Please enter the service cost.");
                    return;
                }

                try {
                    double cost = Double.parseDouble(costField.getText());

                    if (db != null) {
                        String getIdQuery = "SELECT vehicle_id FROM vehicles WHERE registration_number = ?";
                        db.setPstmt(getIdQuery);
                        db.setParameter(1, selectedVehicle.getRegistrationNumber());
                        ResultSet rs = db.executeQuery();

                        if (rs.next()) {
                            int vehicleId = rs.getInt("vehicle_id");
                            String insertQuery = "INSERT INTO service_records (vehicle_id, service_date, service_type, description, cost) VALUES (?, ?, ?, ?, ?)";
                            db.setPstmt(insertQuery);
                            db.setParameter(1, vehicleId);
                            db.setParameter(2, Date.valueOf(datePicker.getValue()));
                            db.setParameter(3, typeCombo.getValue());
                            db.setParameter(4, descArea.getText());
                            db.setParameter(5, cost);
                            db.executePstmt();
                            showInfo("Success", "Service record added successfully!");
                            updateDashboardStats();

                            // Clear form
                            typeCombo.setValue("Oil Change");
                            descArea.clear();
                            costField.clear();
                        }
                    } else {
                        showInfo("Demo Mode", "Service record would be added here (Database not connected)");
                    }
                } catch (NumberFormatException e) {
                    showError("Error", "Please enter a valid cost amount (numbers only).");
                } catch (Exception e) {
                    showError("Error", "Failed to add service record: " + e.getMessage());
                }
            }
        });
    }

    // ============================================
    // INSURANCE MODULE METHODS
    // ============================================

    @FXML
    private void handleAddInsurance() {
        String selection = insuranceVehicleCombo.getValue();
        if (selection == null || selection.isEmpty()) {
            showWarning("No Vehicle", "Please select a vehicle.");
            return;
        }

        if (policyNumberField.getText().isEmpty() || insurerNameField.getText().isEmpty() ||
                premiumField.getText().isEmpty()) {
            showWarning("Incomplete Form", "Please fill all insurance details.");
            return;
        }

        try {
            if (db != null) {
                String regNumber = selection.split(" - ")[0];
                String query = "INSERT INTO insurance_policy (vehicle_reg_number, policy_number, insurer_name, start_date, end_date, premium, coverage) VALUES (?, ?, ?, ?, ?, ?, ?)";
                db.setPstmt(query);
                db.setParameter(1, regNumber);
                db.setParameter(2, policyNumberField.getText());
                db.setParameter(3, insurerNameField.getText());
                db.setParameter(4, Date.valueOf(startDatePicker.getValue()));
                db.setParameter(5, Date.valueOf(endDatePicker.getValue()));
                db.setParameter(6, Double.parseDouble(premiumField.getText()));
                db.setParameter(7, coverageArea.getText());
                db.executePstmt();
                showInfo("Success", "Insurance policy added!");
                clearInsuranceForm();
                loadExpiringInsurance();
                updateDashboardStats();
            } else {
                showInfo("Demo Mode", "Insurance policy would be added here");
                clearInsuranceForm();
            }
        } catch (Exception e) {
            showError("Error", "Failed to add insurance: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewInsurance() {
        String selection = viewInsuranceVehicleCombo.getValue();
        if (selection == null || selection.isEmpty()) {
            showWarning("No Vehicle", "Please select a vehicle.");
            return;
        }

        try {
            if (db != null) {
                String regNumber = selection.split(" - ")[0];
                String query = "SELECT * FROM insurance_policy WHERE vehicle_reg_number = ? AND end_date >= CURRENT_DATE ORDER BY start_date DESC LIMIT 1";
                db.setPstmt(query);
                db.setParameter(1, regNumber);
                ResultSet rs = db.executeQuery();

                if (rs.next()) {
                    String details = String.format(
                            "Policy Number: %s\nInsurer: %s\nStart Date: %s\nEnd Date: %s\nPremium: USD %.2f\nCoverage: %s\nStatus: %s",
                            rs.getString("policy_number"),
                            rs.getString("insurer_name"),
                            rs.getDate("start_date"),
                            rs.getDate("end_date"),
                            rs.getDouble("premium"),
                            rs.getString("coverage") != null ? rs.getString("coverage") : "Standard Coverage",
                            "ACTIVE"
                    );
                    insuranceDetailsArea.setText(details);
                } else {
                    insuranceDetailsArea.setText("No active insurance policy found for this vehicle.");
                }
            } else {
                insuranceDetailsArea.setText("Demo Mode - Insurance details would be shown here");
            }
        } catch (Exception e) {
            showError("Error", "Failed to load insurance: " + e.getMessage());
        }
    }

    private void clearInsuranceForm() {
        if (policyNumberField != null) policyNumberField.clear();
        if (insurerNameField != null) insurerNameField.clear();
        if (premiumField != null) premiumField.clear();
        if (coverageArea != null) coverageArea.clear();
        if (startDatePicker != null) startDatePicker.setValue(LocalDate.now());
        if (endDatePicker != null) endDatePicker.setValue(LocalDate.now().plusYears(1));
    }

    // ============================================
    // POLICE MODULE METHODS
    // ============================================

    @FXML
    private void handleReportViolation() {
        String selection = violationVehicleCombo.getValue();
        if (selection == null || selection.isEmpty()) {
            showWarning("No Vehicle", "Please select a vehicle.");
            return;
        }

        if (violationTypeCombo.getValue() == null) {
            showWarning("No Violation Type", "Please select a violation type.");
            return;
        }

        if (fineAmountField.getText().isEmpty()) {
            showWarning("No Fine Amount", "Please enter the fine amount.");
            return;
        }

        String regNumber = selection.split(" - ")[0];

        try {
            double fineAmount = Double.parseDouble(fineAmountField.getText());

            if (db != null) {
                String getIdQuery = "SELECT vehicle_id FROM vehicles WHERE registration_number = ?";
                db.setPstmt(getIdQuery);
                db.setParameter(1, regNumber);
                ResultSet rs = db.executeQuery();

                if (rs.next()) {
                    int vehicleId = rs.getInt("vehicle_id");
                    String insertQuery = "INSERT INTO violations (vehicle_id, violation_date, violation_type, fine_amount, officer_name, status) VALUES (?, ?, ?, ?, ?, 'UNPAID')";
                    db.setPstmt(insertQuery);
                    db.setParameter(1, vehicleId);
                    db.setParameter(2, Date.valueOf(violationDatePicker.getValue()));
                    db.setParameter(3, violationTypeCombo.getValue());
                    db.setParameter(4, fineAmount);
                    db.setParameter(5, officerNameField.getText());
                    db.executePstmt();
                    showInfo("Success", "Violation reported!");

                    violationTypeCombo.setValue(null);
                    fineAmountField.clear();
                    officerNameField.clear();
                    violationDatePicker.setValue(LocalDate.now());

                    loadUnpaidViolations();
                    updateDashboardStats();
                }
            } else {
                showInfo("Demo Mode", "Violation would be reported here");
                loadUnpaidViolations();
            }
        } catch (NumberFormatException e) {
            showError("Error", "Please enter a valid fine amount.");
        } catch (Exception e) {
            showError("Error", "Failed to report: " + e.getMessage());
        }
    }

    @FXML
    private void handleMarkViolationPaid() {
        String selected = unpaidViolationsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            showWarning("No Selection", "Please select a violation to mark as paid.");
            return;
        }

        String idStr = selected.replaceAll(".*\\[ID:(\\d+)\\].*", "$1");
        try {
            int violationId = Integer.parseInt(idStr);
            if (db != null) {
                String query = "UPDATE violations SET status = 'PAID' WHERE violation_id = ?";
                db.setPstmt(query);
                db.setParameter(1, violationId);
                db.executePstmt();
                showInfo("Success", "Violation marked as paid!");
                loadUnpaidViolations();
                updateDashboardStats();
            } else {
                showInfo("Demo Mode", "Violation would be marked as paid here");
                loadUnpaidViolations();
            }
        } catch (Exception e) {
            showError("Error", "Failed to update violation: " + e.getMessage());
        }
    }

    // ============================================
    // CUSTOMER SUPPORT METHODS
    // ============================================

    @FXML
    private void handleSubmitQuery() {
        String selection = queryVehicleCombo.getValue();
        if (selection == null || selection.isEmpty()) {
            showWarning("No Vehicle", "Please select a vehicle.");
            return;
        }

        String customerName = customerNameField.getText().trim();
        String queryText = queryTextArea.getText().trim();

        if (customerName.isEmpty() || queryText.isEmpty()) {
            showWarning("Incomplete", "Please enter your name and query.");
            return;
        }

        try {
            if (db != null) {
                String regNumber = selection.split(" - ")[0];
                String getVehicleId = "SELECT vehicle_id FROM vehicles WHERE registration_number = ?";
                db.setPstmt(getVehicleId);
                db.setParameter(1, regNumber);
                ResultSet rs = db.executeQuery();

                if (rs.next()) {
                    int vehicleId = rs.getInt("vehicle_id");

                    String getCustomer = "SELECT customer_id FROM customer WHERE name = ?";
                    db.setPstmt(getCustomer);
                    db.setParameter(1, customerName);
                    rs = db.executeQuery();

                    int customerId;
                    if (rs.next()) {
                        customerId = rs.getInt("customer_id");
                    } else {
                        String insertCustomer = "INSERT INTO customer (name, email) VALUES (?, ?) RETURNING customer_id";
                        db.setPstmtWithKeys(insertCustomer);
                        db.setParameter(1, customerName);
                        db.setParameter(2, customerEmailField.getText());
                        db.executePstmt();
                        ResultSet keys = db.getGeneratedKeys();
                        if (keys.next()) {
                            customerId = keys.getInt(1);
                        } else {
                            customerId = -1;
                        }
                    }

                    if (customerId != -1) {
                        String insertQuery = "INSERT INTO customer_query (customer_id, vehicle_id, query_text) VALUES (?, ?, ?)";
                        db.setPstmt(insertQuery);
                        db.setParameter(1, customerId);
                        db.setParameter(2, vehicleId);
                        db.setParameter(3, queryText);
                        db.executePstmt();
                        showInfo("Success", "Your query has been submitted!");
                        loadCustomerQueries(regNumber);
                    }
                }
            } else {
                showInfo("Demo Mode", "Query would be submitted here");
                customerQueriesList.getItems().add(0, "From: " + customerName + " - " + LocalDate.now() + "\nQ: " + queryText);
            }
            customerNameField.clear();
            customerEmailField.clear();
            queryTextArea.clear();
        } catch (Exception e) {
            showError("Error", "Failed to submit query: " + e.getMessage());
        }
    }

    private void loadCustomerQueries(String regNumber) {
        if (customerQueriesList == null) return;
        try {
            if (db != null) {
                customerQueriesList.getItems().clear();
                String sql = "SELECT cq.*, c.name FROM customer_query cq " +
                        "JOIN customer c ON cq.customer_id = c.customer_id " +
                        "JOIN vehicles v ON cq.vehicle_id = v.vehicle_id " +
                        "WHERE v.registration_number = ? ORDER BY cq.query_date DESC";
                db.setPstmt(sql);
                db.setParameter(1, regNumber);
                ResultSet rs = db.executeQuery();

                while (rs.next()) {
                    customerQueriesList.getItems().add(
                            String.format("From: %s | %s\nQ: %s\nA: %s",
                                    rs.getString("name"),
                                    rs.getDate("query_date"),
                                    rs.getString("query_text"),
                                    rs.getString("response_text") != null ? rs.getString("response_text") : "Pending")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading queries: " + e.getMessage());
        }
    }

    // ============================================
    // REPORTS MODULE METHODS
    // ============================================

    @FXML
    private void handleGenerateFullReport() {
        StringBuilder report = new StringBuilder();
        report.append("VEHICLE IDENTIFICATION SYSTEM REPORT\n");
        report.append("=".repeat(60)).append("\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        if (reportTypeCombo != null && reportTypeCombo.getValue() != null) {
            report.append("Report Type: ").append(reportTypeCombo.getValue()).append("\n\n");
        }

        report.append("Total Vehicles: ").append(vehicleData.size()).append("\n\n");
        report.append("VEHICLE LIST:\n");
        report.append("-".repeat(60)).append("\n");
        report.append(String.format("%-15s | %-12s | %-12s | %4s | %-20s\n",
                "REGISTRATION", "MAKE", "MODEL", "YEAR", "OWNER"));
        report.append("-".repeat(80)).append("\n");

        for (Vehicle v : vehicleData) {
            report.append(String.format("%-15s | %-12s | %-12s | %4d | %-20s\n",
                    v.getRegistrationNumber(), v.getMake(), v.getModel(), v.getYear(), v.getOwnerName()));
        }

        if (reportPreviewArea != null) {
            reportPreviewArea.setText(report.toString());
        }
        showInfo("Report Generated", "Full report generated successfully!");
    }

    @FXML
    private void handleExportReport() {
        if (reportPreviewArea == null || reportPreviewArea.getText().isEmpty()) {
            showWarning("No Report", "Generate a report first.");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Report");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.print(reportPreviewArea.getText());
                    showInfo("Success", "Report exported to: " + file.getName());
                }
            }
        } catch (Exception e) {
            showError("Error", "Failed to export: " + e.getMessage());
        }
    }

    @FXML
    private void handleInsuranceReport() {
        if (reportTypeCombo != null) {
            reportTypeCombo.setValue("Insurance Report");
            handleGenerateFullReport();
        }
    }

    @FXML
    private void handleViolationReport() {
        if (reportTypeCombo != null) {
            reportTypeCombo.setValue("Violation Report");
            handleGenerateFullReport();
        }
    }

    @FXML
    private void handleGenerateAllVehiclesReport() {
        if (reportTypeCombo != null) {
            reportTypeCombo.setValue("All Vehicles");
            handleGenerateFullReport();
        }
    }

    // ============================================
    // MENU BAR METHODS
    // ============================================

    @FXML
    private void handleExportData() {
        if (vehicleData.isEmpty()) {
            showWarning("No Data", "No vehicles to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Vehicle Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("vehicles_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Registration Number,Make,Model,Year,Owner Name,Owner Email,Owner Phone");
                for (Vehicle v : vehicleData) {
                    writer.printf("%s,%s,%s,%d,%s,%s,%s\n",
                            v.getRegistrationNumber(),
                            v.getMake(),
                            v.getModel(),
                            v.getYear(),
                            v.getOwnerName(),
                            v.getOwnerEmail() != null ? v.getOwnerEmail() : "",
                            v.getOwnerPhone() != null ? v.getOwnerPhone() : ""
                    );
                }
                showInfo("Export Successful",
                        String.format("Exported %d vehicles to %s", vehicleData.size(), file.getName()));
            } catch (IOException e) {
                showError("Export Failed", e.getMessage());
            }
        }
    }

    // ============================================
    // ADDITIONAL FEATURES
    // ============================================

    @FXML
    private void handleBackupData() {
        try {
            if (db != null) {
                showInfo("Backup", "Database backup feature");
            } else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Backup Data");
                fileChooser.setInitialFileName("backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                        for (Vehicle v : vehicleData) {
                            writer.println(v.getRegistrationNumber() + "," + v.getMake() + "," +
                                    v.getModel() + "," + v.getYear() + "," + v.getOwnerName());
                        }
                        showInfo("Success", "Backup saved to: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            showError("Error", "Backup failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAuditLog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Audit Log");
        alert.setHeaderText("System Activity Log");
        alert.setContentText("Recent Activities:\n- User logged in: " +
                (currentUser != null ? currentUser.getUsername() : "Unknown") +
                "\n- System started: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                "\n- Database connected: " + (db != null && db.isConnected() ? "Yes" : "No"));
        alert.showAndWait();
    }

    @FXML
    private void handleDocumentation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Documentation");
        alert.setHeaderText("Vehicle Identification System - Documentation");
        alert.setContentText("""
            SYSTEM DOCUMENTATION
            
            Architecture: MVC Pattern
            Frontend: JavaFX 17
            Backend: PostgreSQL
            JDBC Driver: PostgreSQL 42.7.3
            
            Features:
            - Vehicle Registration and Management
            - Service Record Tracking
            - Insurance Policy Management
            - Violation Reporting
            - Customer Support Queries
            - Report Generation (CSV Export)
            - Dashboard with Statistics
            - User Role Management
            
            For complete documentation, visit the GitHub repository.
            """);
        alert.showAndWait();
    }

    @FXML
    private void handleShowStatistics() {
        try {
            int totalVehicles = vehicleData.size();
            long uniqueOwners = vehicleData.stream().map(Vehicle::getOwnerName).distinct().count();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("System Statistics");
            alert.setHeaderText("Vehicle Identification System Statistics");
            alert.setContentText(String.format(
                    "SYSTEM STATISTICS\n\n" +
                            "Total Vehicles: %d\n" +
                            "Total Owners: %d\n" +
                            "Average Vehicles per Owner: %.1f\n" +
                            "Database Status: %s\n" +
                            "Application Status: Running\n" +
                            "Current User: %s\n" +
                            "User Role: %s",
                    totalVehicles,
                    uniqueOwners,
                    uniqueOwners > 0 ? (double) totalVehicles / uniqueOwners : 0,
                    (db != null && db.isConnected()) ? "Connected ✓" : "Not Connected ✗",
                    currentUser != null ? currentUser.getUsername() : "Guest",
                    currentUser != null ? currentUser.getRole() : "None"
            ));
            alert.showAndWait();
        } catch (Exception e) {
            showError("Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            showInfo("Import", "Import feature - Would load data from: " + file.getName());
        }
    }

    // ============================================
    // NAVIGATION HANDLERS
    // ============================================

    @FXML private void toggleSidebar() {
        if (isSidebarVisible) {
            hideSidebar();
            hideSidebarTimer.stop();
        } else {
            showSidebar();
            hideSidebarTimer.playFromStart();
        }
    }

    @FXML private void handleDashboard() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(0); }
    @FXML private void handleAddVehicle() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(1); }
    @FXML private void handleWorkshop() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(2); }
    @FXML private void handleInsurance() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(3); }
    @FXML private void handlePoliceModule() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(4); }
    @FXML private void handleCustomerSupport() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(5); }
    @FXML private void handleReports() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(6); }
    @FXML private void handleDemoFeatures() { if (mainTabPane != null) mainTabPane.getSelectionModel().select(7); }
    @FXML private void handleRefresh() { loadDataFromDatabase(); updateDashboardStats(); }
    @FXML private void handleExit() { Platform.exit(); }
    @FXML private void handleLogout() { Platform.exit(); }

    // ============================================
    // VEHICLE MANAGEMENT
    // ============================================

    @FXML
    private void handleRegisterNewVehicle() {
        if (regNumberField.getText().isEmpty() || makeComboBox.getValue() == null ||
                modelField.getText().isEmpty() || ownerField.getText().isEmpty() || yearField.getText().isEmpty()) {
            showWarning("Incomplete Form", "Please fill all required fields.");
            return;
        }

        try {
            if (db != null) {
                String query = "INSERT INTO vehicles (registration_number, make, model, year, owner_name, owner_email, owner_phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
                db.setPstmt(query);
                db.setParameter(1, regNumberField.getText().toUpperCase());
                db.setParameter(2, makeComboBox.getValue());
                db.setParameter(3, modelField.getText());
                db.setParameter(4, Integer.parseInt(yearField.getText()));
                db.setParameter(5, ownerField.getText());
                db.setParameter(6, emailField.getText());
                db.setParameter(7, phoneField.getText());
                db.executePstmt();
                showInfo("Success", "Vehicle registered successfully!");
            } else {
                Vehicle newVehicle = new Vehicle(
                        regNumberField.getText().toUpperCase(),
                        makeComboBox.getValue(),
                        modelField.getText(),
                        Integer.parseInt(yearField.getText()),
                        ownerField.getText(),
                        phoneField.getText(),
                        emailField.getText()
                );
                vehicleData.add(newVehicle);
                showInfo("Success", "Vehicle added to demo list!");
            }
            loadDataFromDatabase();
            handleClearForm();
            updateDashboardStats();
            setupAdditionalCombos();
        } catch (Exception e) {
            showError("Error", "Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        if (regNumberField != null) regNumberField.clear();
        if (makeComboBox != null) makeComboBox.setValue(null);
        if (modelField != null) modelField.clear();
        if (yearField != null) yearField.clear();
        if (ownerField != null) ownerField.clear();
        if (emailField != null) emailField.clear();
        if (phoneField != null) phoneField.clear();
        if (statusLabel != null) statusLabel.setText("Form cleared");
    }

    @FXML
    private void handleDeleteVehicle() {
        Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a vehicle to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Vehicle");
        confirm.setContentText("Are you sure you want to delete " + selected.getRegistrationNumber() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (db != null) {
                    try {
                        String query = "DELETE FROM vehicles WHERE registration_number = ?";
                        db.setPstmt(query);
                        db.setParameter(1, selected.getRegistrationNumber());
                        db.executePstmt();
                        showInfo("Success", "Vehicle deleted from database!");
                    } catch (Exception e) {
                        showError("Error", "Failed to delete: " + e.getMessage());
                    }
                }
                vehicleData.remove(selected);
                updateDashboardStats();
                setupAdditionalCombos();
                showInfo("Success", "Vehicle deleted!");
            }
        });
    }

    @FXML
    private void handleViewHistory() {
        Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a vehicle.");
            return;
        }
        showInfo("Vehicle History", "History for: " + selected.getRegistrationNumber());
    }

    @FXML
    private void handleUserGuide() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Guide");
        alert.setHeaderText("Vehicle Identification System - Quick Guide");
        alert.setContentText("""
            QUICK START GUIDE
            
            1. Click on a vehicle row in the table to select it
            2. Then click ADD SERVICE RECORD to add service
            3. Select service type from dropdown (16 options available)
            4. Enter cost and description
            5. Click OK to save
            
            Login: admin / admin123
            """);
        alert.showAndWait();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Vehicle Identification System");
        alert.setContentText("Version 2.0\nDeveloped for OOP II\n© 2026 Pharaoh Motors");
        alert.showAndWait();
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private void clearForm() {
        if (regNumberField != null) regNumberField.clear();
        if (makeComboBox != null) makeComboBox.setValue(null);
        if (modelField != null) modelField.clear();
        if (yearField != null) yearField.clear();
        if (ownerField != null) ownerField.clear();
        if (emailField != null) emailField.clear();
        if (phoneField != null) phoneField.clear();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}