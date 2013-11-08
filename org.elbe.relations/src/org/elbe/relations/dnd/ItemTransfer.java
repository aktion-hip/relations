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

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.elbe.relations.data.utility.UniqueID;

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

	@Inject
	private Logger log;

	// prevent public class instantiation
	private ItemTransfer() {
	}

	public static ItemTransfer getInstance(final Logger inLog) {
		if (instance.log == null && inLog != null) {
			instance.log = inLog;
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
	public void javaToNative(final Object inObject,
			final TransferData inTransferData) {
		if (!checkUniqueID(inObject) || !isSupportedType(inTransferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		final UniqueID[] lIDs = (UniqueID[]) inObject;
		ByteArrayOutputStream lOut = null;
		DataOutputStream lWrite = null;
		try {
			// write data to byte array
			lOut = new ByteArrayOutputStream();
			lWrite = new DataOutputStream(lOut);
			for (int i = 0; i < lIDs.length; i++) {
				lWrite.writeInt(lIDs[i].itemType);
				lWrite.writeLong(lIDs[i].itemID);
			}
			final byte[] lBuffer = lOut.toByteArray();
			super.javaToNative(lBuffer, inTransferData);
		}
		catch (final IOException exc) {
			if (log != null) {
				log.error(exc, exc.getMessage());
			}
		} finally {
			try {
				if (lWrite != null)
					lWrite.close();
				if (lOut != null)
					lOut.close();
			}
			catch (final Exception exc) {
				// intentionally left empty
			}
		}
	}

	@Override
	public Object nativeToJava(final TransferData inTransferData) {
		if (isSupportedType(inTransferData)) {
			final byte[] lBuffer = (byte[]) super.nativeToJava(inTransferData);
			if (lBuffer == null) {
				return null;
			}

			UniqueID[] outIDs = new UniqueID[0];
			ByteArrayInputStream lIn = null;
			DataInputStream lRead = null;
			try {
				lIn = new ByteArrayInputStream(lBuffer);
				lRead = new DataInputStream(lIn);
				while (lRead.available() > 5) {
					final int lItemType = lRead.readInt();
					final long lItemID = lRead.readLong();
					final UniqueID lID = new UniqueID(lItemType, lItemID);

					final UniqueID[] lNewIDs = new UniqueID[outIDs.length + 1];
					System.arraycopy(outIDs, 0, lNewIDs, 0, outIDs.length);
					lNewIDs[outIDs.length] = lID;
					outIDs = lNewIDs;
				}
			}
			catch (final IOException exc) {
				if (log != null) {
					log.error(exc, exc.getMessage());
				}
				return null;
			} finally {
				try {
					if (lRead != null)
						lRead.close();
					if (lIn != null)
						lIn.close();
				}
				catch (final Exception exc) {
					// intentionally left empty
				}
			}

			return outIDs;
		}
		return null;
	}

	private boolean checkUniqueID(final Object inObject) {
		if (inObject == null || !(inObject instanceof UniqueID[])) {
			return false;
		}
		final UniqueID[] lIDs = (UniqueID[]) inObject;
		for (int i = 0; i < lIDs.length; i++) {
			if (lIDs[i] == null || lIDs[i].itemID == 0 || lIDs[i].itemType == 0) {
				return false;
			}
		}
		return true;
	}

}
