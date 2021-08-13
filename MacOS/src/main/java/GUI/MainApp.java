package GUI;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static DriveHandling.DriveService.*;


public class MainApp extends Application {


    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        //Icon Code
        Image icon = new Image("images/icon.png");
        stage.getIcons().add(icon);
        stage.setTitle("Drive Sync");

        stage.setScene(scene);
        stage.show();

        //Verification of Folder Integrity
        verifyFolder();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
