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
package org.elbe.relations.parsing;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import org.elbe.relations.RelationsMessages;

/**
 * Parameter object that contains the extracted metadata.
 *
 * @author Luthiger Created on 14.01.2010
 */
public class ExtractedData {
    private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final String ITEM_SEPARATOR = ";"; //$NON-NLS-1$
    private static final int KB = 1024;
    private static final DecimalFormat FORMAT_DECIMAL = new DecimalFormat(
            "###,##0.00 kB"); //$NON-NLS-1$

    private static final String RESOURCE_FILE = "File"; //$NON-NLS-1$
    private static final String RESOURCE_URL = "URL"; //$NON-NLS-1$

    private String title = ""; //$NON-NLS-1$
    private String comment = ""; //$NON-NLS-1$
    private String fileSize = ""; //$NON-NLS-1$
    private String filePath = ""; //$NON-NLS-1$
    private String fileType = ""; //$NON-NLS-1$
    private String dateCreated = ""; //$NON-NLS-1$
    private String year = ""; //$NON-NLS-1$
    private String dateModified = ""; //$NON-NLS-1$
    private String author = ""; //$NON-NLS-1$
    private String publisher = ""; //$NON-NLS-1$
    private String contributor = ""; //$NON-NLS-1$
    private String resourceType = RESOURCE_FILE;

    /**
     * @return String the title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return String the comment.
     */
    public String getText() {
        final StringBuilder text = new StringBuilder();
        if (hasContent(this.comment)) {
            text.append(this.comment).append(NL);
        }
        final StringBuilder additional = getAdditional();
        if (additional.length() != 0) {
            text.append("[<i>").append(additional).append("</i>]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new String(text).trim();
    }

    public void setTitle(final String inTitle) {
        this.title = inTitle;
    }

    private StringBuilder getAdditional() {
        final StringBuilder outText = new StringBuilder();

        boolean lFirst = true;
        lFirst = addPart(outText, this.author,
                RelationsMessages.getString("ExtractedData.lbl.author") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
                lFirst);
        lFirst = addPart(outText, this.publisher,
                RelationsMessages.getString("ExtractedData.lbl.publisher") //$NON-NLS-1$
                + ": ", //$NON-NLS-1$
                lFirst);
        lFirst = addPart(outText, this.contributor,
                RelationsMessages.getString("ExtractedData.lbl.contributor") //$NON-NLS-1$
                + ": ", //$NON-NLS-1$
                lFirst);
        lFirst = addPart(outText, this.filePath, this.resourceType + ": ", lFirst); //$NON-NLS-1$
        lFirst = addPart(outText, this.fileSize,
                RelationsMessages.getString("ExtractedData.lbl.size") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
                lFirst);
        lFirst = addPart(outText, this.fileType,
                RelationsMessages.getString("ExtractedData.lbl.type") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
                lFirst);
        lFirst = addPart(outText, this.dateCreated,
                RelationsMessages.getString("ExtractedData.lbl.created") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
                lFirst);
        addPart(outText, this.dateModified,
                RelationsMessages.getString("ExtractedData.lbl.modified") //$NON-NLS-1$
                + ": ", //$NON-NLS-1$
                lFirst);
        return outText;
    }

    private boolean addPart(final StringBuilder text, final String field, final String label, boolean first) {
        if (hasContent(field)) {
            if (!first) {
                text.append(ITEM_SEPARATOR).append(NL);
            }
            text.append(label).append(field);
            first = false;
        }
        return first;
    }

    private boolean hasContent(final String content) {
        return content == null || !content.isBlank();
    }

    public void setFileSize(final long inLength) {
        if (inLength == 0) {
            return;
        }
        this.fileSize = FORMAT_DECIMAL.format((double) inLength / KB);
    }

    public void setFilePath(final String inFilePath) {
        this.filePath = inFilePath;
    }

    public void setURL(final String inUrl) {
        this.filePath = inUrl;
        this.resourceType = RESOURCE_URL;
    }

    public String getPath() {
        return this.filePath;
    }

    public void setFileType(final String inFileType) {
        this.fileType = inFileType;
    }

    public void setDateModified(final long inModified) {
        if (inModified == 0) {
            return;
        }
        final Date lDate = new Date(inModified);
        this.dateModified = String.format("%s, %s", //$NON-NLS-1$
                DateFormat.getDateInstance(DateFormat.LONG).format(lDate),
                DateFormat.getTimeInstance(DateFormat.LONG).format(lDate));
    }

    /**
     * The item's creation date.
     *
     * @param inCreated
     *            long the milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    public void setDateCreated(final long inCreated) {
        if (inCreated == 0) {
            return;
        }
        setDateCreated(new Date(inCreated));
    }

    /** The item's creation date.
     *
     * @param date {@link Date} */
    public void setDateCreated(final Date date) {
        if (date == null) {
            return;
        }
        this.dateCreated = String.format("%s, %s", //$NON-NLS-1$
                DateFormat.getDateInstance(DateFormat.LONG).format(date),
                DateFormat.getTimeInstance(DateFormat.LONG).format(date)); // $NON-NLS-1$
        final Calendar lDate = Calendar.getInstance();
        lDate.setTime(date);
        this.year = String.valueOf(lDate.get(Calendar.YEAR));
    }

    /**
     * The item's creation date.
     *
     * @param inDate
     *            String the date in plain string format. (Not checked.)
     */
    public void setDateCreated(final String inDate) {
        this.dateCreated = inDate;
    }

    public void setComment(final String inComment) {
        this.comment = inComment;
    }

    public void setAuthor(final String inAuthor) {
        if (inAuthor == null) {
            return;
        }
        this.author = inAuthor;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setPublisher(final String inPublisher) {
        if (inPublisher == null) {
            return;
        }
        this.publisher = inPublisher;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public void setContributor(final String inContributor) {
        if (inContributor == null) {
            return;
        }
        this.contributor = inContributor;
    }

    public String getContributor() {
        return this.contributor;
    }

    public String getYear() {
        return this.year;
    }

}
