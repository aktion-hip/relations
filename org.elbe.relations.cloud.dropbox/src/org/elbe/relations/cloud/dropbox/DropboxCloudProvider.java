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
package org.elbe.relations.cloud.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.services.ICloudProvider;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.google.gson.JsonObject;

/** The Dropbox Cloud Provider component to upload a file to the Dropbox folder.
 *
 * @author lbenno
 * @see https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/upload-file/src/main/java/com/dropbox/core/examples/upload_file/Main.java
 * @see https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/authorize/src/main/java/com/dropbox/core/examples/authorize/Main.java
 * @see https://dropbox.github.io/dropbox-sdk-java/api-docs/v2.0.x/com/dropbox/core/DbxWebAuth.html */
@SuppressWarnings("restriction")
public class DropboxCloudProvider implements ICloudProvider {
    private static final String DROP_BOX_ROOT = "/synchronization"; //$NON-NLS-1$
    private static final String DROP_BOX_PATH = DROP_BOX_ROOT + "/%s"; //$NON-NLS-1$
    private static final String DROP_BOX_CLIENT_IT = "relations-cloud/1.0"; //$NON-NLS-1$

    @Override
    public boolean upload(final File toExport, final String fileName, final JsonObject configuration,
            final boolean isFullExport, final Logger log) {
        final String token = getToken(configuration);
        if (token.isEmpty()) {
            return false;
        }

        final DbxRequestConfig config = new DbxRequestConfig(DROP_BOX_CLIENT_IT);
        final DbxClientV2 client = new DbxClientV2(config, token);
        uploadFile(client, toExport, String.format(DROP_BOX_PATH, fileName), log);
        if (isFullExport) {
            deleteIncremental(client, log);
        }
        return true;
    }

    private void deleteIncremental(final DbxClientV2 client, final Logger log) {
        try {
            ListFolderResult files = client.files().listFolder(DROP_BOX_ROOT);
            while (true) {
                for (final Metadata metadata : files.getEntries()) {
                    if (metadata.getName().startsWith("relations_delta_")) { //$NON-NLS-1$
                        client.files().deleteV2(metadata.getPathDisplay());
                    }
                }
                if (!files.getHasMore()) {
                    break;
                }
                files = client.files().listFolderContinue(files.getCursor());
            }
        } catch (final DbxException exc) {
            log.error(exc, "Error encountered during cleaning up Dropbox cloud!"); //$NON-NLS-1$
        }
    }

    private void uploadFile(final DbxClientV2 client, final File local, final String dropboxPath, final Logger log) {
        try (InputStream in = new FileInputStream(local)) {
            final FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.OVERWRITE)
                    .withClientModified(new Date(local.lastModified()))
                    .uploadAndFinish(in);
            log.trace(metadata.toStringMultiline());
        } catch (final IOException | DbxException exc) {
            log.error(exc, "Error encountered during export to Dropbox cloud!"); //$NON-NLS-1$
        }
    }

    private String getToken(final JsonObject configuration) {
        if (configuration.has(DropboxCloudProviderConfig.KEY_TOKEN)) {
            return configuration.get(DropboxCloudProviderConfig.KEY_TOKEN).getAsString();
        }
        return ""; //$NON-NLS-1$
    }

}
