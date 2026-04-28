package kamohelo.pharaoh_motors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label adminLabel;
    @FXML private Label managerLabel;
    @FXML private Label userLabel;

    private DatabaseConnection db;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection with UPDATED Neon credentials
        db = new DatabaseConnection("neondb_owner", "npg_OuES6k3RJYoH");
        db.openConn();

        // Set up show password functionality
        if (showPasswordCheckBox != null) {
            showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Password");
                    alert.setHeaderText("Your Password");
                    alert.setContentText(passwordField.getText());
                    alert.showAndWait();
                }
            });
        }

        // Set up enter key to login
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // Setup demo account auto-fill
        if (adminLabel != null) {
            adminLabel.setOnMouseClicked(event -> {
                usernameField.setText("admin");
                passwordField.setText("admin123");
                statusLabel.setText("Admin credentials loaded - Click Login");
                statusLabel.setStyle("-fx-text-fill: #00ff00;");
            });
        }

        if (managerLabel != null) {
            managerLabel.setOnMouseClicked(event -> {
                usernameField.setText("manager1");
                passwordField.setText("manager123");
                statusLabel.setText("Manager credentials loaded - Click Login");
                statusLabel.setStyle("-fx-text-fill: #00ff00;");
            });
        }

        if (userLabel != null) {
            userLabel.setOnMouseClicked(event -> {
                usernameField.setText("user1");
                passwordField.setText("user123");
                statusLabel.setText("User credentials loaded - Click Login");
                statusLabel.setStyle("-fx-text-fill: #00ff00;");
            });
        }

        usernameField.requestFocus();
        statusLabel.setText("Enter username and password to continue");
        statusLabel.setStyle("-fx-text-fill: #ffd700;");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            return;
        }

        progressIndicator.setVisible(true);
        loginButton.setDisable(true);
        statusLabel.setText("Authenticating...");
        statusLabel.setStyle("-fx-text-fill: #ffd700;");

        new Thread(() -> {
            try {
                User user = authenticateUser(username, password);

                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    loginButton.setDisable(false);

                    if (user != null && user.isActive()) {
                        currentUser = user;
                        statusLabel.setText("Login successful! Welcome " + user.getFullName());
                        statusLabel.setStyle("-fx-text-fill: #00ff00;");
                        recordLoginHistory(user.getUserId(), "SUCCESS");
                        openDashboard();
                    } else if (user != null && !user.isActive()) {
                        statusLabel.setText("Account is disabled. Please contact administrator.");
                        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                        recordLoginHistory(-1, "FAILED-Account disabled");
                    } else {
                        statusLabel.setText("Invalid username or password");
                        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                        recordLoginHistory(-1, "FAILED-Invalid credentials");
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    loginButton.setDisable(false);
                    statusLabel.setText("Database error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private User authenticateUser(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE username = ? AND is_active = true";
            db.setPstmt(query);
            db.setParameter(1, username);
            ResultSet rs = db.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                if (storedPassword.equals(password)) {
                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getBoolean("is_active"),
                            rs.getTimestamp("last_login") != null ?
                                    rs.getTimestamp("last_login").toLocalDateTime() : null,
                            rs.getTimestamp("created_at") != null ?
                                    rs.getTimestamp("created_at").toLocalDateTime() : null
                    );
                    updateLastLogin(user.getUserId());
                    return user;
                }
            }
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void updateLastLogin(int userId) {
        try {
            String query = "UPDATE users SET last_login = ? WHERE user_id = ?";
            db.setPstmt(query);
            db.setParameter(1, Timestamp.valueOf(LocalDateTime.now()));
            db.setParameter(2, userId);
            db.executePstmt();
        } catch (Exception e) {
            System.out.println("Failed to update last login: " + e.getMessage());
        }
    }

    private void recordLoginHistory(int userId, String status) {
        try {
            String shortStatus = status.length() > 45 ? status.substring(0, 45) : status;
            String query = "INSERT INTO login_history (user_id, login_time, status) VALUES (?, ?, ?)";
            db.setPstmt(query);
            db.setParameter(1, userId == -1 ? null : userId);
            db.setParameter(2, Timestamp.valueOf(LocalDateTime.now()));
            db.setParameter(3, shortStatus);
            db.executePstmt();
        } catch (Exception e) {
            System.out.println("Failed to record login history: " + e.getMessage());
        }
    }

    private void openDashboard() {
        try {
            System.out.println("Attempting to load dashboard...");

            URL fxmlUrl = getClass().getResource("pharaoh_motors.fxml");
            if (fxmlUrl == null) {
                System.err.println("pharaoh_motors.fxml not found!");
                throw new RuntimeException("Cannot find pharaoh_motors.fxml");
            }

            System.out.println("Found FXML at: " + fxmlUrl.getPath());

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            Pharaohmotors mainController = loader.getController();
            mainController.setCurrentUser(currentUser);
            mainController.setDatabaseConnection(db);

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Vehicle Identification System - " + currentUser.getUsername());
            dashboardStage.setScene(new Scene(root, 1400, 900));
            dashboardStage.setMaximized(true);
            dashboardStage.setMinWidth(1200);
            dashboardStage.setMinHeight(800);
            dashboardStage.show();

            System.out.println("Dashboard opened successfully");

            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            statusLabel.setText("Error opening dashboard: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load Dashboard");
            alert.setContentText("Error: " + e.getMessage() +
                    "\n\nMake sure pharaoh_motors.fxml is in the same directory as LoginController.class\n\n" +
                    "Expected location: " + getClass().getResource(""));
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        if (db != null) {
            db.closeConnection();
        }
        System.exit(0);
    }
}