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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDictionary;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDocument;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSName;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class PDDocument {

	private COSDocument document;

	//cached values
    private PDDocumentInformation documentInformation;

	/**
	 *
	 * @param inDocument
	 */
	public PDDocument(COSDocument inDocument) {
		document = inDocument;
	}

    /**
     * This will get the low level document.
     *
     * @return The document that this layer sits on top of.
     */
    public COSDocument getDocument() {
        return document;
    }

    /**
     * This will load a document from a file.
     *
     * @param file The name of the file to load.
     * @return The document that was loaded.
     * @throws IOException If there is an error reading from the stream.
     */
	public static PDDocument load(File inFile) throws IOException {
		return load(new FileInputStream(inFile));
	}

	/**
	 * This will load a document from an input stream.
	 *
	 * @param inInputStream FileInputStream
	 * @return PDDocument The document that was loaded.
	 * @throws IOException
	 */
	private static PDDocument load(FileInputStream inInputStream) throws IOException {
		PDFParser lParser = new PDFParser(new BufferedInputStream(inInputStream));
		lParser.parse();
		return lParser.getPDDocument();
	}

    /**
     * This will get the document info dictionary.  This is guaranteed to not return null.
     *
     * @return The documents /Info dictionary
     */
	public PDDocumentInformation getDocumentInformation() {
        if (documentInformation == null) {
            COSDictionary lTrailer = document.getTrailer();
            COSDictionary lInfoDictionary = (COSDictionary)lTrailer.getDictionaryObject(COSName.INFO);
            if (lInfoDictionary == null) {
                lInfoDictionary = new COSDictionary();
                lTrailer.setItem(COSName.INFO, lInfoDictionary);
            }
            documentInformation = new PDDocumentInformation(lInfoDictionary);
        }
        return documentInformation;
	}

	/**
     * This will set the document information for this document.
     *
     * @param inInfo The updated document information.
     */
    public void setDocumentInformation(PDDocumentInformation inInfo) {
        documentInformation = inInfo;
        document.getTrailer().setItem(COSName.INFO, inInfo.getDictionary());
    }

    /**
     * This will tell if this document is encrypted or not.
     *
     * @return true If this document is encrypted.
     */
	public boolean isEncrypted() {
		return document.isEncrypted();
	}

    /**
     * This will close the underlying COSDocument object.
     *
     * @throws IOException If there is an error releasing resources.
     */
	public void close() throws IOException {
        document.close();
	}

}
