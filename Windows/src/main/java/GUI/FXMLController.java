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

    private Drive service = driveConnect();

    public FXMLController() throws GeneralSecurityException, IOException {
    }

    public void initialize() {

        syncFiles.setOnAction(value ->  {
            updateFolder(service,"Upload Files", getFolderId());
            System.out.println(getFolderId());
        });

        dlFiles.setOnAction(value ->  {
            downloadFolder(service,getFolderId(),"Upload Files");
            //testing(service);
            dlFiles.setText("Clicked!");
        });

    }
}
