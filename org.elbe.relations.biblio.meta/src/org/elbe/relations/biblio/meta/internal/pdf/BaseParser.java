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
import java.io.InputStream;
import java.io.OutputStream;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSArray;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSBase;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSBoolean;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDictionary;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDocument;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSInteger;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSName;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSNull;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSNumber;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSObject;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSObjectKey;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSStream;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSString;


/**
 * This class is used to contain parsing logic that will be used by both the
 * PDFParser and the COSStreamParser.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class BaseParser {

	private static final int E = 'e';
    private static final int N = 'n';
    private static final int D = 'd';

    private static final int S = 's';
    private static final int T = 't';
    private static final int R = 'r';
    //private static final int E = 'e';
    private static final int A = 'a';
    private static final int M = 'm';

    private static final int O = 'o';
    private static final int B = 'b';
    private static final int J = 'j';

    /**
     * This is a byte array that will be used for comparisons.
     */
    public static final byte[] ENDSTREAM = new byte[] { E, N, D, S, T, R, E, A, M };

    /**
     * This is a byte array that will be used for comparisons.
     */
    public static final byte[] ENDOBJ = new byte[] { E, N, D, O, B, J };

	/**
     * This is a byte array that will be used for comparisons.
     */
    public static final String DEF = "def"; //$NON-NLS-1$

	protected PushBackInputStream pdfSource;
	protected COSDocument document;

	/**
	 * Constructor
	 *
	 * @param inInput InputStream
	 * @throws IOException
	 */
	public BaseParser(InputStream inInput) throws IOException {
        pdfSource = new PushBackInputStream(new BufferedInputStream(inInput, 16384), 4096);
	}

	protected void setDocument(COSDocument inDocument) {
		document = inDocument;
	}

	protected String readLine() throws IOException {
		if (pdfSource.isEOF()) {
			throw new IOException("Error: End-of-File, expected line"); //$NON-NLS-1$
		}
		StringBuilder lBuffer = new StringBuilder();
		int c;
		while ((c = pdfSource.read()) != -1) {
			if (isEOL(c)) {
				break;
			}
			lBuffer.append((char)c);
		}
		return new String(lBuffer);
	}

	protected boolean isEOL(int c) {
		return c == 10 || c == 13;
	}

	/**
	 * This will skip all spaces and comments that are present.
	 *
	 * @throws IOException If there is an error reading from the stream.
	 */
	protected void skipSpaces() throws IOException {
        int c = pdfSource.read();
        // identical to, but faster as: isWhiteSpace(c) || c == 37
        while (c == 0 || c == 9 || c == 12  || c == 10
                || c == 13 || c == 32 || c == 37)//37 is the % character, a comment
        {
            if (c == 37) {
                // skip past the comment section
                c = pdfSource.read();
                while (!isEOL(c) && c != -1) {
                    c = pdfSource.read();
                }
            }
            else{
                c = pdfSource.read();
            }
        }
        if (c != -1) {
            pdfSource.unread(c);
        }

	}

    /**
     * This will read the next string from the stream.
     *
     * @return The string that was read from the stream.
     * @throws IOException If there is an error reading from the stream.
     */
	protected String readString() throws IOException {
        skipSpaces();
        StringBuilder buffer = new StringBuilder();
        int c = pdfSource.read();
        while(!isEndOfName((char)c) && !isClosing(c) && c != -1) {
            buffer.append( (char)c );
            c = pdfSource.read();
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
        return buffer.toString();
	}

	/**
	 * This will read the next string from the stream up to a certain length.
	 *
	 * @param length The length to stop reading at.
	 * @return The string that was read from the stream of length 0 to length.
	 * @throws IOException If there is an error reading from the stream.
	 */
	protected String readString(int length) throws IOException {
        skipSpaces();

        int c = pdfSource.read();

        //average string size is around 2 and the normal string buffer size is
        //about 16 so lets save some space.
        StringBuilder lBuffer = new StringBuilder(length);
        while (!isWhitespace(c) && !isClosing(c) && c != -1 && lBuffer.length() < length &&
                c != '[' &&
                c != '<' &&
                c != '(' &&
                c != '/' ) {
            lBuffer.append((char)c);
            c = pdfSource.read();
        }
        if (c != -1)  {
            pdfSource.unread(c);
        }
        return new String(lBuffer);
	}

    /**
     * Determine if a character terminates a PDF name.
     *
     * @param ch The character
     * @return <code>true</code> if the character terminates a PDF name, otherwise <code>false</code>.
     */
	protected boolean isEndOfName(char ch) {
        return (ch == ' ' || ch == 13 || ch == 10 || ch == 9 || ch == '>' || ch == '<'
            || ch == '[' || ch =='/' || ch ==']' || ch ==')' || ch =='(' ||
            ch == -1 //EOF
        );
	}

	/**
	 * This will read an integer from the stream.
	 *
	 * @return The integer that was read from the stream.
	 * @throws IOException If there is an error reading from the stream.
	 */
	protected int readInt() throws IOException {
        skipSpaces();
        int outValue = 0;

        int lLastByte = 0;
        StringBuffer lIntBuffer = new StringBuffer();
        while ((lLastByte = pdfSource.read()) != 32 &&
                lLastByte != 10 &&
                lLastByte != 13 &&
                lLastByte != 60 && //see sourceforge bug 1714707
                lLastByte != 0 && //See sourceforge bug 853328
                lLastByte != -1 ) {
            lIntBuffer.append((char)lLastByte);
        }
        if (lLastByte != -1) {
            pdfSource.unread(lLastByte);
        }

        try {
            outValue = Integer.parseInt(lIntBuffer.toString());
        }
        catch( NumberFormatException exc) {
            pdfSource.unread(lIntBuffer.toString().getBytes());
            throw new IOException("Error: Expected an integer type, actual='" + lIntBuffer + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return outValue;
	}

	/**
	 * This will parse a PDF dictionary.
	 *
	 * @return The parsed dictionary.
	 * @throws IOException IF there is an error reading the stream.
	 */
	protected COSDictionary parseCOSDictionary() throws IOException {
		char c = (char)pdfSource.read();
        if (c != '<') {
            throw new IOException("expected='<' actual='" + c + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        c = (char)pdfSource.read();
        if (c != '<') {
            throw new IOException( "expected='<' actual='" + c + "' " + pdfSource ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        skipSpaces();

        COSDictionary outObject = new COSDictionary();
        boolean lDone = false;
        while (!lDone) {
            skipSpaces();
            c = (char)pdfSource.peek();
            if (c == '>') {
                lDone = true;
            }
            else if (c != '/') {
                //an invalid dictionary, we are expecting
                //the key, read until we can recover
            	//log.warn("Invalid dictionary, found:" + (char)c + " but expected:\''");
                int lRead = pdfSource.read();
                while (lRead != -1 && lRead != '/' && lRead != '>') {
                    lRead = pdfSource.read();
                }
                if (lRead != -1) {
                    pdfSource.unread(lRead);
                }
                else {
                    return outObject;
                }
            }
            else {
                COSName lKey = parseCOSName();
                COSBase lValue = parseCOSDictionaryValue();
                skipSpaces();
                if (((char)pdfSource.peek()) == 'd') {
                    //if the next string is 'def' then we are parsing a cmap stream
                    //and want to ignore it, otherwise throw an exception.
                    String potentialDEF = readString();
                    if (!potentialDEF.equals(DEF)) {
                        pdfSource.unread(potentialDEF.getBytes());
                    }
                    else {
                        skipSpaces();
                    }
                }
                if (lValue == null) {
                    //log.warn("Bad Dictionary Declaration " + pdfSource );
                }
                else {
                    outObject.setItem(lKey, lValue);
                }
            }
        }
        char ch = (char)pdfSource.read();
        if (ch != '>') {
            throw new IOException( "expected='>' actual='" + ch + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ch = (char)pdfSource.read();
        if (ch != '>') {
            throw new IOException( "expected='>' actual='" + ch + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return outObject;
	}

    /**
     * This will parse a PDF dictionary value.
     *
     * @return The parsed Dictionary object.
     * @throws IOException If there is an error parsing the dictionary object.
     */
    private COSBase parseCOSDictionaryValue() throws IOException {
        COSBase outValue = null;
        COSBase lNumber = parseDirObject();
        skipSpaces();
        char lNext = (char)pdfSource.peek();
        if (lNext >= '0' && lNext <= '9') {
            COSBase lGenerationNumber = parseDirObject();
            skipSpaces();
            char r = (char)pdfSource.read();
            if (r != 'R') {
                throw new IOException( "expected='R' actual='" + r + "' " + pdfSource ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            COSObjectKey lKey = new COSObjectKey(((COSInteger)lNumber).intValue(), ((COSInteger)lGenerationNumber).intValue());
            outValue = document.getObjectFromPool(lKey);
        }
        else {
            outValue = lNumber;
        }
        return outValue;
	}

    /**
     * This will parse a directory object from the stream.
     *
     * @return The parsed object.
     * @throws IOException If there is an error during parsing.
     */
	protected COSBase parseDirObject() throws IOException {
        COSBase outValue = null;

        skipSpaces();
        int lNextByte = pdfSource.peek();
        char c = (char)lNextByte;
        switch (c) {
        case '<': {
            int lLeftBracket = pdfSource.read();//pull off first left bracket
            c = (char)pdfSource.peek(); //check for second left bracket
            pdfSource.unread(lLeftBracket);
            if (c == '<') {
                outValue = parseCOSDictionary();
                skipSpaces();
            }
            else {
                outValue = parseCOSString();
            }
            break;
        }
        case '[': { // array
            outValue = parseCOSArray();
            break;
        }
        case '(':
            outValue = parseCOSString();
            break;
        case '/':   // name
            outValue = parseCOSName();
            break;
        case 'n': { // null
            String nullString = readString();
            if (!nullString.equals("null")) { //$NON-NLS-1$
                throw new IOException("Expected='null' actual='" + nullString + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            outValue = COSNull.NULL;
            break;
        }
        case 't': {
            String trueString = new String(pdfSource.readFully(4));
            if (trueString.equals("true")) { //$NON-NLS-1$
                outValue = COSBoolean.TRUE;
            }
            else {
                throw new IOException("expected true actual='" + trueString + "' " + pdfSource); //$NON-NLS-1$ //$NON-NLS-2$
            }
            break;
        }
        case 'f': {
            String falseString = new String(pdfSource.readFully(5));
            if (falseString.equals("false")) { //$NON-NLS-1$
                outValue = COSBoolean.FALSE;
            }
            else {
                throw new IOException( "expected false actual='" + falseString + "' " + pdfSource ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            break;
        }
        case 'R':
            pdfSource.read();
            outValue = new COSObject(null);
            break;
        case (char)-1:
            return null;
        default: {
            if (Character.isDigit(c) || c == '-' || c == '+' || c == '.') {
            	StringBuilder lBuffer = new StringBuilder();
                int ic = pdfSource.read();
                c = (char)ic;
                while (Character.isDigit( c )||
                        c == '-' ||
                        c == '+' ||
                        c == '.' ||
                        c == 'E' ||
                        c == 'e' ) {
                    lBuffer.append( c );
                    ic = pdfSource.read();
                    c = (char)ic;
                }
                if (ic != -1) {
                    pdfSource.unread(ic);
                }
                outValue = COSNumber.get(new String(lBuffer));
            }
            else {
                //This is not suppose to happen, but we will allow for it
                //so we are more compatible with POS writers that don't
                //follow the spec
                String lBadString = readString();
                //throw new IOException( "Unknown dir object c='" + c +
                //"' peek='" + (char)pdfSource.peek() + "' " + pdfSource );
                if (lBadString == null || lBadString.length() == 0) {
                    int peek = pdfSource.peek();
                    // we can end up in an infinite loop otherwise
                    throw new IOException( "Unknown dir object c='" + c + "' cInt=" + (int)c + " peek='" + (char)peek + "' peekInt=" + peek + " " + pdfSource ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                }
            }
        }
        }
        return outValue;
	}

    /**
     * This will parse a PDF array object.
     *
     * @return The parsed PDF array.
     * @throws IOException If there is an error parsing the stream.
     */
    protected COSBase parseCOSArray() throws IOException {
        char ch = (char)pdfSource.read();
        if (ch != '[') {
            throw new IOException( "expected='[' actual='" + ch + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        COSArray outValue = new COSArray();
        COSBase lParsedObject = null;
        skipSpaces();
        int i = 0;
        while (((i = pdfSource.peek()) > 0) && ((char)i != ']')) {
            lParsedObject = parseDirObject();
            if (lParsedObject instanceof COSObject) {
                // We have to check if the expected values are there or not PDFBOX-385
                if (outValue.get(outValue.size()-1) instanceof COSInteger) {
                    COSInteger genNumber = (COSInteger)outValue.remove(outValue.size() -1);
                    if (outValue.get(outValue.size()-1) instanceof COSInteger) {
                        COSInteger number = (COSInteger)outValue.remove(outValue.size() -1);
                        COSObjectKey key = new COSObjectKey(number.intValue(), genNumber.intValue());
                        lParsedObject = document.getObjectFromPool(key);
                    }
                    else {
                        // the object reference is somehow wrong
                        lParsedObject = null;
                    }
                }
                else {
                    lParsedObject = null;
                }
            }
            if (lParsedObject != null) {
                outValue.add(lParsedObject);
            }
            else {
            	//log.warn("Corrupt object reference" );
                //it could be a bad object in the array which is just skipped
            }
            skipSpaces();
        }
        pdfSource.read(); //read ']'
        skipSpaces();
        return outValue;
	}

	/**
     * This will parse a PDF string.
     *
     * @return The parsed PDF string.
     * @throws IOException If there is an error reading from the stream.
     */
	private COSBase parseCOSString() throws IOException {
        char nextChar = (char)pdfSource.read();
        COSString outValue = new COSString();
        char openBrace;
        char closeBrace;
        if (nextChar == '(') {
            openBrace = '(';
            closeBrace = ')';
        }
        else if (nextChar == '<') {
            openBrace = '<';
            closeBrace = '>';
        }
        else {
            throw new IOException( "parseCOSString string should start with '(' or '<' and not '" + nextChar + "' " + pdfSource ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        //This is the number of braces read
        int braces = 1;
        int c = pdfSource.read();
        while (braces > 0 && c != -1) {
            char ch = (char)c;
            int nextc = -2; // not yet read
            if (ch == closeBrace) {
                braces--;
                byte[] nextThreeBytes = new byte[3];
                int amountRead = pdfSource.read(nextThreeBytes);

                //lets handle the special case seen in Bull  River Rules and Regulations.pdf
                //The dictionary looks like this
                //    2 0 obj
                //    <<
                //        /Type /Info
                //        /Creator (PaperPort http://www.scansoft.com)
                //        /Producer (sspdflib 1.0 http://www.scansoft.com)
                //        /Title ( (5)
                //        /Author ()
                //        /Subject ()
                //
                // Notice the /Title, the braces are not even but they should
                // be.  So lets assume that if we encounter an this scenario
                //   <end_brace><new_line><opening_slash> then that
                // means that there is an error in the pdf and assume that
                // was the end of the document.
                if (amountRead == 3) {
                    if (nextThreeBytes[0] == 0x0d &&
                            nextThreeBytes[1] == 0x0a &&
                            nextThreeBytes[2] == 0x2f) {
                        braces = 0;
                    }
                }
                if (amountRead > 0) {
                    pdfSource.unread(nextThreeBytes, 0, amountRead);
                }
                if (braces != 0) {
                    outValue.append(ch);
                }
            }
            else if(ch == openBrace) {
                braces++;
                outValue.append( ch );
            }
            else if(ch == '\\') {
                //patched by ram
                char next = (char)pdfSource.read();
                switch(next) {
                case 'n':
                    outValue.append( '\n' );
                    break;
                case 'r':
                    outValue.append( '\r' );
                    break;
                case 't':
                    outValue.append( '\t' );
                    break;
                case 'b':
                    outValue.append( '\b' );
                    break;
                case 'f':
                    outValue.append( '\f' );
                    break;
                case '(':
                case ')':
                case '\\':
                    outValue.append( next );
                    break;
                case 10:
                case 13:
                    //this is a break in the line so ignore it and the newline and continue
                    c = pdfSource.read();
                    while (isEOL(c) && c != -1) {
                        c = pdfSource.read();
                    }
                    nextc = c;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7': {
                    StringBuilder octal = new StringBuilder();
                    octal.append(next);
                    c = pdfSource.read();
                    char digit = (char)c;
                    if( digit >= '0' && digit <= '7' ) {
                        octal.append( digit );
                        c = pdfSource.read();
                        digit = (char)c;
                        if (digit >= '0' && digit <= '7') {
                            octal.append( digit );
                        }
                        else {
                            nextc = c;
                        }
                    }
                    else {
                        nextc = c;
                    }

                    int character = 0;
                    try {
                        character = Integer.parseInt( octal.toString(), 8 );
                    }
                    catch (NumberFormatException exc) {
                        throw new IOException( "Error: Expected octal character, actual='" + octal + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    outValue.append(character);
                    break;
                }
                default: {
                    outValue.append( '\\' );
                    outValue.append( next );
                    //another ficken problem with PDF's, sometimes the \ doesn't really
                    //mean escape like the PDF spec says it does, sometimes is should be literal
                    //which is what we will assume here.
                    //throw new IOException( "Unexpected break sequence '" + next + "' " + pdfSource );
                }
                }
            }
            else {
                if (openBrace == '<') {
                    if (isHexDigit(ch)) {
                        outValue.append(ch);
                    }
                }
                else {
                    outValue.append(ch);
                }
            }
            if (nextc != -2) {
                c = nextc;
            }
            else {
                c = pdfSource.read();
            }
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
        if (openBrace == '<') {
            outValue = COSString.createFromHexString(outValue.getString());
        }
		return outValue;
	}

	/**
     * This will parse a PDF name from the stream.
     *
     * @return The parsed PDF name.
     * @throws IOException If there is an error reading from the stream.
     */
	private COSName parseCOSName() throws IOException {
        int c = pdfSource.read();
        if ((char)c != '/') {
            throw new IOException("expected='/' actual='" + (char)c + "'-" + c + " " + pdfSource ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        StringBuilder lBuffer = new StringBuilder();
        c = pdfSource.read();
        while (c != -1) {
            char ch = (char)c;
            if (ch == '#') {
                char ch1 = (char)pdfSource.read();
                char ch2 = (char)pdfSource.read();

                // Prior to PDF v1.2, the # was not a special character.  Also,
                // it has been observed that various PDF tools do not follow the
                // spec with respect to the # escape, even though they report
                // PDF versions of 1.2 or later.  The solution here is that we
                // interpret the # as an escape only when it is followed by two
                // valid hex digits.
                //
                if (isHexDigit(ch1) && isHexDigit(ch2)) {
                	String hex = "" + ch1 + ch2; //$NON-NLS-1$
                	try {
                		lBuffer.append( (char) Integer.parseInt(hex, 16));
                	}
                    catch (NumberFormatException exc) {
                        throw new IOException("Error: expected hex number, actual='" + hex + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    c = pdfSource.read();
                }
                else {
                    pdfSource.unread(ch2);
                    c = ch1;
                    lBuffer.append( ch );
                }
            }
            else if (isEndOfName(ch)) {
                break;
            }
            else {
                lBuffer.append(ch);
                c = pdfSource.read();
            }
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
        return COSName.getPDFName(new String(lBuffer));
	}

    /**
	 * This will read bytes until the end of line marker occurs.
	 *
	 * @param inString The next expected string in the stream.
	 * @return The characters between the current position and the end of the line.
	 * @throws IOException If there is an error reading from the stream or theString does not match what was read.
	 */
	protected String readExpectedString(String inString) throws IOException {
        int c = pdfSource.read();
        while (isWhitespace(c) && c != -1) {
            c = pdfSource.read();
        }
        StringBuilder lBuffer = new StringBuilder(inString.length());
        int lCharsRead = 0;
        while (!isEOL(c) && c != -1 && lCharsRead < inString.length()) {
            char lNext = (char)c;
            lBuffer.append( lNext );
            if (inString.charAt( lCharsRead ) == lNext) {
                lCharsRead++;
            }
            else {
                pdfSource.unread(lBuffer.toString().getBytes());
                throw new IOException( "Error: Expected to read '" + inString + "' instead started reading '" + lBuffer.toString() + "'" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            c = pdfSource.read();
        }
        while (isEOL(c) && c != -1) {
            c = pdfSource.read();
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
		return new String(lBuffer);
	}

    /**
     * This will tell if the next byte is whitespace or not.
     *
     * @param c The character to check against whitespace
     * @return true if the next byte in the stream is a whitespace character.
     */
	protected boolean isWhitespace(int c) {
        return c == 0 || c == 9 || c == 12  || c == 10 || c == 13 || c == 32;
	}

	/**
	 * This will read a COSStream from the input stream.
	 *
	 * @param inFile The file to write the stream to when reading.
	 * @param inDictionary The dictionary that goes with this stream.
	 * @return The parsed pdf stream.
	 * @throws IOException If there is an error reading the stream.
	 */
	protected COSBase parseCOSStream(COSDictionary inDictionary, RandomAccess inFile) throws IOException {
        COSStream outStream = new COSStream(inDictionary, inFile);
        OutputStream lOutput = null;
        try {
        	String lStreamString = readString();
            if (!lStreamString.equals("stream")) { //$NON-NLS-1$
                throw new IOException("expected='stream' actual='" + lStreamString + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            //PDF Ref 3.2.7 A stream must be followed by either
            //a CRLF or LF but nothing else.
            int lWhitespace = pdfSource.read();

            //see brother_scan_cover.pdf, it adds whitespaces
            //after the stream but before the start of the
            //data, so just read those first
            while (lWhitespace == 0x20) {
            	lWhitespace = pdfSource.read();
            }
            if (lWhitespace == 0x0D) {
                lWhitespace = pdfSource.read();
                if (lWhitespace != 0x0A) {
                    pdfSource.unread( lWhitespace );
                    //The spec says this is invalid but it happens in the real
                    //world so we must support it.
                }
            }
            else if (lWhitespace == 0x0A) {
                //that is fine
            }
            else {
                //we are in an error.
                //but again we will do a lenient parsing and just assume that everything
                //is fine
                pdfSource.unread(lWhitespace);
            }

            /*This needs to be dic.getItem because when we are parsing, the underlying object
             * might still be null.
             */
            COSBase lStreamLength = inDictionary.getItem(COSName.LENGTH);

            //Need to keep track of the
            lOutput = outStream.createFilteredStream(lStreamLength);

            String lEndStream = null;
            readUntilEndStream(lOutput);
            skipSpaces();
            lEndStream = readString();

            if (!lEndStream.equals("endstream")) { //$NON-NLS-1$
                /*
                 * Sometimes stream objects don't have an endstream tag so readUntilEndStream(out)
                 * also can stop on endobj tags. If that's the case we need to make sure to unread
                 * the endobj so parseObject() can handle that case normally.
                 */
                if (lEndStream.startsWith("endobj")) { //$NON-NLS-1$
                    byte[] lEndobjarray = lEndStream.getBytes();
                    pdfSource.unread(lEndobjarray);
                }
                /*
                 * Some PDF files don't contain a new line after endstream so we
                 * need to make sure that the next object number is getting read separately
                 * and not part of the endstream keyword. Ex. Some files would have "endstream8"
                 * instead of "endstream"
                 */
                else if(lEndStream.startsWith("endstream")) { //$NON-NLS-1$
                    String lExtra = lEndStream.substring(9, lEndStream.length());
                    lEndStream = lEndStream.substring(0, 9);
                    byte[] lArray = lExtra.getBytes();
                    pdfSource.unread(lArray);
                }
                else {
                    /*
                     * If for some reason we get something else here, Read until we find the next
                     * "endstream"
                     */
                    readUntilEndStream(lOutput);
                    lEndStream = readString();
                    if (!lEndStream.equals("endstream")) { //$NON-NLS-1$
                        throw new IOException("expected='endstream' actual='" + lEndStream + "' " + pdfSource); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
        finally {
            if (lOutput != null) {
            	lOutput.close();
            }
        }
        return outStream;
	}

	private void readUntilEndStream(OutputStream out) throws IOException {
        int byteRead;
        do { //use a fail fast test for end of stream markers
            byteRead = pdfSource.read();
            if(byteRead==E){//only branch if "e"
                byteRead = pdfSource.read();
                if(byteRead==N){ //only continue branch if "en"
                    byteRead = pdfSource.read();
                    if(byteRead==D){//up to "end" now
                        byteRead = pdfSource.read();
                        if(byteRead==S){
                            byteRead = pdfSource.read();
                            if(byteRead==T){
                                byteRead = pdfSource.read();
                                if(byteRead==R){
                                    byteRead = pdfSource.read();
                                    if(byteRead==E){
                                        byteRead = pdfSource.read();
                                        if(byteRead==A){
                                            byteRead = pdfSource.read();
                                            if(byteRead==M){
                                                //found the whole marker
                                                pdfSource.unread( ENDSTREAM );
                                                return;
                                            }
                                        }else{
                                            out.write(ENDSTREAM, 0, 7);
                                        }
                                    }else{
                                        out.write(ENDSTREAM, 0, 6);
                                    }
                                }else{
                                    out.write(ENDSTREAM, 0, 5);
                                }
                            }else{
                                out.write(ENDSTREAM, 0, 4);
                            }
                        }else if(byteRead==O){
                            byteRead = pdfSource.read();
                            if(byteRead==B){
                                byteRead = pdfSource.read();
                                if(byteRead==J){
                                    //found whole marker
                                    pdfSource.unread(ENDOBJ);
                                    return;
                                }
                            }else{
                                out.write(ENDOBJ, 0, 4);
                            }
                        }else{
                            out.write(E);
                            out.write(N);
                            out.write(D);
                        }
                    }else{
                        out.write(E);
                        out.write(N);
                    }
                }else{
                    out.write(E);
                }
            }
            if(byteRead!=-1)out.write(byteRead);

        } while(byteRead!=-1);
	}

	/**
	 * This will tell if the next character is a closing brace( close of PDF array ).
	 *
	 * @return true if the next byte is ']', false otherwise.
	 * @throws IOException If an IO error occurs.
	 */
	protected boolean isClosing() throws IOException {
		return isClosing(pdfSource.peek());
	}

    /**
     * This will tell if the next character is a closing brace( close of PDF array ).
     *
     * @param c The character to check against end of line
     * @return true if the next byte is ']', false otherwise.
     */
    protected boolean isClosing(int c) {
        return c == ']';
    }

	private static boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9') ||
        (ch >= 'a' && ch <= 'f') ||
        (ch >= 'A' && ch <= 'F');
        // the line below can lead to problems with certain versions of the IBM JIT compiler
        // (and is slower anyway)
        //return (HEXDIGITS.indexOf(ch) != -1);
    }

}
