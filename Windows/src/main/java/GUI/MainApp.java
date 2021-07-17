package GUI;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;



public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //FXML Version of root
        Parent root = FXMLLoader.load(getClass().getResource("scene.fxml"));

        //Non-FXML Version of root
        //Group root = new Group();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        //Icon Code
        Image icon = new Image("images/icon.png");
        stage.getIcons().add(icon);
        stage.setTitle("Drive Sync");

//        //Text
//        Text l1 = new Text("Testing");
//        l1.setX(50);
//        l1.setY(50);
//        l1.setFill(Color.WHITE);
//        root.getChildren().add(l1);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
