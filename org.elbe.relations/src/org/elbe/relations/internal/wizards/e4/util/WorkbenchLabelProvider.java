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
package org.elbe.relations.internal.wizards.e4.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Provides basic labels for adaptable objects that have the
 * <code>IWorkbenchAdapter</code> adapter associated with them. All dispensed
 * images are cached until the label provider is explicitly disposed. This class
 * provides a facility for subclasses to define annotations on the labels and
 * icons of adaptable objects.
 * 
 * @author Luthiger
 * @see org.eclipse.ui.model.WorkbenchLabelProvider
 */
public class WorkbenchLabelProvider extends LabelProvider {

	private ResourceManager resourceManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(final Object inElement) {
		final IWorkbenchAdapter lAdapter = getAdapter(inElement);
		if (lAdapter == null) {
			return "";
		}
		final String lLabel = lAdapter.getLabel(inElement);
		return lLabel;
	}

	/*
	 * (non-Javadoc) Method declared on ILabelProvider
	 */
	@Override
	public final Image getImage(final Object inElement) {
		// obtain the base image by querying the element
		final IWorkbenchAdapter lAdapter = getAdapter(inElement);
		if (lAdapter == null) {
			return null;
		}
		ImageDescriptor lDescriptor = lAdapter.getImageDescriptor(inElement);
		if (lDescriptor == null) {
			return null;
		}

		// add any annotations to the image descriptor
		lDescriptor = decorateImage(lDescriptor, inElement);

		return (Image) getResourceManager().get(lDescriptor);
	}

	/**
	 * Returns an image descriptor that is based on the given descriptor, but
	 * decorated with additional information relating to the state of the
	 * provided object.
	 * 
	 * Subclasses may reimplement this method to decorate an object's image.
	 * 
	 * @param inInput
	 *            The base image to decorate.
	 * @param inElement
	 *            The element used to look up decorations.
	 * @return the resuling ImageDescriptor.
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor
	 */
	protected ImageDescriptor decorateImage(final ImageDescriptor inInput,
			final Object inElement) {
		return inInput;
	}

	/**
	 * Returns the implementation of IWorkbenchAdapter for the given object.
	 * 
	 * @param inObject
	 *            the object to look up.
	 * @return IWorkbenchAdapter or<code>null</code> if the adapter is not
	 *         defined or the object is not adaptable.
	 */
	protected final IWorkbenchAdapter getAdapter(final Object inObject) {
		return (IWorkbenchAdapter) Util.getAdapter(inObject,
				IWorkbenchAdapter.class);
	}

	/**
	 * Lazy load the resource manager
	 * 
	 * @return The resource manager, create one if necessary
	 */
	private ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(
					JFaceResources.getResources());
		}

		return resourceManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		if (resourceManager != null)
			resourceManager.dispose();
		resourceManager = null;
		super.dispose();
	}

}
