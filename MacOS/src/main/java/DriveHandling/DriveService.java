package DriveHandling;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class DriveService {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/DriveHandling/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    //Creates folder and saves id to fileLocations.csv
    //Returns Id of folder
    public static String makeFolder(Drive service){
        File fileMetadata = new File();
        fileMetadata.setName("Google Drive Sync");
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        String folderId = null;
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            folderId = file.getId();
            setFolderId(folderId);

        } catch (IOException e) {
            System.out.println("An error occurred in makeFolder().");
            e.printStackTrace();
        }
        return folderId;

    }

    //Takes care of uploading file to google drive.
    public static void uploadFile(Drive service){
        try {
            File fileMetadata = new File();
            fileMetadata.setName("TestFile.txt");
            fileMetadata.setParents(Collections.singletonList(getFolderId()));
            java.io.File filePath = new java.io.File("Upload Files/TestFile.txt");
            FileContent mediaContent = new FileContent("text/txt", filePath);
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        } catch(GoogleJsonResponseException e){
            makeFolder(service);
            uploadFile(service);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //

    //Takes in specific file to update
    public static void updateFile(Drive service, String fileId){
        try {
            File file = service.files().get(fileId).execute();

            java.io.File filePath = new java.io.File("Upload Files/TestFile.txt");
            FileContent mediaContent = new FileContent("text/txt", filePath);

            file = service.files().update(fileId,file, mediaContent).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Reads fileLocations.csv for folder ID
    //FolderID should always contained in the second line of folderID.csv. Returns null if not present
    public static String getFolderId(){
        java.io.File store = new java.io.File("resources/DriveHandling/fileLocations.csv");
        String line = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(store));

            line = br.readLine();
            line = br.readLine();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] parts = line.split(",");
        if(parts[0].equals("Folder")){
            return parts[1];
        } else {
            return null;
        }
    }

    //Sets new folderID into fileLocations.csv, always stored in line 2
    public static void setFolderId(String id){
        java.io.File store = new java.io.File("resources/DriveHandling/fileLocations.csv");
        try {
            StringBuffer inputBuffer = new StringBuffer();
            BufferedReader br = new BufferedReader(new FileReader(store));
            String line;
            int i = 0;
            while((line = br.readLine()) != null){
                if(i == 1){
                    inputBuffer.append("Folder," + id);
                    inputBuffer.append("\n");
                } else {
                    inputBuffer.append(line);
                    inputBuffer.append("\n");
                }
                i++;
            }
            br.close();

            FileOutputStream fileOut = new FileOutputStream("fileLocations.csv");
            fileOut.write(inputBuffer.toString().getBytes(StandardCharsets.UTF_8));
            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Uploads Entire folder to google drive



    //Uploads Individual file to google drive.


    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        //When this ends, there should be a valid google drive folder and file
        //to store its id (unless saved id is not valid, will look at later)
        String folderId = getFolderId();
        try {
            if(folderId == null){
                //Query to try and find existing folder
                FileList result = service.files().list()
                        .setQ("name='Google Drive Sync' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .execute();
                if(result.getFiles().size() > 1){
                    System.out.println("Multiple Valid Folders found in Drive");
                    System.exit(1);
                } else if(result.getFiles().size() == 1){
                    folderId = result.getFiles().get(0).getId();
                    setFolderId(folderId);
                } else {
                    folderId = makeFolder(service);
                }

            }


        } catch(GoogleJsonResponseException e){
            folderId = makeFolder(service);
            e.printStackTrace();
        }  catch (IOException e) {
            System.out.println("An error occurred in main.");
            e.printStackTrace();
        }

        uploadFile(service);


    }
}
