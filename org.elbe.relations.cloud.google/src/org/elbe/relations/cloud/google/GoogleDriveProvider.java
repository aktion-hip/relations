/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.cloud.google;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.services.ICloudProvider;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.JsonObject;

/** The Google Drive Provider component to upload a file to the Google Drive folder.
 *
 * @see https://github.com/google/google-api-java-client-samples/blob/master/drive-cmdline-sample/src/main/java/com/google/api/services/samples/drive/cmdline/DriveSample.java
 * @see https://developers.google.com/drive/api/v3/manage-uploads
 * @author lbenno */
@SuppressWarnings("restriction")
public class GoogleDriveProvider implements ICloudProvider {
    private static final String APPLICATION_NAME = "Relations-rcp"; //$NON-NLS-1$
    private static final String DRIVE_PATH = "relations"; //$NON-NLS-1$
    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"; //$NON-NLS-1$
    private static final String MIME_TYPE_FILE = "application/zip"; //$NON-NLS-1$
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE,
            DriveScopes.DRIVE_APPDATA);
    private static final String CLIENT_SECRET_DIR = "client_secret.json"; //$NON-NLS-1$
    private static final String CREDENTIALS_FOLDER = "gd_credentials"; //$NON-NLS-1$

    @Override
    public boolean upload(final java.io.File toExport, final String fileName, final JsonObject configuration,
            final boolean isFullExport, final Logger log) {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            final Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            final String folderId = getFolderId(drive);
            removeIfExists(drive, fileName, folderId);

            // upload file
            final File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));

            final FileContent mediaContent = new FileContent(MIME_TYPE_FILE, toExport);
            drive.files().create(fileMetadata, mediaContent).setFields("id, parents").execute(); //$NON-NLS-1$

            if (isFullExport) {
                removeIncremental(drive, folderId);
            }
        } catch (GeneralSecurityException | IOException exc) {
            log.error(exc, "Unable to upload the data export to Google Drive!"); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private String getFolderId(final Drive drive) throws IOException {
        // test if folder exists
        final FileList folderList = drive.files().list()
                .setQ(String.format(
                        "mimeType='%s' and trashed=false and name = '%s'", MIME_TYPE_FOLDER, DRIVE_PATH)) //$NON-NLS-1$
                .execute();
        final List<File> folders = folderList.getFiles();
        if (folders.size() == 0) {
            // create folder
            final File folderMetadata = new File();
            folderMetadata.setName(DRIVE_PATH);
            folderMetadata.setMimeType("application/vnd.google-apps.folder"); //$NON-NLS-1$

            final File folder = drive.files().create(folderMetadata).setFields("id").execute(); //$NON-NLS-1$
            return folder.getId();
        }
        return folders.get(0).getId();
    }

    private void removeIfExists(final Drive drive, final String fileName, final String folderId) throws IOException {
        final FileList fileList = drive.files().list()
                .setQ(String.format(
                        "mimeType='%s' and trashed=false and name = '%s' and '%s' in parents", MIME_TYPE_FILE, //$NON-NLS-1$
                        fileName, folderId))
                .execute();
        for (final File file : fileList.getFiles()) {
            drive.files().delete(file.getId()).execute();
        }
    }

    private void removeIncremental(final Drive drive, final String folderId) throws IOException {
        final FileList fileList = drive.files().list()
                .setQ(String.format(
                        "mimeType='%s' and trashed=false and name contains 'relations_delta_' and '%s' in parents", //$NON-NLS-1$
                        MIME_TYPE_FILE, folderId))
                .execute();
        for (final File file : fileList.getFiles()) {
            drive.files().delete(file.getId()).execute();
        }
    }

    /** Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If there is no client_secret. */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        final InputStream input = GoogleDriveProvider.class.getResourceAsStream(CLIENT_SECRET_DIR);
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(input));

        // Build flow and trigger user authorization request.
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(getCredentialsFolder()))
                .setAccessType("offline") //$NON-NLS-1$
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user"); //$NON-NLS-1$
    }

    private java.io.File getCredentialsFolder() {
        final java.io.File parent = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
        return new java.io.File(parent, CREDENTIALS_FOLDER);
    }

}
