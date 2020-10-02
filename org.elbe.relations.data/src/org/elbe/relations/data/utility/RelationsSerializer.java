package org.elbe.relations.data.utility;

import java.nio.charset.StandardCharsets;
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
    protected void startDomainObject(final GeneralDomainObject object) {
        emit_nl();
        emitStartTag(String.format(TMPL_TAG, object.getObjectName()));
    }

    @Override
    protected void endDomainObject(final GeneralDomainObject object) {
        emit_nl();
        emit_indent();
        emitEndTag(String.format(TMPL_TAG, object.getObjectName()));
    }

    @Override
    protected void startIterator(final DomainObjectIterator iterator) {
        // intentionally left empty
    }

    @Override
    protected void endIterator(final DomainObjectIterator iterator) {
        // intentionally left empty
    }

    @Override
    protected void startProperty(final Property property) {
        emit_nl();
        final Object value = property.getValue();
        final PropertyDef propertyDef = property.getPropertyDef();
        emitStartTag(String.format(
                "%s field=\"%s\" type=\"%s\"", property.getName(), //$NON-NLS-1$
                propertyDef.getMappingDef().getColumnName(),
                propertyDef.getValueType()));

        final String lFormatPattern = property.getFormatPattern();

        if (value == null) {
            return;
        }

        if ("none".equals(lFormatPattern)) { //$NON-NLS-1$
            emitText(property.getValue());
            return;
        }

        if (value instanceof Timestamp || value instanceof Date
                || value instanceof Time) {
            emitText(value.toString());
            return;
        }

        if (value instanceof Number) {
            if (lFormatPattern != null) {
                if (lFormatPattern.equals(" ") && ((Number) value).intValue() == 0) {
                    return;
                }

                this.decimalFormat.applyPattern(lFormatPattern);
                emitText(this.decimalFormat.format(value));
                return;
            } else {
                emitText(value);
                return;
            }
        }

        emitText(prepareForExport(value.toString()));
    }

    @Override
    protected void endProperty(final Property property) {
        emit_nl();
        emit_indent();
        emitEndTag(property.getName());
    }

    @Override
    protected void startPropertySet(final PropertySet set) {
        // intentionally left empty
    }

    @Override
    protected void endPropertySet(final PropertySet set) {
        // intentionally left empty
    }

    @Override
    protected void endSortedArray(final SortedArray sortedArray) {
        // intentionally left empty
    }

    @Override
    protected void startSortedArray(final SortedArray sortedArray) {
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
     * @param toProcess
     *            String to process
     * @return String
     */
    public static String prepareForExport(final String toProcess) {
        String processed = toProcess;
        for (int i = 0; i < IN_EXPORTED_XML.length; i++) {
            processed = processed.replace(IN_DATABASE[i],
                    IN_EXPORTED_XML[i]);
        }
        return processed;
    }

    /**
     * Convenience method: prepares text which will be imported from an XML
     * file. Reverts the process of
     * <code>RelationsSerializer.prepareForExport()</code>.
     *
     * @param toProcess
     *            String to process
     * @return String
     */
    public static String prepareForImport(final String toProcess) {
        String processed = toProcess;
        for (int i = 0; i < IN_EXPORTED_XML.length; i++) {
            processed = processed.replace(IN_EXPORTED_XML[i],
                    IN_DATABASE[i]);
        }
        // character encoding for text fields
        return new String(processed.getBytes(), StandardCharsets.UTF_8);
    }

}
