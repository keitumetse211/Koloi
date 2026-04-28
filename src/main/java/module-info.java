module kamohelo.pharaoh_motors {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens kamohelo.pharaoh_motors to javafx.fxml;
    exports kamohelo.pharaoh_motors;
}