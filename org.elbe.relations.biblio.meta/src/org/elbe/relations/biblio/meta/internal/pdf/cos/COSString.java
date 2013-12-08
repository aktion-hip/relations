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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This represents a string object in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSString extends COSBase {

    private ByteArrayOutputStream out;
    private String str = null;

	/**
     * Constructor.
     */
    public COSString() {
        out = new ByteArrayOutputStream();
    }

    /**
     * Explicit constructor for ease of manual PDF construction.
     *
     * @param inValue The string value of the object.
     */
    public COSString(String inValue) {
        try {
            boolean lUnicode16 = false;
            char[] lChars = inValue.toCharArray();
            for (int i=0; i<lChars.length; i++) {
                if (lChars[i] > 255 ) {
                    lUnicode16 = true;
                    break;
                }
            }
            if (lUnicode16) {
                byte[] lData = inValue.getBytes("UTF-16BE"); //$NON-NLS-1$
                out = new ByteArrayOutputStream(lData.length +2);
                out.write(0xFE);
                out.write(0xFF);
                out.write(lData);
            }
            else {
                byte[] lData = inValue.getBytes("ISO-8859-1"); //$NON-NLS-1$
                out = new ByteArrayOutputStream( lData.length );
                out.write( lData );
            }
        }
        catch (IOException exc) {
            exc.printStackTrace();
            //should never happen
        }
	}

	/**
     * This will append a byte[] to the string.
     *
     * @param data The byte[] to add to this string.
     *
     * @throws IOException If an IO error occurs while writing the byte.
     */
    public void append(byte[] data) throws IOException {
        out.write( data );
        this.str = null;
    }

    /**
     * This will append a byte to the string.
     *
     * @param in The byte to add to this string.
     *
     * @throws IOException If an IO error occurs while writing the byte.
     */
    public void append(int in) throws IOException {
        out.write(in);
        this.str = null;
    }

    /**
     * This will get the bytes of the string.
     *
     * @return A byte array that represents the string.
     */
    public byte[] getBytes() {
        return out.toByteArray();
    }

    /**
     * This will get the string that this object wraps.
     *
     * @return The wrapped string.
     */
    public String getString() {
        if (this.str != null) {
            return this.str;
        }
        String outValue;
        String lEncoding = "ISO-8859-1"; //$NON-NLS-1$
        byte[] lData = getBytes();
        int lStart = 0;
        if (lData.length > 2) {
            if( lData[0] == (byte)0xFF && lData[1] == (byte)0xFE ) {
                lEncoding = "UTF-16LE"; //$NON-NLS-1$
                lStart=2;
            }
            else if( lData[0] == (byte)0xFE && lData[1] == (byte)0xFF ) {
                lEncoding = "UTF-16BE"; //$NON-NLS-1$
                lStart=2;
            }
        }
        try {
            outValue = new String( getBytes(), lStart, lData.length-lStart, lEncoding );
        }
        catch (UnsupportedEncodingException exc) {
            //should never happen
            exc.printStackTrace();
            outValue = new String( getBytes() );
        }
        this.str = outValue;
        return outValue;
    }

    /**
     * This will create a COS string from a string of hex characters.
     *
     * @param inHex A hex string.
     * @return A cos string with the hex characters converted to their actual bytes.
     * @throws IOException If there is an error with the hex string.
     */
    public static COSString createFromHexString(String inHex) throws IOException {
        COSString outValue = new COSString();
        StringBuilder lHexBuffer = new StringBuilder(inHex.trim());
        //if odd number then the last hex digit is assumed to be 0
        if (lHexBuffer.length() % 2 == 1 ) {
            lHexBuffer.append( "0" ); //$NON-NLS-1$
        }
        for (int i=0; i<lHexBuffer.length();) {
            String lHexChars = "" + lHexBuffer.charAt(i++) + lHexBuffer.charAt( i++ ); //$NON-NLS-1$
            try {
                outValue.append( Integer.parseInt( lHexChars, 16 ) );
            }
            catch (NumberFormatException exc) {
                throw new IOException( "Error: Expected hex number, actual='" + lHexChars + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return outValue;
    }

    @Override
    public String toString() {
    	return "COSString{" + this.getString() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        return getString().hashCode();
    }

    @Override
    public boolean equals(Object inObj) {
        if (inObj instanceof COSString) {
            inObj = ((COSString) inObj).getString();
            return this.getString().equals(inObj);
        }
        return false;
    }

}
