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
package org.elbe.relations.biblio.meta.internal.pdf.cos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elbe.relations.biblio.meta.internal.pdf.PDFObjectStreamParser;
import org.elbe.relations.biblio.meta.internal.pdf.PDFXrefStreamParser;
import org.elbe.relations.biblio.meta.internal.pdf.RandomAccess;
import org.elbe.relations.biblio.meta.internal.pdf.RandomAccessFile;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSDocument extends COSBase {

	private File tmpFile = null;
	private RandomAccess scratchFile = null;
	private String headerString = "%PDF-1.4";
	private float version;
	private COSDictionary trailer;

    /**
     * Maps object and generation ids to object byte offsets.
     */
	private Map<COSObjectKey, Integer> xrefTable = new HashMap<COSObjectKey, Integer>();
    /**
     * Maps ObjectKeys to a COSObject. Note that references to these objects
     * are also stored in COSDictionary objects that map a name to a specific object.
     */
	private Map<COSObjectKey, COSObject> objectPool = new HashMap<COSObjectKey, COSObject>();

	/**
	 *
	 * @throws IOException
	 */
	public COSDocument() throws IOException {
		this(new File(System.getProperty("java.io.tmpdir")));
	}

	/**
	 *
	 * @param inScratchDir File
	 * @throws IOException
	 */
	public COSDocument(File inScratchDir) throws IOException {
		tmpFile = File.createTempFile("pdfbox", "tmp", inScratchDir);
		scratchFile = new RandomAccessFile(tmpFile, "rw");
	}

	/**
	 * @param inHeader {@link String} The headerString to set.
	 */
	public void setHeaderString(String inHeader) {
		headerString = inHeader;
	}

    /**
     * @return Returns the headerString.
     */
    public String getHeaderString() {
        return headerString;
    }

	/**
	 * @param inVersion float The version of the PDF document.
	 */
	public void setVersion(float inVersion) {
		version = inVersion;
	}

    /**
     * This will get the version of this PDF document.
     *
     * @return This documents version.
     */
    public float getVersion() {
        return version;
    }

	/**
	 * Used to populate the XRef HashMap. Will add an Xreftable entry
     * that maps ObjectKeys to byte offsets in the file.
	 *
	 * @param inKey COSObjectKey The objkey, with id and gen numbers
	 * @param inOffset int The byte offset in this file
	 */
	public void setXRef(COSObjectKey inKey, int inOffset) {
		xrefTable .put(inKey, new Integer(inOffset));
	}

    /**
     * This will get an object from the pool.
     *
     * @param inKey The object key.
     * @return The object in the pool or a new one if it has not been parsed yet.
     * @throws IOException If there is an error getting the proxy object.
     */
	public COSObject getObjectFromPool(COSObjectKey inKey) {
        COSObject outObject = null;
        if (inKey != null) {
            outObject = (COSObject)objectPool.get(inKey);
        }
        if (outObject == null) {
            // this was a forward reference, make "proxy" object
            outObject = new COSObject(null);
            if (inKey != null) {
                outObject.setObjectNumber(new COSInteger(inKey.getNumber()));
                outObject.setGenerationNumber(new COSInteger(inKey.getGeneration()));
                objectPool.put(inKey, outObject);
            }
        }

		return outObject;
	}

    /**
     * This will get the scratch file for this document.
     *
     * @return The scratch file.
     */
	public RandomAccess getScratchFile() {
		return scratchFile;
	}

    /**
     * Returns the xrefTable which is a mapping of ObjectKeys
     * to byte offsets in the file.
     *
     * @return mapping of ObjectsKeys to byte offsets
     */
	public Map<COSObjectKey, Integer> getXrefTable() {
		return xrefTable;
	}


    /**
     * // MIT added, maybe this should not be supported as trailer is a persistence construct.
     * This will set the document trailer.
     *
     * @param inTrailer the document trailer dictionary
     */
    public void setTrailer(COSDictionary inTrailer) {
        trailer = inTrailer;
    }

    /**
     * This will get the document trailer.
     *
     * @return the document trailer dict
     */
	public COSDictionary getTrailer() {
		return trailer;
	}

    /**
     * This method will search the list of objects for types of XRef and
     * uses the parsed data to populate the trailer information as well as
     * the xref Map.
     *
     * @throws IOException if there is an error parsing the stream
     */
	public void parseXrefStreams() throws IOException {
        COSDictionary lTrailerDict = new COSDictionary();
        Iterator<COSObject> lXrefIterator = getObjectsByType("XRef").iterator();
        while (lXrefIterator.hasNext()) {
            COSObject lXrefStream = lXrefIterator.next();
            COSStream lStream = (COSStream)lXrefStream.getObject();
            lTrailerDict.addAll(lStream);
            PDFXrefStreamParser lParser = new PDFXrefStreamParser(lStream, this);
            lParser.parse();
        }
        setTrailer(lTrailerDict);
	}

    /**
     * This will get all dictionary objects by type.
     *
     * @param type The type of the object.
     * @return This will return an object with the specified type.
     * @throws IOException If there is an error getting the object
     */
	private List<COSObject> getObjectsByType(String inType) {
		return getObjectsByType(COSName.getPDFName(inType));
	}

    /**
     * This will get a dictionary object by type.
     *
     * @param type The type of the object.
     * @return This will return an object with the specified type.
     * @throws IOException If there is an error getting the object
     */
	private List<COSObject> getObjectsByType(COSName type) {
        List<COSObject> outList = new ArrayList<COSObject>();
        Iterator<COSObject> iter = objectPool.values().iterator();
        while (iter.hasNext()) {
            COSObject lObject = iter.next();
            COSBase lRealObject = lObject.getObject();
            if (lRealObject instanceof COSDictionary) {
                try {
                    COSDictionary lDictionary = (COSDictionary)lRealObject;
                    COSName lObjectType = (COSName)lDictionary.getItem(COSName.TYPE);
                    if (lObjectType != null && lObjectType.equals(type)) {
                        outList.add(lObject);
                    }
                }
                catch (ClassCastException e) {
                    //log.warn(e, e);
                }
            }
        }
        return outList;
	}

    /**
     * This will tell if this is an encrypted document.
     *
     * @return true If this document is encrypted.
     */
	public boolean isEncrypted() {
        boolean outEncrypted = false;
        if (trailer != null) {
            outEncrypted = trailer.getDictionaryObject("Encrypt") != null;
        }
        return outEncrypted;
	}

    /**
     * This method will search the list of objects for types of ObjStm.  If it finds
     * them then it will parse out all of the objects from the stream that is contains.
     *
     * @throws IOException If there is an error parsing the stream.
     */
	public void dereferenceObjectStreams() throws IOException {
        Iterator<COSObject> lObjectStreams = getObjectsByType("ObjStm").iterator();
        while (lObjectStreams.hasNext()) {
            COSObject lObjectStream = lObjectStreams.next();
            COSStream lStream = (COSStream)lObjectStream.getObject();
            PDFObjectStreamParser lParser = new PDFObjectStreamParser(lStream, this);
            lParser.parse();
            Iterator<COSObject> lCompressedObjects = lParser.getObjects().iterator();
            while (lCompressedObjects.hasNext()) {
                COSObject lNext = lCompressedObjects.next();
                COSObjectKey lKey = new COSObjectKey(lNext);
                COSObject lObject = getObjectFromPool(lKey);
                lObject.setObject(lNext.getObject());
            }
        }
	}

    /**
     * This will close all storage and delete the tmp files.
     *
     *  @throws IOException If there is an error close resources.
     */
	public void close() throws IOException {
        if (scratchFile != null) {
            scratchFile.close();
            scratchFile = null;
        }
        if (tmpFile != null) {
            tmpFile.delete();
            tmpFile = null;
        }
	}

}
