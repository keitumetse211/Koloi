package kamohelo.pharaoh_motors;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // Constructor for new user
    public User(String username, String password, String fullName, String email, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isActive = true;
    }

    // Full constructor
    public User(int userId, String username, String password, String fullName,
                String email, String role, boolean isActive,
                LocalDateTime lastLogin, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isManager() {
        return "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ") - " + role;
    }
}