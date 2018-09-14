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
package org.elbe.relations.cloud.azure;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.services.ICloudProvider;

import com.google.gson.JsonObject;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.ListFileItem;

/** The MS Azure Provider component to upload a file to the Azure folder.
 *
 * @author lbenno */
@SuppressWarnings("restriction")
public class AzureProvider implements ICloudProvider {
    private static final String AZ_SHARE = "relations"; //$NON-NLS-1$

    @Override
    public boolean upload(final File toExport, final String fileName, final JsonObject configuration,
            final boolean isFullExport, final Logger log) {
        final String connectString = getConnectString(configuration);
        if (connectString.isEmpty()) {
            return false;
        }

        try {
            final CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectString);
            final CloudFileClient fileClient = storageAccount.createCloudFileClient();
            final CloudFileShare share = fileClient.getShareReference(AZ_SHARE);
            if (share.createIfNotExists()) {
                log.info("Created new share /" + AZ_SHARE + " on MS Azure."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            final CloudFileDirectory rootDir = share.getRootDirectoryReference();
            final CloudFile cloudFile = rootDir.getFileReference(fileName);
            cloudFile.uploadFromFile(toExport.getAbsolutePath());

            if (isFullExport) {
                // remove all existing increments in the cloud storage
                final Iterable<ListFileItem> incrementalFiles = rootDir.listFilesAndDirectories("relations_delta_", //$NON-NLS-1$
                        null, null);
                for (final ListFileItem incrementalFile : incrementalFiles) {
                    if (incrementalFile instanceof CloudFile) {
                        ((CloudFile) incrementalFile).delete();
                    }
                }
            }
            return true;
        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException exc) {
            log.error(exc, "Unable to upload data to MS Azure!"); //$NON-NLS-1$
        }
        return false;
    }

    private String getConnectString(final JsonObject configuration) {
        if (configuration.has(AzureProviderConfig.KEY_CONNECT)) {
            return configuration.get(AzureProviderConfig.KEY_CONNECT).getAsString();
        }
        return ""; //$NON-NLS-1$
    }

}
