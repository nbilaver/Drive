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
import com.google.api.services.drive.model.PermissionList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;



public class DriveService {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final String FILELOCATIONS_PATH = "src/main/resources/DriveHandling/fileLocations.csv";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/DriveHandling/credentials.json";

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

    public DriveService() throws GeneralSecurityException, IOException {

    }


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
    public static String makeFolder(){
        File fileMetadata = new File();
        fileMetadata.setName("Google Drive Sync");
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        String folderId = null;
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            folderId = file.getId();
            System.out.println("MADE FOLDER, NEW ID: "+ folderId);
            setFolderId(folderId);

        } catch (IOException e) {
            System.out.println("An error occurred in makeFolder().");
            e.printStackTrace();
        }
        return folderId;

    }

    //Takes care of uploading file to google drive.
    //TODO folders haven't been tested.
    public static void updateFolder(String path, String folderId){
        try {
            java.io.File uploadF = new java.io.File(path);
            java.io.File[] directoryListing = uploadF.listFiles();
            if(directoryListing != null){
                for(java.io.File child : directoryListing){
                    if(child.getName().equals(".DS_Store")){
                        continue;
                    }

                    if(child.isFile()){
                        System.out.println("name = '" + child.getName() +"' and '" + folderId + "' in parents and trashed = false");
                        FileList result = service.files().list()
                                .setQ("name = '" + child.getName() +"' and '" + folderId + "' in parents and trashed = false")
                                .setSpaces("drive")
                                .setFields("nextPageToken, files(id, name)")
                                .execute();

                        System.out.println(result.getFiles().size());
                        if(result.getFiles().size() == 1){
                            String fileId = result.getFiles().get(0).getId();
                            File oldFile = service.files().get(fileId).execute();

                            File file = new File();
                            file.setName(oldFile.getName());
                            file.setParents(oldFile.getParents());
                            file.setDescription(oldFile.getDescription());
                            file.setMimeType(oldFile.getMimeType());

                            FileContent mediaContent = new FileContent(null, child);

                            File updatedFile = service.files().update(fileId, file, mediaContent).execute();

                        } else if (result.getFiles().size() == 0){

                            File fileMetadata = new File();
                            fileMetadata.setName(child.getName());
                            fileMetadata.setParents(Collections.singletonList(folderId));

                            FileContent mediaContent = new FileContent(null,child);

                            File file = service.files().create(fileMetadata, mediaContent)
                                    .setFields("id, parents")
                                    .execute();
                        } //Probably should find case where there are more than 1 file found, but unlikely to happen, so future todo

                    } else if(child.isDirectory()){
                        //TODO Need to set parent for new folders
                        FileList result = service.files().list()
                                .setQ("name = '" + child.getName() +"' and '" + folderId + "' in parents and " +
                                        "mimeType = 'application/vnd.google-apps.folder' " +
                                        "and trashed = false")
                                .setSpaces("drive")
                                .setFields("nextPageToken, files(id, name)")
                                .execute();

                        String childId = null;
                        if(result.getFiles().size() == 1){
                            childId = result.getFiles().get(0).getId();

                        } else if (result.getFiles().size() == 0){

                            File fileMetadata = new File();
                            fileMetadata.setName(child.getName());
                            fileMetadata.setMimeType("application/vnd.google-apps.folder");
                            fileMetadata.setParents(Collections.singletonList(folderId));


                            File file = service.files().create(fileMetadata)
                                    .setFields("id")
                                    .execute();
                            childId = file.getId();
                        }//Same thing about accounting for result.getFiles.size > 1, fix later
                        updateFolder(path + "/" + child.getName(),childId);
                    }
                }
            }
        }
//        catch(GoogleJsonResponseException e){
//            //TODO this currently leads to an infinite repeat and other issues
//            if(e.getStatusCode() == 404){
//                System.out.println("Update Folder 404 exception triggered");
//            } else {
//                e.printStackTrace();
//            }
//        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Downloads files from the google drive folder
    //TODO figure out recursive download of the folder
    public static Boolean downloadFolder(String folderId, String path){
        System.out.println("DOWNLOADING FILE");

        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            File currentFolder = service.files().get(folderId).execute();
            String folderName = currentFolder.getName();

            //TODO Here is read only file system error also wrong name
            Files.createDirectories(Paths.get(path));


            String pageToken = null;
            do{
                FileList result = service.files().list()
                        .setQ("'" + folderId + "' in parents and trashed = false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .execute();
                for(File file : result.getFiles()){
                    System.out.println(file.getMimeType());
                    System.out.println(file.getName());
                    if(file.getMimeType() != null && file.getMimeType().equals("application/vnd.google-apps.folder")){
                        downloadFolder(file.getId(),path + "/" + file.getName());
                    } else {
                        service.files().get(file.getId())
                                .executeMediaAndDownloadTo(outputStream);
                        FileOutputStream fileOut = new FileOutputStream(path + "/" + file.getName());
                        fileOut.write(outputStream.toString().getBytes(StandardCharsets.UTF_8));
                        fileOut.close();
                    }
                    pageToken = result.getNextPageToken();
                }
            } while(pageToken != null);

            System.out.println("FINISHED DOWNLOADING FILE");
            return true;
        } catch (IOException e) {
            System.out.println("FAILURE");
            e.printStackTrace();
            return false;
        }

    }

    public static void testing(){
        try {
            FileList result = null;
            result = service.files().list()
                    .setQ("'" + getFolderId() + "' in parents and trashed = false")
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Takes in specific file to update
    public static void updateFile(String fileId){
        try {
            File file = service.files().get(fileId).execute();

            java.io.File filePath = new java.io.File("Google Drive Sync/TestFile.txt");
            FileContent mediaContent = new FileContent("text/txt", filePath);

            file = service.files().update(fileId,file, mediaContent).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Reads fileLocations.csv for folder ID
    //FolderID should always contained in the second line of folderID.csv. Returns null if not present
    public static String getFolderId(){
        java.io.File store = new java.io.File(FILELOCATIONS_PATH);
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
        if(line == null){
            return null;
        }
        String[] parts = line.split(",");
        if(parts[0].equals("Folder")){
            return parts[1];
        } else {
            return null;
        }
    }

    //Sets new folderID into fileLocations.csv, always stored in line 2
    //TODO Doesn't work when fileLocations is only 2 lines long.
    public static void setFolderId(String id){
        java.io.File store = new java.io.File(FILELOCATIONS_PATH);
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

            FileOutputStream fileOut = new FileOutputStream(FILELOCATIONS_PATH);
            fileOut.write(inputBuffer.toString().getBytes(StandardCharsets.UTF_8));
            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO Verify Location of folder on client computer
    public static Boolean verifyFolder(){
        try {
            if(getFolderId() == null){
                FileList result = service.files().list()
                        .setQ("name='Google Drive Sync' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .execute();
                if(result.getFiles().size() > 1){
                    System.out.println("Multiple Valid Folders found in Drive");
                    System.exit(1);
                } else if(result.getFiles().size() == 1){
                    System.out.println("set folderid from existing folder");
                    setFolderId(result.getFiles().get(0).getId());
                } else {
                    System.out.println("made folder");
                    makeFolder();
                }
                return true;
            } else {
                //TODO Check if folder from folderid is trashed, and if it doesn't, make new folder
                File folder = service.files().get(getFolderId())
                        .setFields("id,trashed")
                        .execute();
                System.out.println(folder.getId());
                //For some reason, this always returns null
                System.out.println(folder.getTrashed());
                if(folder.getTrashed()){
                    System.out.println("Folder is in trash, made new folder");
                    makeFolder();
                    return true;
                }

                return true;

            }
        } catch(GoogleJsonResponseException e){
            if(e.getStatusCode() == 404){
                System.out.println("File Id is invalid, made new folder");
                makeFolder();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Drive driveConnect() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        return service;



    }
}
