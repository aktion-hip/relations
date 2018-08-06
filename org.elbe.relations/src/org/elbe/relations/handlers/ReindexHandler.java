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
package org.elbe.relations.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.elbe.relations.internal.actions.IndexerAction;

/**
 * Handler for reindexing the items in the actual database.
 * 
 * @author Luthiger
 */
public class ReindexHandler {

	@Execute
	public void execute(final IEclipseContext inContext)
	        throws InvocationTargetException, InterruptedException {
		final IndexerAction lAction = ContextInjectionFactory
		        .make(IndexerAction.class, inContext);
		lAction.setSilent(false);
		lAction.run();
	}

}
