package GUI;

import com.google.api.services.drive.Drive;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static DriveHandling.DriveService.*;

public class FXMLController {

    private final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "controller-thread");
        t.setDaemon(true);
        return t;
    });

    private static Drive service;
    static {
        try {
            service = driveConnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private Label label;
    @FXML
    private Button syncFiles;
    @FXML
    private Button dlFiles;
    @FXML
    private Text progressText;


    public void initialize() {
        verifyFolder(service);
    }

    @FXML
    private void upFolder(ActionEvent event) {
        event.consume();

        uploadingTask task = new uploadingTask(getFolderId(),"Google Drive Sync");
        task.setOnRunning(t -> progressText.setText("Running"));
        task.setOnSucceeded(t -> progressText.setText("Finished"));
        task.setOnFailed(t -> progressText.setText("Failure"));

        executor.execute(task);

    }

    @FXML
    private void dlFolder(ActionEvent event){
        event.consume();

        downloadingTask task = new downloadingTask(getFolderId(),"Google Drive Sync");
        task.setOnRunning(t -> progressText.setText("Running"));
        task.setOnSucceeded(t -> progressText.setText("Finished"));
        task.setOnFailed(t -> progressText.setText("Failure"));

        executor.execute(task);
    }

    private static class uploadingTask extends Task<Void> {
        private final String folderId;
        private final String path;

        private uploadingTask(String folderId,String path){
            this.folderId = folderId;
            this.path = path;
        }

        @Override
        protected Void call() throws Exception {
            uploadFolder(service,folderId,path);
            return null;
        }
    }

    private static class downloadingTask extends Task<Void>{
        private final String folderId;
        private final String path;

        private downloadingTask(String folderId,String path){
            this.folderId = folderId;
            this.path = path;
        }

        @Override
        protected Void call() throws Exception {
            downloadFolder(service,folderId,path);
            return null;
        }
    }
}
