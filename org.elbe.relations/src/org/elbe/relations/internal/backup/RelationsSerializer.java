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
package org.elbe.relations.internal.backup;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;

import org.hip.kernel.bom.AbstractSerializer;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.SortedArray;
import org.hip.kernel.bom.model.PropertyDef;

/**
 * Serializer to export the entries of the Relations database into an XML file.
 *
 * @author Luthiger Created on 04.10.2008
 */
@SuppressWarnings("serial")
public class RelationsSerializer extends AbstractSerializer {
	// constants
	private final static String[] IN_EXPORTED_XML = { "&amp;" }; //$NON-NLS-1$
	private final static String[] IN_DATABASE = { "&" }; //$NON-NLS-1$
	private static final char[][] CLEAN = { { (char) 0x08, (char) 0x5c },
			{ (char) 0x1a, (char) 0x5c } };
	private final static String TMPL_TAG = "%sEntry"; //$NON-NLS-1$

	private final DecimalFormat decimalFormat = new DecimalFormat();

	@Override
	protected void startDomainObject(final GeneralDomainObject inObject) {
		emit_nl();
		emitStartTag(String.format(TMPL_TAG, inObject.getObjectName()));
	}

	@Override
	protected void endDomainObject(final GeneralDomainObject inObject) {
		emit_nl();
		emit_indent();
		emitEndTag(String.format(TMPL_TAG, inObject.getObjectName()));
	}

	@Override
	protected void startIterator(final DomainObjectIterator inIterator) {
		// intentionally left empty
	}

	@Override
	protected void endIterator(final DomainObjectIterator inIterator) {
		// intentionally left empty
	}

	@Override
	protected void startProperty(final Property inProperty) {
		emit_nl();
		final Object lValue = inProperty.getValue();
		final PropertyDef lPropertyDef = inProperty.getPropertyDef();
		emitStartTag(String.format(
				"%s field=\"%s\" type=\"%s\"", inProperty.getName(), //$NON-NLS-1$
				lPropertyDef.getMappingDef().getColumnName(),
				lPropertyDef.getValueType()));

		final String lFormatPattern = inProperty.getFormatPattern();

		if (lValue == null)
			return;

		if ("none".equals(lFormatPattern)) { //$NON-NLS-1$
			emitText(inProperty.getValue());
			return;
		}

		if ((lValue instanceof Timestamp) || (lValue instanceof Date)
				|| (lValue instanceof Time)) {
			emitText(lValue.toString());
			return;
		}

		if (lValue instanceof Number) {
			if (lFormatPattern != null) {
				if (lFormatPattern.equals(" ") && ((Number) lValue).intValue() == 0) //$NON-NLS-1$
					return;

				this.decimalFormat.applyPattern(lFormatPattern);
				emitText(this.decimalFormat.format(lValue));
				return;
			} else {
				emitText(lValue);
				return;
			}
		}

		emitText(prepareForExport(lValue.toString()));
	}

	@Override
	protected void endProperty(final Property inProperty) {
		emit_nl();
		emit_indent();
		emitEndTag(inProperty.getName());
	}

	@Override
	protected void startPropertySet(final PropertySet inSet) {
		// intentionally left empty
	}

	@Override
	protected void endPropertySet(final PropertySet inSet) {
		// intentionally left empty
	}

	@Override
	protected void endSortedArray(final SortedArray inSortedArray) {
		// intentionally left empty
	}

	@Override
	protected void startSortedArray(final SortedArray inSortedArray) {
		// intentionally left empty
	}

	@Override
	public String toString() {
		String out = getBuffer2().toString();
		for (int i = 0; i < CLEAN.length; i++) {
			out = out.replace(CLEAN[i][0], CLEAN[i][1]);
		}
		return out;
	}

	/**
	 * Convenience method: prepares text which will be exported to an XML file.
	 * Makes the processed text valid XML in a text node.
	 *
	 * @param inToProcess
	 *            String to process
	 * @return String
	 */
	public static String prepareForExport(final String inToProcess) {
		String outProcessed = inToProcess;
		for (int i = 0; i < IN_EXPORTED_XML.length; i++) {
			outProcessed = outProcessed.replace(IN_DATABASE[i],
					IN_EXPORTED_XML[i]);
		}
		return outProcessed;
	}

	/**
	 * Convenience method: prepares text which will be imported from an XML
	 * file. Reverts the process of
	 * <code>RelationsSerializer.prepareForExport()</code>.
	 *
	 * @param inToProcess
	 *            String to process
	 * @return String
	 */
	public static String prepareForImport(final String inToProcess) {
		String outProcessed = inToProcess;
		for (int i = 0; i < IN_EXPORTED_XML.length; i++) {
			outProcessed = outProcessed.replace(IN_EXPORTED_XML[i],
					IN_DATABASE[i]);
		}
		return outProcessed;
	}

}
