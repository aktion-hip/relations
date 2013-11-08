/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elbe.relations.biblio.meta.internal.pdf;

import java.io.IOException;
import java.util.Calendar;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSBase;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDictionary;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSName;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSObjectable;

/**
 * This is the document metadata.  Each getXXX method will return the entry if
 * it exists or null if it does not exist.  If you pass in null for the setXXX
 * method then it will clear the value.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class PDDocumentInformation implements COSObjectable {
    private static final COSName TITLE = COSName.getPDFName( "Title" ); //$NON-NLS-1$
    private static final COSName AUTHOR = COSName.getPDFName( "Author" ); //$NON-NLS-1$
    private static final COSName SUBJECT = COSName.getPDFName( "Subject" ); //$NON-NLS-1$
    private static final COSName KEYWORDS = COSName.getPDFName( "Keywords" ); //$NON-NLS-1$
    private static final COSName CREATOR = COSName.getPDFName( "Creator" ); //$NON-NLS-1$
    private static final COSName PRODUCER = COSName.getPDFName( "Producer" ); //$NON-NLS-1$
    private static final COSName CREATION_DATE = COSName.getPDFName( "CreationDate" ); //$NON-NLS-1$
    private static final COSName MODIFICATION_DATE = COSName.getPDFName( "ModDate" ); //$NON-NLS-1$
//    private static final COSName TRAPPED = COSName.getPDFName( "Trapped" );
    private COSDictionary info;

    /**
     * Default Constructor.
     */
    public PDDocumentInformation() {
        info = new COSDictionary();
    }

    /**
     * Constructor that is used for a preexisting dictionary.
     *
     * @param inDictionary The underlying dictionary.
     */
    public PDDocumentInformation(COSDictionary inDictionary) {
        info = inDictionary;
    }

	/* (non-Javadoc)
	 * @see org.elbe.relations.biblio.meta.internal.pdf.COSObjectable#getCOSObject()
	 */
	@Override
	public COSBase getCOSObject() {
		return info;
	}

	/**
     * This will get the underlying dictionary that this object wraps.
     *
     * @return The underlying info dictionary.
     */
    public COSDictionary getDictionary() {
        return info;
    }

    /**
     * This will get the title of the document.  This will return null if no title exists.
     *
     * @return The title of the document.
     */
    public String getTitle() {
        return info.getString(TITLE);
    }

    /**
     * This will set the title of the document.
     *
     * @param inTitle The new title for the document.
     */
    public void setTitle(String inTitle) {
        info.setString(TITLE, inTitle);
    }

    /**
     * This will get the author of the document.  This will return null if no author exists.
     *
     * @return The author of the document.
     */
    public String getAuthor() {
        return info.getString(AUTHOR);
    }

    /**
     * This will set the author of the document.
     *
     * @param inAuthor The new author for the document.
     */
    public void setAuthor(String inAuthor) {
        info.setString(AUTHOR, inAuthor);
    }

    /**
     * This will get the subject of the document.  This will return null if no subject exists.
     *
     * @return The subject of the document.
     */
    public String getSubject() {
        return info.getString(SUBJECT);
    }

    /**
     * This will set the subject of the document.
     *
     * @param inSubject The new subject for the document.
     */
    public void setSubject(String inSubject) {
        info.setString(SUBJECT, inSubject);
    }
    /**
     * This will get the keywords of the document.  This will return null if no keywords exists.
     *
     * @return The keywords of the document.
     */
    public String getKeywords() {
        return info.getString(KEYWORDS);
    }

    /**
     * This will set the keywords of the document.
     *
     * @param inKeywords The new keywords for the document.
     */
    public void setKeywords(String inKeywords ) {
        info.setString(KEYWORDS, inKeywords);
    }

    /**
     * This will get the creator of the document.  This will return null if no creator exists.
     *
     * @return The creator of the document.
     */
    public String getCreator() {
        return info.getString(CREATOR);
    }

    /**
     * This will set the creator of the document.
     *
     * @param inCreator The new creator for the document.
     */
    public void setCreator(String inCreator) {
        info.setString(CREATOR, inCreator);
    }

    /**
     * This will get the producer of the document.  This will return null if no producer exists.
     *
     * @return The producer of the document.
     */
    public String getProducer() {
        return info.getString(PRODUCER);
    }

    /**
     * This will set the producer of the document.
     *
     * @param inProducer The new producer for the document.
     */
    public void setProducer(String inProducer) {
        info.setString(PRODUCER, inProducer);
    }

    /**
     * This will get the creation date of the document.  This will return null if no creation date exists.
     *
     * @return The creation date of the document.
     *
     * @throws IOException If there is an error creating the date.
     */
    public Calendar getCreationDate() throws IOException {
        return info.getDate(CREATION_DATE);
    }

    /**
     * This will set the creation date of the document.
     *
     * @param inDate The new creation date for the document.
     */
    public void setCreationDate(Calendar inDate) {
        info.setDate(CREATION_DATE, inDate);
    }

    /**
     * This will get the modification date of the document.  This will return null if no modification date exists.
     *
     * @return The modification date of the document.
     *
     * @throws IOException If there is an error creating the date.
     */
    public Calendar getModificationDate() throws IOException {
        return info.getDate(MODIFICATION_DATE);
    }

    /**
     * This will set the modification date of the document.
     *
     * @param date The new modification date for the document.
     */
    public void setModificationDate(Calendar date) {
        info.setDate( MODIFICATION_DATE, date );
    }

}
