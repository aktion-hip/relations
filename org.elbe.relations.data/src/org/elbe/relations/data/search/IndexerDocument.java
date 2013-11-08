/*
This package is part of Relations project.
Copyright (C) 2007, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.elbe.relations.data.search;

import java.util.Collection;
import java.util.Vector;

/**
 * Wrapper for lucene Document
 *
 * @author Luthiger
 * Created on 20.11.2008
 */
public class IndexerDocument {
	private Collection<IndexerField> fields = new Vector<IndexerField>();
	
	/**
	 * @param inField IndexerField
	 */
	public void addField(IndexerField inField) {
		fields.add(inField);
	}

	/**
	 * Returns all fields added to the document.
	 * 
	 * @return Collection<IndexerField>
	 */
	public Collection<IndexerField> getFields() {
		return fields;
	}

}
