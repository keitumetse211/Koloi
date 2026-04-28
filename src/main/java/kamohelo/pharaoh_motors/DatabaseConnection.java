package kamohelo.pharaoh_motors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Properties;

public class DatabaseConnection {
    private Connection conn;
    private final String url = "jdbc:postgresql://ep-silent-lake-amsvmrvl.c-5.us-east-1.aws.neon.tech/neondb?sslmode=require";
    private String user;
    private String password;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private boolean isConnected = false;

    // Constructor
    public DatabaseConnection(String user, String password) {
        this.user = user;
        this.password = password;
        connect();
        if (isConnected) {
            createTablesIfNotExist();
            createStoredProceduresAndViews();
            insertDefaultData();
        }
    }

    // Connect to database with better properties
    private void connect() {
        try {
            Class.forName("org.postgresql.Driver");

            // Set connection properties to avoid SCRAM issues
            Properties props = new Properties();
            props.setProperty("user", this.user);
            props.setProperty("password", this.password);
            props.setProperty("ssl", "true");
            props.setProperty("sslmode", "require");
            props.setProperty("gssEncMode", "disable");

            this.conn = DriverManager.getConnection(this.url, props);
            this.isConnected = true;
            System.out.println("✓ Database connected successfully to Neon!");
            System.out.println("  Host: ep-silent-lake-amsvmrvl.c-5.us-east-1.aws.neon.tech");
            System.out.println("  Database: neondb");
            System.out.println("  User: " + this.user);
        } catch (SQLException e) {
            this.isConnected = false;
            System.out.println("✗ SQL Error: " + e.getMessage());
            if (e.getMessage().contains("Connection refused")) {
                System.out.println("  → Make sure you're using the correct Neon connection string");
            } else if (e.getMessage().contains("database")) {
                System.out.println("  → Make sure the database name is correct");
            } else if (e.getMessage().contains("SCRAM")) {
                System.out.println("  → Authentication issue. Try resetting your Neon password.");
                System.out.println("  → Go to Neon Dashboard → Settings → Reset Password");
            }
        } catch (ClassNotFoundException e) {
            this.isConnected = false;
            System.out.println("✗ PostgreSQL JDBC Driver not found!");
            System.out.println("  → Download postgresql-42.7.3.jar from https://jdbc.postgresql.org/download.html");
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.isConnected && this.conn != null;
    }

    public void openConn() {
        if (isConnected()) return;
        connect();
    }

    public Connection getConn() {
        if (!isConnected()) {
            openConn();
        }
        return conn;
    }

    public PreparedStatement getPstmt() {
        return pstmt;
    }

    public void setPstmt(String sql) {
        try {
            if (this.pstmt != null) {
                this.pstmt.close();
            }
            if (getConn() == null) {
                System.out.println("✗ Cannot prepare statement: No database connection");
                return;
            }
            this.pstmt = this.getConn().prepareStatement(sql);
        } catch (SQLException e) {
            System.out.println("✗ Error preparing statement: " + e.getMessage());
            System.out.println("  SQL: " + sql);
        }
    }

    public void setPstmtWithKeys(String sql) {
        try {
            if (this.pstmt != null) {
                this.pstmt.close();
            }
            if (getConn() == null) {
                System.out.println("✗ Cannot prepare statement: No database connection");
                return;
            }
            this.pstmt = this.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            System.out.println("✗ Error preparing statement with keys: " + e.getMessage());
            System.out.println("  SQL: " + sql);
        }
    }

    public void setParameter(int index, Object value) {
        try {
            if (this.pstmt == null) {
                System.out.println("✗ Cannot set parameter: Prepared statement is null");
                return;
            }
            if (value == null) {
                this.pstmt.setNull(index, java.sql.Types.NULL);
            } else {
                this.pstmt.setObject(index, value);
            }
        } catch (SQLException e) {
            System.out.println("✗ Error setting parameter at index " + index + ": " + e.getMessage());
        }
    }

    public void setParameters(Object... params) {
        for (int i = 0; i < params.length; i++) {
            setParameter(i + 1, params[i]);
        }
    }

    public int executePstmt() {
        int res = 0;
        try {
            if (this.pstmt == null) {
                System.out.println("✗ Cannot execute update: Prepared statement is null");
                return 0;
            }
            res = this.pstmt.executeUpdate();
            System.out.println("✓ Update executed successfully. Rows affected: " + res);
        } catch (SQLException e) {
            System.out.println("✗ Error executing update: " + e.getMessage());
        }
        return res;
    }

    public ResultSet executeQuery() {
        try {
            if (this.pstmt == null) {
                System.out.println("✗ Cannot execute query: Prepared statement is null");
                return null;
            }
            if (this.rs != null) {
                this.rs.close();
            }
            this.rs = this.pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("✗ Error executing query: " + e.getMessage());
        }
        return rs;
    }

    public ResultSet executeQuery(String sql, Object... params) {
        setPstmt(sql);
        setParameters(params);
        return executeQuery();
    }

    public int executeUpdate(String sql, Object... params) {
        setPstmt(sql);
        setParameters(params);
        return executePstmt();
    }

    public ResultSet getGeneratedKeys() {
        try {
            if (this.pstmt == null) return null;
            return this.pstmt.getGeneratedKeys();
        } catch (SQLException e) {
            System.out.println("✗ Error getting generated keys: " + e.getMessage());
            return null;
        }
    }

    public int getGeneratedId() {
        try {
            ResultSet generatedKeys = getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("✗ Error getting generated ID: " + e.getMessage());
        }
        return -1;
    }

    public void beginTransaction() {
        try {
            if (getConn() != null) {
                this.getConn().setAutoCommit(false);
                System.out.println("✓ Transaction started");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error starting transaction: " + e.getMessage());
        }
    }

    public void commit() {
        try {
            if (getConn() != null) {
                this.getConn().commit();
                this.getConn().setAutoCommit(true);
                System.out.println("✓ Transaction committed");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error committing transaction: " + e.getMessage());
        }
    }

    public void rollback() {
        try {
            if (getConn() != null) {
                this.getConn().rollback();
                this.getConn().setAutoCommit(true);
                System.out.println("⚠ Transaction rolled back");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error rolling back transaction: " + e.getMessage());
        }
    }

    public boolean tableExists(String tableName) {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?", tableName);
            if (rs != null && rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("✗ Error checking table existence: " + e.getMessage());
        }
        return false;
    }

    public int getRecordCount(String tableName) {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM " + tableName);
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("✗ Error getting record count: " + e.getMessage());
        }
        return 0;
    }

    public int[] executeBatch() {
        try {
            if (this.pstmt == null) return new int[0];
            return this.pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("✗ Error executing batch: " + e.getMessage());
            return new int[0];
        }
    }

    public void addBatch() {
        try {
            if (this.pstmt != null) {
                this.pstmt.addBatch();
            }
        } catch (SQLException e) {
            System.out.println("✗ Error adding to batch: " + e.getMessage());
        }
    }

    public void clearBatch() {
        try {
            if (this.pstmt != null) {
                this.pstmt.clearBatch();
            }
        } catch (SQLException e) {
            System.out.println("✗ Error clearing batch: " + e.getMessage());
        }
    }

    public void createTablesIfNotExist() {
        try {
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "role VARCHAR(20) DEFAULT 'USER', " +
                    "is_active BOOLEAN DEFAULT TRUE, " +
                    "last_login TIMESTAMP, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            String createVehicles = "CREATE TABLE IF NOT EXISTS vehicles (" +
                    "vehicle_id SERIAL PRIMARY KEY, " +
                    "registration_number VARCHAR(50) UNIQUE NOT NULL, " +
                    "make VARCHAR(50) NOT NULL, " +
                    "model VARCHAR(50) NOT NULL, " +
                    "year INTEGER NOT NULL, " +
                    "owner_name VARCHAR(100) NOT NULL, " +
                    "owner_email VARCHAR(100), " +
                    "owner_phone VARCHAR(50), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            String createServices = "CREATE TABLE IF NOT EXISTS service_records (" +
                    "service_id SERIAL PRIMARY KEY, " +
                    "vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE, " +
                    "service_date DATE NOT NULL, " +
                    "service_type VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "cost DECIMAL(10,2), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            String createViolations = "CREATE TABLE IF NOT EXISTS violations (" +
                    "violation_id SERIAL PRIMARY KEY, " +
                    "vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE, " +
                    "violation_date DATE NOT NULL, " +
                    "violation_type VARCHAR(100) NOT NULL, " +
                    "fine_amount DECIMAL(10,2), " +
                    "officer_name VARCHAR(100), " +
                    "status VARCHAR(20) DEFAULT 'UNPAID', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            String createInsurance = "CREATE TABLE IF NOT EXISTS insurance_policy (" +
                    "insurance_id SERIAL PRIMARY KEY, " +
                    "vehicle_reg_number VARCHAR(50) REFERENCES vehicles(registration_number), " +
                    "policy_number VARCHAR(100) UNIQUE NOT NULL, " +
                    "insurer_name VARCHAR(100) NOT NULL, " +
                    "start_date DATE NOT NULL, " +
                    "end_date DATE NOT NULL, " +
                    "premium DECIMAL(10,2), " +
                    "coverage TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            String createCustomer = "CREATE TABLE IF NOT EXISTS customer (" +
                    "customer_id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "address TEXT, " +
                    "phone VARCHAR(50), " +
                    "email VARCHAR(100), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            String createCustomerQuery = "CREATE TABLE IF NOT EXISTS customer_query (" +
                    "query_id SERIAL PRIMARY KEY, " +
                    "customer_id INTEGER REFERENCES customer(customer_id), " +
                    "vehicle_id INTEGER REFERENCES vehicles(vehicle_id), " +
                    "query_date DATE DEFAULT CURRENT_DATE, " +
                    "query_text TEXT NOT NULL, " +
                    "response_text TEXT, " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            String createLoginHistory = "CREATE TABLE IF NOT EXISTS login_history (" +
                    "history_id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(user_id), " +
                    "login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "status VARCHAR(100))";

            executeUpdate(createUsers);
            executeUpdate(createVehicles);
            executeUpdate(createServices);
            executeUpdate(createViolations);
            executeUpdate(createInsurance);
            executeUpdate(createCustomer);
            executeUpdate(createCustomerQuery);
            executeUpdate(createLoginHistory);

            System.out.println("✓ Database tables verified/created on Neon!");
        } catch (Exception e) {
            System.out.println("✗ Error creating tables: " + e.getMessage());
        }
    }

    public void insertDefaultData() {
        try {
            String insertUsers = "INSERT INTO users (username, password, full_name, email, role, is_active) VALUES " +
                    "('admin', 'admin123', 'System Administrator', 'admin@pharaohmotors.com', 'ADMIN', true), " +
                    "('manager1', 'manager123', 'Operations Manager', 'manager@pharaohmotors.com', 'MANAGER', true), " +
                    "('user1', 'user123', 'Regular User', 'user@pharaohmotors.com', 'USER', true) " +
                    "ON CONFLICT (username) DO NOTHING";

            String insertVehicles = "INSERT INTO vehicles (registration_number, make, model, year, owner_name, owner_email, owner_phone) VALUES " +
                    "('ABC123', 'Toyota', 'Camry', 2020, 'John Doe', 'john@example.com', '+27 12 345 6789'), " +
                    "('XYZ789', 'Honda', 'Civic', 2019, 'Jane Smith', 'jane@example.com', '+27 23 456 7890'), " +
                    "('DEF456', 'Ford', 'Mustang', 2021, 'Bob Johnson', 'bob@example.com', '+27 34 567 8901') " +
                    "ON CONFLICT (registration_number) DO NOTHING";

            executeUpdate(insertUsers);
            executeUpdate(insertVehicles);
            System.out.println("✓ Default data inserted into Neon!");
        } catch (Exception e) {
            System.out.println("✗ Error inserting default data: " + e.getMessage());
        }
    }

    public void createStoredProceduresAndViews() {
        if (!isConnected()) {
            System.out.println("Cannot create stored procedures: Not connected to database");
            return;
        }

        String createVehicleSummaryView = "CREATE OR REPLACE VIEW vehicle_summary_view AS " +
                "SELECT " +
                "    v.vehicle_id, " +
                "    v.registration_number, " +
                "    v.make, " +
                "    v.model, " +
                "    v.year, " +
                "    v.owner_name, " +
                "    v.owner_email, " +
                "    v.owner_phone, " +
                "    COUNT(DISTINCT sr.service_id) AS service_count, " +
                "    COUNT(DISTINCT vi.violation_id) AS violation_count, " +
                "    COALESCE(SUM(sr.cost), 0) AS total_service_cost " +
                "FROM vehicles v " +
                "LEFT JOIN service_records sr ON v.vehicle_id = sr.vehicle_id " +
                "LEFT JOIN violations vi ON v.vehicle_id = vi.vehicle_id " +
                "GROUP BY v.vehicle_id";
        executeUpdate(createVehicleSummaryView);
        System.out.println("✓ Created view: vehicle_summary_view");

        String createActiveInsuranceView = "CREATE OR REPLACE VIEW active_insurance_view AS " +
                "SELECT " +
                "    ip.insurance_id, " +
                "    ip.vehicle_reg_number, " +
                "    ip.policy_number, " +
                "    ip.insurer_name, " +
                "    ip.start_date, " +
                "    ip.end_date, " +
                "    ip.premium, " +
                "    ip.coverage, " +
                "    v.make, " +
                "    v.model, " +
                "    v.owner_name " +
                "FROM insurance_policy ip " +
                "JOIN vehicles v ON ip.vehicle_reg_number = v.registration_number " +
                "WHERE ip.end_date >= CURRENT_DATE";
        executeUpdate(createActiveInsuranceView);
        System.out.println("✓ Created view: active_insurance_view");

        String createUnpaidViolationsView = "CREATE OR REPLACE VIEW unpaid_violations_view AS " +
                "SELECT " +
                "    vi.violation_id, " +
                "    v.registration_number, " +
                "    v.make, " +
                "    v.model, " +
                "    v.owner_name, " +
                "    vi.violation_date, " +
                "    vi.violation_type, " +
                "    vi.fine_amount, " +
                "    vi.officer_name " +
                "FROM violations vi " +
                "JOIN vehicles v ON vi.vehicle_id = v.vehicle_id " +
                "WHERE vi.status = 'UNPAID'";
        executeUpdate(createUnpaidViolationsView);
        System.out.println("✓ Created view: unpaid_violations_view");

        System.out.println("✓ Views created successfully on Neon!");
    }

    public ResultSet getVehicleSummaryView() throws SQLException {
        String query = "SELECT * FROM vehicle_summary_view ORDER BY registration_number";
        setPstmt(query);
        return executeQuery();
    }

    public ResultSet getActiveInsuranceView() throws SQLException {
        String query = "SELECT * FROM active_insurance_view ORDER BY end_date";
        setPstmt(query);
        return executeQuery();
    }

    public ResultSet getUnpaidViolationsView() throws SQLException {
        String query = "SELECT * FROM unpaid_violations_view ORDER BY violation_date DESC";
        setPstmt(query);
        return executeQuery();
    }

    public String getConnectionStatus() {
        if (isConnected()) {
            return "✓ Connected to Neon PostgreSQL database";
        } else {
            return "✗ Not connected to Neon database";
        }
    }

    public void closeResultSet() {
        try {
            if (this.rs != null) {
                this.rs.close();
                this.rs = null;
            }
        } catch (SQLException e) {
            System.out.println("Error closing result set: " + e.getMessage());
        }
    }

    public void closeStatement() {
        try {
            if (this.pstmt != null) {
                this.pstmt.close();
                this.pstmt = null;
            }
        } catch (SQLException e) {
            System.out.println("Error closing statement: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            closeResultSet();
            closeStatement();
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
                this.isConnected = false;
                System.out.println("✓ Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("✗ Error closing connection: " + e.getMessage());
        }
    }
}