package GUI;

import com.google.api.services.drive.Drive;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static DriveHandling.DriveService.*;

//TODO Due to the buttons here, it is necessary to connect to google drive twice. That is terrible, but I can't think of another solution.


public class FXMLController {
    
    @FXML
    private Label label;
    @FXML
    private Button syncFiles;
    @FXML
    private Button dlFiles;


    public FXMLController() throws GeneralSecurityException, IOException {
    }

    public void initialize() {

        syncFiles.setOnAction(value ->  {
            updateFolder("Upload Files", getFolderId());
            syncFiles.setText("FINISHED");
        });

        dlFiles.setOnAction(value ->  {
            downloadFolder(getFolderId(),"Google Drive Sync");
            //testing();
            dlFiles.setText("Clicked!");
        });

    }
}
