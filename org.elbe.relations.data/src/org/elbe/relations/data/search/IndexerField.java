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

/**
 * Wrapper for lucene Field
 *
 * @author Luthiger
 * Created on 20.11.2008
 */
public class IndexerField {
	public enum Store {YES, NO, COMPRESS};
	public enum Index {ANALYZED, NOT_ANALYZED, ANALYZED_NO_NORMS, NOT_ANALYZED_NO_NORMS, NO}

	private String fieldName;
	private String value;
	private Store storeValue;
	private Index indexValue;

	/**
	 * IndexerField constructor.
	 * 
	 * @param inFieldName String
	 * @param inValue String
	 * @param inStore IndexerField.Store
	 * @param inIndex IndexerField.Index
	 */
	public IndexerField(String inFieldName, String inValue, Store inStore, Index inIndex) {
		fieldName = inFieldName;
		value = inValue;
		storeValue = inStore;
		indexValue = inIndex;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getValue() {
		return value;
	}

	public Store getStoreValue() {
		return storeValue;
	}

	public Index getIndexValue() {
		return indexValue;
	}
	
	@Override
	public String toString() {
		return String.format("%s: %s", fieldName, value); //$NON-NLS-1$
	}
	
}
