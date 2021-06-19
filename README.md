# Drive
Syncs files across computers using google drive. 

Note: Sensitive file are ignored, and this project won't work initially

Mandatory Files Ignored:
credentials.json  //Credentials file from google drive api. Not explicity sensitive, but leak of the file could allow other programs to impersonaten my app,
                  //so leaving it out until it seems unnecessary/I want to publish it publically, and a credentials file is necessary. 
                  
Other files ignored, but not mandatory:
folderId.txt      //Stores Id of google drive folder. 

tokens            //Folder that stores credentials to log into google drive after initial log in. If leaked shortly after creation, it could allow unwanted parties
                  //to access user's google drive. Obviously, I did not upload this file into github, for security reasons. If tokens is absent, user will have to 
                  //go through inital login routine. When token is created, leave it only on a device you trust. 
