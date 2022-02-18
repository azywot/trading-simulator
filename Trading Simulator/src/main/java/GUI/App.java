package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import javafx.scene.image.Image;

/**
 * JavaFX Trading Simulator Application
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = loadFXML("primary");
        Scene scene = new Scene(root);
        Image icon = new Image(String.valueOf(getClass().getResource("icon.png")));

        stage.getIcons().add(icon);
        stage.setTitle("Trading Simulator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(e -> System.exit(0));
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}