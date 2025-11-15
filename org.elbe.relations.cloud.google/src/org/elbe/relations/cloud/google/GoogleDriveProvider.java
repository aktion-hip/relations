/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.PlainMessageDialog;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.services.ICloudProvider;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.JsonObject;

/** The Google Drive Provider component to upload a file to the Google Drive folder.
 *
 * @author lbenno */
public class GoogleDriveProvider implements ICloudProvider {
    private static final String APPLICATION_NAME = "Relations-rcp"; //$NON-NLS-1$
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final java.io.File TOKENS_DIRECTORY = new java.io.File("tokens");

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    private static final String DRIVE_PATH = "relations"; //$NON-NLS-1$
    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"; //$NON-NLS-1$
    private static final String MIME_TYPE_FILE = "application/zip"; //$NON-NLS-1$

    @Override
    public boolean upload(final java.io.File toExport, final String fileName, final JsonObject configuration,
            final boolean isFullExport, final Logger log) {

        Display.getDefault().asyncExec(() -> {
            PlainMessageDialog.getBuilder(Display.getDefault().getActiveShell(), "Google Drive")
            .message(
                    "This step opens a browser window where you have to login to your Google login and consent.")
            .buttonLabels(List.of("Close")).build().open();
        });

        try {
            final var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final Drive service = new Drive.Builder(httpTransport, JSON_FACTORY,
                    getCredentials(httpTransport, configuration))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            final String folderId = getFolderId(service);
            removeIfExists(service, fileName, folderId);

            // upload file
            final File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));

            final FileContent mediaContent = new FileContent(MIME_TYPE_FILE, toExport);
            service.files().create(fileMetadata, mediaContent)
            .setFields("id, parents").execute(); //$NON-NLS-1$

            if (isFullExport) {
                // remove all existing increments in the cloud storage
                removeIncremental(service, folderId);
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
        if (folders.isEmpty()) {
            // create folder
            final File folderMetadata = new File();
            folderMetadata.setName(DRIVE_PATH);
            folderMetadata.setMimeType(MIME_TYPE_FOLDER);

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
     * @param httpTransport
     * @param configuration
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If there is no client_secret. */
    private static Credential getCredentials(final NetHttpTransport httpTransport, final JsonObject configuration)
            throws IOException {
        // Load client secrets.
        final InputStream in = new ByteArrayInputStream(configuration.toString().getBytes());
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization if needed.
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(TOKENS_DIRECTORY))
                .setAccessType("offline").build(); //$NON-NLS-1$

        final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user"); //$NON-NLS-1$
    }

}
