package GUI;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import static DriveHandling.DriveService.getFolderId;
import static DriveHandling.DriveService.setFolderId;

public class FXMLController {
    
    @FXML
    private Label label;
    @FXML
    private Button syncFiles;
    @FXML
    private Button dlFiles;
    
    public void initialize() {

        syncFiles.setOnAction(value ->  {
            syncFiles.setText("Clicked!");
            System.out.println(getFolderId());
            //setFolderId("TEST");
        });

        dlFiles.setOnAction(value ->  {
            dlFiles.setText("Clicked!");
        });

    }    
}
