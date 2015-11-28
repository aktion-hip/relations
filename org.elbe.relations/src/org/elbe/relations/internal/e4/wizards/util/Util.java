/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.internal.e4.wizards.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Helper class
 *
 * @author Luthiger
 */
public final class Util {

	private Util() {
		// prevent instantiation
	}

	/**
	 * Utility for extracting the description child of an element.
	 *
	 * @param inConfigElement
	 *            the element
	 * @return the description
	 * @since 3.1
	 */
	public static String getDescription(
	        final IConfigurationElement inConfigElement) {
		final IConfigurationElement[] lChildren = inConfigElement
		        .getChildren(IWorkbenchRegistryConstants.TAG_DESCRIPTION);
		if (lChildren.length >= 1) {
			return lChildren[0].getValue();
		}
		return "";//$NON-NLS-1$
	}

	/**
	 * If it is possible to adapt the given object to the given type, this
	 * returns the adapter. Performs the following checks:
	 *
	 * <ol>
	 * <li>Returns <code>sourceObject</code> if it is an instance of the adapter
	 * type.</li>
	 * <li>If sourceObject implements IAdaptable, it is queried for adapters.
	 * </li>
	 * <li>If sourceObject is not an instance of PlatformObject (which would
	 * have already done so), the adapter manager is queried for adapters</li>
	 * </ol>
	 *
	 * Otherwise returns null.
	 *
	 * @param inSourceObject
	 *            object to adapt, or null
	 * @param inAdapterType
	 *            type to adapt to
	 * @return a representation of sourceObject that is assignable to the
	 *         adapter type, or null if no such representation exists
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(final Object inSourceObject,
	        final Class<T> inAdapterType) {
		Assert.isNotNull(inAdapterType);
		if (inSourceObject == null) {
			return null;
		}
		if (inAdapterType.isInstance(inSourceObject)) {
			return (T) inSourceObject;
		}

		if (inSourceObject instanceof IAdaptable) {
			final IAdaptable adaptable = (IAdaptable) inSourceObject;

			final Object outResult = adaptable.getAdapter(inAdapterType);
			if (outResult != null) {
				// Sanity-check
				Assert.isTrue(inAdapterType.isInstance(outResult));
				return (T) outResult;
			}
		}

		if (!(inSourceObject instanceof PlatformObject)) {
			final Object outResult = Platform.getAdapterManager()
			        .getAdapter(inSourceObject, inAdapterType);
			if (outResult != null) {
				return (T) outResult;
			}
		}

		return null;
	}

}
