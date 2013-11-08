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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSBase;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDictionary;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDocument;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSInteger;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSObject;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSObjectKey;

/**
 * This class will handle the parsing of the PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class PDFParser extends BaseParser {
	private static final int SPACE_BYTE = 32;
	private static final String PDF_HEADER = "%PDF-"; //$NON-NLS-1$
	private static final String FDF_HEADER = "%FDF-"; //$NON-NLS-1$
	private boolean forceParsing = false;

	/**
     * A list of duplicate objects found when Parsing the PDF
     * File.
     */
    private List<ConflictObj> conflictList = new ArrayList<ConflictObj>();

	public PDFParser(BufferedInputStream inStream) throws IOException {
		super(inStream);
	}

	public void parse() throws IOException {
		COSDocument lDocument = new COSDocument();
		setDocument(lDocument);
		parseHeader();

        //Some PDF files have garbage between the header and the
        //first object
        skipToNextObj();

        boolean lWasLastParsedObjectEOF = false;
        try {
            while (true) {
                if (pdfSource.isEOF()) {
                    break;
                }
                try {
                    lWasLastParsedObjectEOF = parseObject();
                }
                catch (IOException exc) {
                    if (forceParsing) {
                        /*
                         * Warning is sent to the PDFBox.log and to the Console that
                         * we skipped over an object
                         */
                        //log.warn("Parsing Error, Skipping Object", e);
                        skipToNextObj();
                    }
                    else {
                        throw exc;
                    }
                }
                skipSpaces();
            }
            //Test if we saw a trailer section. If not, look for an XRef Stream (Cross-Reference Stream)
            //to populate the trailer and xref information. For PDF 1.5 and above
            if (document.getTrailer() == null) {
                document.parseXrefStreams();
            }
            if (!document.isEncrypted()) {
                document.dereferenceObjectStreams();
            }
            ConflictObj.resolveConflicts(document, conflictList);
        }
        catch (IOException exc) {
            /*
             * PDF files may have random data after the EOF marker. Ignore errors if
             * last object processed is EOF.
             */
            if (!lWasLastParsedObjectEOF) {
                throw exc;
            }
        }
        catch (Throwable t) {
            //so if the PDF is corrupt then close the document and clear
            //all resources to it
            if (document != null) {
                document.close();
            }
            if (t instanceof IOException ) {
                throw (IOException)t;
            }
            else {
                throw new WrappedIOException(t);
            }
        }
        finally {
            pdfSource.close();
        }
	}

    /**
     * This will parse the next object from the stream and add it to
     * the local state.
     *
     * @return Returns true if the processed object had an endOfFile marker
     * @throws IOException If an IO error occurs.
     */
    private boolean parseObject() throws IOException {
        int lCurrentObjByteOffset = pdfSource.getOffset();
        boolean isEndOfFile = false;
        skipSpaces();
        //peek at the next character to determine the type of object we are parsing
        char lPeekedChar = (char)pdfSource.peek();

        //ignore endobj and endstream sections.
        while (lPeekedChar == 'e') {
            //there are times when there are multiple endobj, so lets
            //just read them and move on.
            readString();
            skipSpaces();
            lPeekedChar = (char)pdfSource.peek();
        }
        if (pdfSource.isEOF()) {
            //"Skipping because of EOF" );
            //end of file we will return a false and call it a day.
        }
        //xref table. Note: The contents of the Xref table are currently ignored
        else if (lPeekedChar == 'x') {
            parseXrefTable();
        }
        // Note: startxref can occur in either a trailer section or by itself
        else if (lPeekedChar == 't' || lPeekedChar == 's') {
            if(lPeekedChar == 't') {
                parseTrailer();
                lPeekedChar = (char)pdfSource.peek();
            }
            if (lPeekedChar == 's') {
            	parseStartXref();
                //verify that EOF exists
                String lEof = readExpectedString("%%EOF"); //$NON-NLS-1$
                if (lEof.indexOf( "%%EOF" )== -1 && !pdfSource.isEOF()) { //$NON-NLS-1$
                    throw new IOException( "expected='%%EOF' actual='" + lEof + "' next=" + readString() + " next=" +readString() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                isEndOfFile = true;
            }
        }
        //we are going to parse an normal object
        else {
            int lNumber = -1;
            int lGeneratedNum = -1;
            String lObjectKey = null;
            boolean lMissingObjectNumber = false;
            try {
                char lPeeked = (char)pdfSource.peek();
                if (lPeeked == '<') {
                    lMissingObjectNumber = true;
                }
                else {
                    lNumber = readInt();
                }
            }
            catch (IOException e) {
                //ok for some reason "GNU Ghostscript 5.10" puts two endobj
                //statements after an object, of course this is nonsense
                //but because we want to support as many PDFs as possible
                //we will simply try again
                lNumber = readInt();
            }
            if (!lMissingObjectNumber) {
                skipSpaces();
                lGeneratedNum = readInt();

                lObjectKey = readString(3);
                //System.out.println( "parseObject() num=" + number +
                //" genNumber=" + genNum + " key='" + objectKey + "'" );
                if (!lObjectKey.equals("obj")) { //$NON-NLS-1$
                	throw new IOException("expected='obj' actual='" + lObjectKey + "' " + pdfSource); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            else {
                lNumber = -1;
                lGeneratedNum = -1;
            }

	        skipSpaces();
	        COSBase lParsedObject = parseDirObject();
	        String lEndObjectKey = readString();

	        if (lEndObjectKey.equals("stream")) { //$NON-NLS-1$
	            pdfSource.unread(lEndObjectKey.getBytes());
	            pdfSource.unread(' ');
	            if (lParsedObject instanceof COSDictionary) {
	                lParsedObject = parseCOSStream((COSDictionary)lParsedObject, getDocument().getScratchFile());
	            }
	            else {
	                // this is not legal
	                // the combination of a dict and the stream/endstream forms a complete stream object
	                throw new IOException("stream not preceded by dictionary"); //$NON-NLS-1$
	            }
	            lEndObjectKey = readString();
	        }

	        COSObjectKey lKey = new COSObjectKey(lNumber, lGeneratedNum);
	        COSObject lPdfObject = document.getObjectFromPool(lKey);
	        if (lPdfObject.getObject() == null) {
	        	lPdfObject.setObject(lParsedObject);
	        }
            /*
             * If the object we returned already has a baseobject, then we have a conflict
             * which we will resolve using information after we parse the xref table.
             */
            else {
                addObjectToConflicts(lCurrentObjByteOffset, lKey, lParsedObject);
            }
	        if (!lEndObjectKey.equals("endobj")) { //$NON-NLS-1$
	        	if (lEndObjectKey.startsWith("endobj")) { //$NON-NLS-1$
                    /*
                     * Some PDF files don't contain a new line after endobj so we
                     * need to make sure that the next object number is getting read separately
                     * and not part of the endobj keyword. Ex. Some files would have "endobj28"
                     * instead of "endobj"
                     */
                    pdfSource.unread(lEndObjectKey.substring(6).getBytes());
	        	}
                else if(!pdfSource.isEOF()) {
                    try {
                        //It is possible that the endobj  is missing, there
                        //are several PDFs out there that do that so skip it and move on.
                        Float.parseFloat(lEndObjectKey);
                        pdfSource.unread(SPACE_BYTE);
                        pdfSource.unread(lEndObjectKey.getBytes());
                    }
                    catch( NumberFormatException e ) {
                        //we will try again in case there was some garbage which
                        //some writers will leave behind.
                        String secondEndObjectKey = readString();
                        if (!secondEndObjectKey.equals( "endobj" ) ) { //$NON-NLS-1$
                            if (isClosing()) {
                                //found a case with 17506.pdf object 41 that was like this
                                //41 0 obj [/Pattern /DeviceGray] ] endobj
                                //notice the second array close, here we are reading it
                                //and ignoring and attempting to continue
                                pdfSource.read();
                            }
                            skipSpaces();
                            String thirdPossibleEndObj = readString();
                            if( !thirdPossibleEndObj.equals( "endobj" ) ) { //$NON-NLS-1$
                                throw new IOException("expected='endobj' firstReadAttempt='" + lEndObjectKey + "' " + //$NON-NLS-1$ //$NON-NLS-2$
                                        "secondReadAttempt='" + secondEndObjectKey + "' " + pdfSource); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
	        }
            skipSpaces();
        }
		return isEndOfFile;
	}

    /**
     * Adds a new ConflictObj to the conflictList.
     *
     * @param inOffset the offset of the ConflictObj
     * @param inKey The COSObjectKey of this object
     * @param inBase The COSBase of this conflictObj
     * @throws IOException
     */
    private void addObjectToConflicts(int inOffset, COSObjectKey inKey, COSBase inBase) {
        COSObject obj = new COSObject(null);
        obj.setObjectNumber( new COSInteger( inKey.getNumber() ) );
        obj.setGenerationNumber( new COSInteger( inKey.getGeneration() ) );
        obj.setObject(inBase);
        ConflictObj conflictObj = new ConflictObj(inOffset, inKey, obj);
        conflictList.add(conflictObj);
	}

	/**
     * This will parse the startxref section from the stream.
     * The startxref value is ignored.
     *
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    private boolean parseStartXref() throws IOException {
    	if (pdfSource.peek() != 's') {
    		return false;
    	}
        String lStartXRef = readString();
        if (!lStartXRef.trim().equals("startxref")) { //$NON-NLS-1$
        	return false;
        }
        skipSpaces();
        /* This integer is the byte offset of the first object referenced by the xref or xref stream
         * Not needed for PDFbox
         */
        readInt();
        return true;
	}

	/**
     * This will parse the trailer from the stream and add it to the state.
     *
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    private boolean parseTrailer() throws IOException {
        if(pdfSource.peek() != 't') {
        	return false;
        }
        //read "trailer"
        String nextLine = readLine();
        if(!nextLine.trim().equals("trailer")) { //$NON-NLS-1$
            // in some cases the EOL is missing and the trailer immediately
            // continues with "<<" or with a blank character
            // even if this does not comply with PDF reference we want to support as many PDFs as possible
            // Acrobat reader can also deal with this.
            if (nextLine.startsWith("trailer")) { //$NON-NLS-1$
                byte[] b = nextLine.getBytes();
                int len = "trailer".length(); //$NON-NLS-1$
                pdfSource.unread('\n');
                pdfSource.unread(b, len, b.length-len);
            }
            else {
                return false;
            }
        }
        // in some cases the EOL is missing and the trailer continues with " <<"
        // even if this does not comply with PDF reference we want to support as many PDFs as possible
        // Acrobat reader can also deal with this.
        skipSpaces();

        COSDictionary parsedTrailer = parseCOSDictionary();
        COSDictionary docTrailer = document.getTrailer();
        if (docTrailer == null) {
        	document.setTrailer(parsedTrailer);
        }
        else {
        	docTrailer.addAll(parsedTrailer);
        }
        skipSpaces();
        return true;
	}


    /**
     * This will parse the xref table from the stream and add it to the state
     * The XrefTable contents are ignored.
     *
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    private boolean parseXrefTable() throws IOException {
        if (pdfSource.peek() != 'x') {
            return false;
        }
        String xref = readString();
        if (!xref.trim().equals("xref")) { //$NON-NLS-1$
            return false;
        }
        /*
         * Xref tables can have multiple sections.
         * Each starts with a starting object id and a count.
         */
        while (true) {
            int currObjID = readInt(); // first obj id
            int count = readInt(); // the number of objects in the xref table
            skipSpaces();
            for (int i = 0; i < count; i++) {
            	if (pdfSource.isEOF() || isEndOfName((char)pdfSource.peek())) {
            		break;
            	}
                if (pdfSource.peek() == 't') {
                    break;
                }
                //Ignore table contents
                String currentLine = readLine();
                String[] splitString = currentLine.split(" "); //$NON-NLS-1$
                if (splitString.length < 3) {
                	//log warning
                    break;
                }
                /* This supports the corrupt table as reported in
                 * PDFBOX-474 (XXXX XXX XX n) */
                if (splitString[splitString.length-1].equals("n")) { //$NON-NLS-1$
                    try {
                        int currOffset = Integer.parseInt(splitString[0]);
                        int currGenID = Integer.parseInt(splitString[1]);
                        COSObjectKey objKey = new COSObjectKey(currObjID, currGenID);
                        document.setXRef(objKey, currOffset);
                    }
                    catch (NumberFormatException exc) {
                        throw new IOException(exc.getMessage());
                    }
                }
                else if (!splitString[2].equals("f")) { //$NON-NLS-1$
                    throw new IOException("Corrupt XRefTable Entry - ObjID:" + currObjID); //$NON-NLS-1$
                }
                currObjID++;
                skipSpaces();
            }
            skipSpaces();
            char c = (char)pdfSource.peek();
            if (c < '0' || c > '9') {
                break;
            }
        }
        return true;
	}

    /**
     * Skip to the start of the next object.  This is used to recover
     * from a corrupt object. This should handle all cases that parseObject
     * supports. This assumes that the next object will
     * start on its own line.
     *
     * @throws IOException
     */
	private void skipToNextObj() throws IOException {
        byte[] b = new byte[16];
        Pattern p = Pattern.compile("\\d+\\s+\\d+\\s+obj.*", Pattern.DOTALL); //$NON-NLS-1$
        /* Read a buffer of data each time to see if it starts with a
         * known keyword. This is not the most efficient design, but we should
         * rarely be needing this function. We could update this to use the
         * circular buffer, like in readUntilEndStream().
         */
        while(!pdfSource.isEOF()) {
            int l = pdfSource.read(b);
            if (l < 1) {
            	break;
            }
            String s = new String(b, "US-ASCII"); //$NON-NLS-1$
            if(s.startsWith("trailer") || //$NON-NLS-1$
                    s.startsWith("xref") || //$NON-NLS-1$
                    s.startsWith("startxref") || //$NON-NLS-1$
                    s.startsWith("stream") || //$NON-NLS-1$
                    p.matcher(s).matches()) {
                pdfSource.unread(b);
                break;
            }
            else {
                pdfSource.unread(b, 1, l-1);
            }
        }
	}

	private void parseHeader() throws IOException {
		// read first line
		String lHeader = readLine();
		// some pdf-documents are broken and the pdf-version is in one of the following lines
		if ((lHeader.indexOf(PDF_HEADER) == -1) && (lHeader.indexOf(FDF_HEADER) == -1)) {
			lHeader = readLine();
			while ((lHeader.indexOf(PDF_HEADER) == -1) && (lHeader.indexOf(FDF_HEADER) == -1)) {
				// if a line starts with a digit, it has to be the first one with data in it
				if ((lHeader.length() > 0) && (Character.isDigit(lHeader.charAt(0)))) {
					 break;
				}
				lHeader = readLine();
			}
		}

		// nothing found
		if ((lHeader.indexOf( PDF_HEADER ) == -1) && (lHeader.indexOf( FDF_HEADER ) == -1)) {
			throw new IOException("Error: Header doesn't contain versioninfo"); //$NON-NLS-1$
		}

        //sometimes there are some garbage bytes in the header before the header
        //actually starts, so lets try to find the header first.
        int lHeaderStart = lHeader.indexOf(PDF_HEADER);
        if (lHeaderStart == -1) {
        	lHeaderStart = lHeader.indexOf(FDF_HEADER);
        }
        //greater than zero because if it is zero then
        //there is no point of trimming
        if (lHeaderStart > 0) {
            //trim off any leading characters
            lHeader = lHeader.substring(lHeaderStart, lHeader.length());
        }        /*
         * This is used if there is garbage after the header on the same line
         */
        if (lHeader.startsWith(PDF_HEADER)) {
            if(!lHeader.matches(PDF_HEADER + "\\d.\\d")) { //$NON-NLS-1$
                String lHeaderGarbage = lHeader.substring(PDF_HEADER.length()+3, lHeader.length()) + "\n"; //$NON-NLS-1$
                lHeader = lHeader.substring(0, PDF_HEADER.length()+3);
                pdfSource.unread(lHeaderGarbage.getBytes());
            }
        }
        else {
            if(!lHeader.matches(FDF_HEADER + "\\d.\\d")) { //$NON-NLS-1$
                String lHeaderGarbage = lHeader.substring(FDF_HEADER.length()+3, lHeader.length()) + "\n"; //$NON-NLS-1$
                lHeader = lHeader.substring(0, FDF_HEADER.length()+3);
                pdfSource.unread(lHeaderGarbage.getBytes());
            }
        }
        document.setHeaderString(lHeader);

        try {
        	if (lHeader.startsWith(PDF_HEADER)) {
                float lPdfVersion = Float.parseFloat(lHeader.substring(PDF_HEADER.length(), Math.min(lHeader.length(), PDF_HEADER.length()+3)));
                document.setVersion(lPdfVersion);
        	}
        	else {
                float lFdfVersion = Float.parseFloat(lHeader.substring(FDF_HEADER.length(), Math.min(lHeader.length(), FDF_HEADER.length()+3)));
                document.setVersion(lFdfVersion);
        	}
        }
        catch (NumberFormatException exc)        {
            throw new IOException("Error getting pdf version: " + exc); //$NON-NLS-1$
        }
	}

    /**
     * This will get the document that was parsed.  parse() must be called before this is called.
     * When you are done with this document you must call close() on it to release
     * resources.
     *
     * @return The document that was parsed.
     * @throws IOException If there is an error getting the document.
     */
    public COSDocument getDocument() throws IOException {
        if (document == null) {
            throw new IOException("You must call parse() before calling getDocument()"); //$NON-NLS-1$
        }
        return document;
    }


// --- private classes ---

    /**
     * Used to resolve conflicts when a PDF Document has multiple objects with
     * the same id number. Ideally, we could use the Xref table when parsing
     * the document to be able to determine which of the objects with the same ID
     * is correct, but we do not have access to the Xref Table during parsing.
     * Instead, we queue up the conflicts and resolve them after the Xref has
     * been parsed. The Objects listed in the Xref Table are kept and the
     * others are ignored.
     */
    private static class ConflictObj {
        private int offset;
        private COSObjectKey objectKey;
        private COSObject object;

        public ConflictObj(int inOffset, COSObjectKey inKey, COSObject inPdfObject) {
        	offset = inOffset;
        	objectKey = inKey;
        	object = inPdfObject;
        }

        public String toString() {
            return "Object(" + offset + ", " + objectKey + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /**
         * Sometimes pdf files have objects with the same ID number yet are
         * not referenced by the Xref table and therefore should be excluded.
         * This method goes through the conflicts list and replaces the object stored
         * in the objects array with this one if it is referenced by the xref
         * table.
         * @throws IOException
         */
        private static void resolveConflicts(COSDocument inDocument, List<ConflictObj> inConflictList) throws IOException {
            Iterator<ConflictObj> lConflicts = inConflictList.iterator();
            while (lConflicts.hasNext()) {
                ConflictObj lConflict = lConflicts.next();
                Integer offset = new Integer(lConflict.offset);
                if (inDocument.getXrefTable().containsValue(offset)) {
                    COSObject lPdfObject = inDocument.getObjectFromPool(lConflict.objectKey);
                    lPdfObject.setObject(lConflict.object.getObject());
                }
            }
        }
    }

    /**
     * This will get the PD document that was parsed.  When you are done with
     * this document you must call close() on it to release resources.
     *
     * @return The document at the PD layer.
     * @throws IOException If there is an error getting the document.
     */
	public PDDocument getPDDocument() throws IOException {
		return new PDDocument(getDocument());
	}

}
