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
	private String dateCrated = ""; //$NON-NLS-1$
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
		return title;
	}

	/**
	 * @return String the comment.
	 */
	public String getText() {
		final StringBuilder outText = new StringBuilder();
		if (hasContent(comment)) {
			outText.append(comment).append(NL);
		}
		final StringBuilder lAdditional = getAdditional();
		if (lAdditional.length() != 0) {
			outText.append("[<i>").append(lAdditional).append("</i>]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return new String(outText).trim();
	}

	public void setTitle(final String inTitle) {
		title = inTitle;
	}

	private StringBuilder getAdditional() {
		final StringBuilder outText = new StringBuilder();

		boolean lFirst = true;
		lFirst = addPart(outText, author,
		        RelationsMessages.getString("ExtractedData.lbl.author") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
		        lFirst);
		lFirst = addPart(outText, publisher,
		        RelationsMessages.getString("ExtractedData.lbl.publisher") //$NON-NLS-1$
		                + ": ", //$NON-NLS-1$
		        lFirst);
		lFirst = addPart(outText, contributor,
		        RelationsMessages.getString("ExtractedData.lbl.contributor") //$NON-NLS-1$
		                + ": ", //$NON-NLS-1$
		        lFirst);
		lFirst = addPart(outText, filePath, resourceType + ": ", lFirst); //$NON-NLS-1$
		lFirst = addPart(outText, fileSize,
		        RelationsMessages.getString("ExtractedData.lbl.size") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
		        lFirst);
		lFirst = addPart(outText, fileType,
		        RelationsMessages.getString("ExtractedData.lbl.type") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
		        lFirst);
		lFirst = addPart(outText, dateCrated,
		        RelationsMessages.getString("ExtractedData.lbl.created") + ": ", //$NON-NLS-1$ //$NON-NLS-2$
		        lFirst);
		lFirst = addPart(outText, dateModified,
		        RelationsMessages.getString("ExtractedData.lbl.modified") //$NON-NLS-1$
		                + ": ", //$NON-NLS-1$
		        lFirst);
		return outText;
	}

	private boolean addPart(final StringBuilder inText, final String inField,
	        final String inLabel, boolean inFirst) {
		if (hasContent(inField)) {
			if (!inFirst) {
				inText.append(ITEM_SEPARATOR).append(NL);
			}
			inText.append(inLabel).append(inField);
			inFirst = false;
		}
		return inFirst;
	}

	private boolean hasContent(final String inContent) {
		return inContent == null ? false : inContent.length() != 0;
	}

	public void setFileSize(final long inLength) {
		if (inLength == 0) {
			return;
		}
		fileSize = FORMAT_DECIMAL.format((double) inLength / KB);
	}

	public void setFilePath(final String inFilePath) {
		filePath = inFilePath;
	}

	public void setURL(final String inUrl) {
		filePath = inUrl;
		resourceType = RESOURCE_URL;
	}

	public String getPath() {
		return filePath;
	}

	public void setFileType(final String inFileType) {
		fileType = inFileType;
	}

	public void setDateModified(final long inModified) {
		if (inModified == 0) {
			return;
		}
		final Date lDate = new Date(inModified);
		dateModified = String.format("%s, %s", //$NON-NLS-1$
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

	/**
	 * The item's creation date.
	 *
	 * @param inDate
	 *            Date
	 */
	public void setDateCreated(final Date inDate) {
		if (inDate == null) {
			return;
		}
		dateCrated = String.format("%s, %s", //$NON-NLS-1$
		        DateFormat.getDateInstance(DateFormat.LONG).format(inDate),
		        DateFormat.getTimeInstance(DateFormat.LONG).format(inDate)); // $NON-NLS-1$
		final Calendar lDate = Calendar.getInstance();
		lDate.setTime(inDate);
		year = String.valueOf(lDate.get(Calendar.YEAR));
	}

	/**
	 * The item's creation date.
	 *
	 * @param inDate
	 *            String the date in plain string format. (Not checked.)
	 */
	public void setDateCreated(final String inDate) {
		dateCrated = inDate;
	}

	public void setComment(final String inComment) {
		comment = inComment;
	}

	public void setAuthor(final String inAuthor) {
		if (inAuthor == null) {
			return;
		}
		author = inAuthor;
	}

	public String getAuthor() {
		return author;
	}

	public void setPublisher(final String inPublisher) {
		if (inPublisher == null) {
			return;
		}
		publisher = inPublisher;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setContributor(final String inContributor) {
		if (inContributor == null) {
			return;
		}
		contributor = inContributor;
	}

	public String getContributor() {
		return contributor;
	}

	public String getYear() {
		return year;
	}

}
