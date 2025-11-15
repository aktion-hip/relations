/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
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
package org.elbe.relations.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.elbe.relations.data.utility.UniqueID;

import jakarta.inject.Inject;

/**
 * Transfer class for drag and drop of items. The data being transferred is an
 * <bold>array of type UniqueID</bold>.
 *
 * @author Luthiger Created on 09.08.2006
 */
@SuppressWarnings("restriction")
public class ItemTransfer extends ByteArrayTransfer {
    private static final String ITEM_TRANSFER = "RELATIONS_ITEM_TRANSFER"; //$NON-NLS-1$
    private static final int ITEM_TRANSFER_ID = registerType(ITEM_TRANSFER);

    private static ItemTransfer instance = new ItemTransfer();

    private Logger log;

    // prevent public class instantiation
    private ItemTransfer() {
    }

    @Inject
    public static ItemTransfer getInstance(final Logger logger) {
        if (instance.log == null && logger != null) {
            instance.log = logger;
        }
        return instance;
    }

    /**
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[] { ITEM_TRANSFER_ID };
    }

    /**
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { ITEM_TRANSFER };
    }

    /**
     * @see org.eclipse.swt.dnd.Transfer#validate(java.lang.Object)
     */
    @Override
    protected boolean validate(final Object inObject) {
        return checkUniqueID(inObject);
    }

    @Override
    public void javaToNative(final Object object, final TransferData transferData) {
        if (!checkUniqueID(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }
        final UniqueID[] lIDs = (UniqueID[]) object;
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            // write data to byte array
            final DataOutputStream outData = new DataOutputStream(outStream);
            for (int i = 0; i < lIDs.length; i++) {
                outData.writeInt(lIDs[i].itemType);
                outData.writeLong(lIDs[i].itemID);
            }
            final byte[] lBuffer = outStream.toByteArray();
            super.javaToNative(lBuffer, transferData);
            outData.close();
        }
        catch (final IOException exc) {
            if (this.log != null) {
                this.log.error(exc, exc.getMessage());
            }
        }
    }

    @Override
    public Object nativeToJava(final TransferData transferData) {
        if (isSupportedType(transferData)) {
            final byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null) {
                return null;
            }

            UniqueID[] outIDs = new UniqueID[0];
            try (ByteArrayInputStream inStream = new ByteArrayInputStream(buffer)) {
                final DataInputStream inData = new DataInputStream(inStream);
                while (inData.available() > 5) {
                    final int lItemType = inData.readInt();
                    final long lItemID = inData.readLong();
                    final UniqueID lID = new UniqueID(lItemType, lItemID);

                    final UniqueID[] lNewIDs = new UniqueID[outIDs.length + 1];
                    System.arraycopy(outIDs, 0, lNewIDs, 0, outIDs.length);
                    lNewIDs[outIDs.length] = lID;
                    outIDs = lNewIDs;
                }
                inData.close();
            }
            catch (final IOException exc) {
                if (this.log != null) {
                    this.log.error(exc, exc.getMessage());
                }
                return null;
            }

            return outIDs;
        }
        return null;
    }

    private boolean checkUniqueID(final Object object) {
        if (!(object instanceof UniqueID[])) {
            return false;
        }
        final UniqueID[] ids = (UniqueID[]) object;
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == null || ids[i].itemID == 0 || ids[i].itemType == 0) {
                return false;
            }
        }
        return true;
    }

}
