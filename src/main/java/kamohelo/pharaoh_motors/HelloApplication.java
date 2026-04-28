package kamohelo.pharaoh_motors;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);

        stage.setTitle("Vehicle Identification System - Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}