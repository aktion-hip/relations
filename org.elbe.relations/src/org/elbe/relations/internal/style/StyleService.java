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
package org.elbe.relations.internal.style;


/**
 * <p>
 * A service to manage the <code>StyledText</code>'s styles.
 * </p>
 * <p>
 * This class mediates between the styled text widget's styles provider and the
 * listener classes observing style changes. The style observers can register to
 * this class whereas this class registers to the <code>IStyleProvider</code>.
 * Style changes reported by the style provider are then propagated to the
 * registered listeners.
 * </p>
 * 
 * @author Luthiger Created on 27.08.2007
 * @see IStyleProvider
 * @see IStyleListener
 */
public class StyleService {
	public static final Object STATE_EDITABLE = new Object();
	public static final Object STATE_READ_ONLY = new Object();
	public static final Object UNDEFINED = new Object();

}
