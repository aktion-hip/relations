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
package org.elbe.relations.internal.utility;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.elbe.relations.RelationsMessages;
import org.osgi.framework.BundleContext;

/**
 * This is a utility class providing convenience functionality for dialog forms
 * 
 * @author Luthiger Created on 21.12.2006
 */
public class FormUtility {
	// constants
	public static final Image IMG_INFO = FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION)
			.getImage();
	public static final Image IMG_HINT = FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL)
			.getImage();
	public static final Image IMG_ERROR = FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
	public static final Image IMG_REQUIRED = FieldDecorationRegistry
			.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED)
			.getImage();

	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * Returns the error message from a <code>IStatus</code>. The
	 * <code>IStatus.severity</code> must be at least <code>IStatus.ERROR</code>
	 * . In case of <code>MultiStatus</code> with max severity of
	 * <code>IStatus.ERROR</code>, all messages of severity >
	 * <code>IStatus.OK</code> are included.
	 * 
	 * @param inStatus
	 *            IStatus
	 * @return String The error message or <code>null</code>.
	 */
	public static String getErrorMessage(final IStatus inStatus) {
		if (inStatus.getSeverity() < IStatus.ERROR)
			return null;

		if (inStatus.isMultiStatus()) {
			final StringBuffer outMessage = new StringBuffer();
			final IStatus[] lStati = inStatus.getChildren();
			boolean lFirst = true;
			for (int i = 0; i < lStati.length; i++) {
				if (!lStati[i].isOK()) {
					if (!lFirst) {
						outMessage.append(NL);
					}
					lFirst = false;
					outMessage.append(lStati[i].getMessage());
				}
			}
			return new String(outMessage);
		}
		return inStatus.getMessage();
	}

	/**
	 * Creates an error status for the specified error.
	 * 
	 * @param inMessage
	 *            String error message
	 * @param inContext
	 *            {@link BundleContext}
	 * @return IStatus
	 */
	public static IStatus createErrorStatus(final String inMessage,
			final BundleContext inContext) {
		return createErrorStatus(inMessage, inContext.getBundle()
				.getSymbolicName());
	}

	/**
	 * Creates an error status for the specified error.
	 * 
	 * @param inMessage
	 *            String error message
	 * @param inBundleID
	 *            String
	 * @return {@link IStatus}
	 */
	public static IStatus createErrorStatus(final String inMessage,
			final String inBundleID) {
		return new Status(Status.ERROR, inBundleID, 1, inMessage, null);
	}

	/**
	 * Creates an instance of <code>FieldDecoration</code> to decorate a
	 * required field.
	 * 
	 * @return FieldDecoration
	 */
	public final static FieldDecoration createRequiredDecoration() {
		return createRequiredDecoration(IMG_REQUIRED);
	}

	/**
	 * Creates an instance of <code>FieldDecoration</code> to decorate a
	 * required field with the specified image.
	 * 
	 * @param inImage
	 *            Image
	 * @return FieldDecoration
	 */
	public final static FieldDecoration createRequiredDecoration(
			final Image inImage) {
		return new FieldDecoration(inImage,
				RelationsMessages.getString("RequiredText.deco.required")); //$NON-NLS-1$
	}

	/**
	 * Decorates the specified control with a info marker displaying the
	 * specified text as mouse over.
	 * 
	 * @param inControl
	 *            the Control to decorate
	 * @param inInfo
	 *            String the information text to display as mouse over
	 * @return ControlDecoration the hint decoration
	 */
	public final static ControlDecoration addDecorationInfo(
			final Control inControl, final String inInfo) {
		final ControlDecoration outDecoration = new ControlDecoration(
				inControl, SWT.LEFT | SWT.TOP);
		outDecoration.setImage(IMG_INFO);
		outDecoration.setDescriptionText(inInfo);
		outDecoration.setShowOnlyOnFocus(true);
		return outDecoration;
	}

	/**
	 * Decorates the specified control with a hint displaying the specified text
	 * as mouse over.
	 * 
	 * @param inControl
	 *            the Control to decorate
	 * @param inHint
	 *            String the hint text to display as mouse over
	 * @return ControlDecoration the hint decoration
	 */
	public final static ControlDecoration addDecorationHint(
			final Control inControl, final String inHint) {
		final ControlDecoration outDecoration = new ControlDecoration(
				inControl, SWT.LEFT | SWT.TOP);
		outDecoration.setImage(IMG_HINT);
		outDecoration.setDescriptionText(inHint);
		outDecoration.setShowOnlyOnFocus(true);
		return outDecoration;
	}

	/**
	 * Decorates the specified control with a required marker.
	 * 
	 * @param inControl
	 *            the Control to decorate
	 * @return ControlDecoration the decoration
	 */
	public final static ControlDecoration addDecorationRequired(
			final Control inControl) {
		final ControlDecoration outDecoration = new ControlDecoration(
				inControl, SWT.LEFT | SWT.BOTTOM);
		outDecoration.setImage(IMG_REQUIRED);
		outDecoration.setDescriptionText(RelationsMessages
				.getString("RequiredText.deco.required")); //$NON-NLS-1$
		outDecoration.show();
		return outDecoration;
	}

	/**
	 * Decorates the specified control with an error marker.
	 * 
	 * @param inControl
	 *            the Control to decorate
	 * @return ControlDecoration the decoration
	 */
	public final static ControlDecoration addDecorationError(
			final Control inControl) {
		final ControlDecoration outDecoration = new ControlDecoration(
				inControl, SWT.LEFT | SWT.BOTTOM);
		outDecoration.setImage(IMG_ERROR);
		outDecoration.hide();
		return outDecoration;
	}

}
