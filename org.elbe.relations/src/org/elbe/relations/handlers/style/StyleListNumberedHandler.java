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
package org.elbe.relations.handlers.style;

import org.elbe.relations.internal.style.Styles;
import org.elbe.relations.internal.style.Styles.Style;

/**
 * Handler for the style ordered list command.
 * 
 * @author Luthiger
 */
public class StyleListNumberedHandler extends AbstractStyleHandler {
	private static Styles.Style STYLE = Style.NUMBERED;

	@Override
	protected Style getStyle() {
		return STYLE;
	}
}
