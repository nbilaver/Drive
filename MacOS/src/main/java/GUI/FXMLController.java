package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static DriveHandling.DriveService.*;

public class FXMLController {
    public FXMLController() throws GeneralSecurityException, IOException {
    }

    @FXML
    private Label label;
    @FXML
    private Button syncFiles;
    @FXML
    private Button dlFiles;



    public void initialize() {

        syncFiles.setOnAction(value ->  {
            updateFolder("Upload Files",getFolderId());
            syncFiles.setText("FINISHED");
        });

        dlFiles.setOnAction(value ->  {
            downloadFolder(getFolderId(),"Google Drive Sync");
            dlFiles.setText("FINISHED");
        });

    }
}
