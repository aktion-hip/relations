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
 * Wrapper for lucene Field with special date functionality.
 *
 * @author Luthiger
 * Created on 21.11.2008
 */
public class IndexerDateField extends IndexerField {
	public enum TimeResolution {YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND}

	private long time;
	private TimeResolution resolution;

	/**
	 * 
	 * @param inFieldName String
	 * @param inValue Time as long value
	 * @param inStore
	 * @param inIndex
	 * @param inResolution
	 */
	public IndexerDateField(String inFieldName, long inValue, Store inStore, Index inIndex, TimeResolution inResolution) {
		super(inFieldName, "", inStore, inIndex); //$NON-NLS-1$
		time = inValue;
		resolution = inResolution;
	}

	public long getTime() {
		return time;
	}

	public TimeResolution getResolution() {
		return resolution;
	}

}
