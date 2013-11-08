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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class represents a PDF named object.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public final class COSName extends COSBase implements Comparable<COSName> {

    /**
     * Note: This is synchronized because a HashMap must be synchronized if accessed by
     * multiple threads.
     */
    private static Map<String, COSName> nameMap = Collections.synchronizedMap(new WeakHashMap<String, COSName>(8192));

    /**
     * All common COSName values are stored in a simple HashMap. They are already defined as
     * static constants and don't need to be synchronized for multithreaded environments.
     */
    private static Map<String, COSName> commonNameMap = new HashMap<String, COSName>();

	/**
    * A common COSName value.
    */
    public static final COSName TYPE = new COSName("Type");
    /**
    * A common COSName value.
    */
    public static final COSName LENGTH = new COSName("Length");
    /**
     * A common COSName value.
     */
    public static final COSName FILTER = new COSName("Filter");
    /**
     * A common COSName value.
     */
    public static final COSName FLATE_DECODE = new COSName("FlateDecode");
    /**
     * A common COSName value.
     */
    public static final COSName FLATE_DECODE_ABBREVIATION = new COSName("Fl");
    /**
     * A common COSName value.
     */
    public static final COSName DCT_DECODE = new COSName("DCTDecode");
    /**
     * A common COSName value.
     */
    public static final COSName DCT_DECODE_ABBREVIATION = new COSName("DCT");
    /**
     * A common COSName value.
     */
    public static final COSName CCITTFAX_DECODE = new COSName("CCITTFaxDecode");
    /**
     * A common COSName value.
     */
    public static final COSName CCITTFAX_DECODE_ABBREVIATION = new COSName("CCF");
    /**
     * A common COSName value.
     */
    public static final COSName LZW_DECODE = new COSName("LZWDecode");
    /**
     * A common COSName value.
     */
    public static final COSName LZW_DECODE_ABBREVIATION = new COSName("LZW");
    /**
     * A common COSName value.
     */
    public static final COSName ASCII_HEX_DECODE = new COSName("ASCIIHexDecode");
    /**
     * A common COSName value.
     */
    public static final COSName ASCII_HEX_DECODE_ABBREVIATION = new COSName("AHx");
    /**
     * A common COSName value.
     */
    public static final COSName ASCII85_DECODE = new COSName("ASCII85Decode");
    /**
     * A common COSName value.
     */
    public static final COSName ASCII85_DECODE_ABBREVIATION = new COSName("A85");
    /**
     * A common COSName value.
     */
    public static final COSName RUN_LENGTH_DECODE = new COSName("RunLengthDecode");
    /**
     * A common COSName value.
     */
    public static final COSName RUN_LENGTH_DECODE_ABBREVIATION = new COSName("RL");
    /**
     * A common COSName value.
     */
    public static final COSName INFO = new COSName("Info");

    /**
     * The prefix to a PDF name.
     */
    public static final byte[] NAME_PREFIX = new byte[] {47 }; // The / character
    /**
     * The escape character for a name.
     */
    public static final byte[] NAME_ESCAPE = new byte[] {35 };  //The # character

    private String name;
	private int hashCode;

    /**
     * Private constructor.  This will limit the number of COSName objects.
     * that are created.
     *
     * @param inName The name of the COSName object.
     * @param isState Indicates if the COSName object is static so that it can
     *        be stored in the HashMap without synchronizing.
     */
    private COSName(String inName, boolean isState) {
        name = inName;
        if (isState) {
            commonNameMap.put(inName, this);
        }
        else {
            nameMap.put(inName, this);
        }
        hashCode = name.hashCode();
    }

    /**
     * Private constructor.  This will limit the number of COSName objects.
     * that are created.
     *
     * @param inName String The name of the COSName object.
     */
    private COSName(String inName) {
		this(inName, true);
	}

	@Override
	public int compareTo(COSName inOther) {
		return name.compareTo(inOther.name);
	}

    /**
     * This will get a COSName object with that name.
     *
     * @param inName The name of the object.
     * @return A COSName with the specified name.
     */
    public static final COSName getPDFName(String inName) {
        COSName outName = null;
        if (inName != null) {
            // Is it a common COSName ??
            outName = (COSName)commonNameMap.get(inName);
            if (outName == null) {
                // It seems to be a document specific COSName
                outName = (COSName)nameMap.get(inName);
                if (outName == null) {
                    //name is added to the synchronized map in the constructor
                    outName = new COSName(inName, false);
                }
            }
        }
        return outName;
    }

    @Override
    public int hashCode() {
    	return hashCode;
    }

    /**
     * This will get the name of this COSName object.
     *
     * @return The name of the object.
     */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "COSName{" + name + "}";
	}

	@Override
	public boolean equals(Object inObj) {
        boolean retval = this == inObj;
        if( !retval && inObj instanceof COSName )
        {
            COSName other = (COSName)inObj;
            retval = name == other.name || name.equals( other.name );
        }
        return retval;
	}
}
