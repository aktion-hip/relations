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

import java.io.IOException;

/**
 * This class represents an integer number in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSInteger extends COSNumber {

	private long value;

	public COSInteger(long inValue) {
		value = inValue;
	}

	/**
	 *
	 * @param inValue String
	 * @throws IOException
	 */
    public COSInteger(String inValue) throws IOException {
    	try {
    		value = Long.parseLong(inValue);
    	}
    	catch( NumberFormatException e ) {
    		throw new IOException( "Error: value is not an integer type actual='" + inValue + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
    	}
	}

	/**
     * Polymorphic access to value as int
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
	public long longValue() {
		return value;
	}

    /**
     * Polymorphic access to value as int
     * This will get the integer value of this object.
     *
     * @return The int value of this object,
     */
	public int intValue() {
		return (int)value;
	}

	@Override
	public String toString() {
		return "COSInt{" + value + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int hashCode() {
        return (int)(value ^ (value >> 32));
	}

	@Override
	public boolean equals(Object inObj) {
        return inObj instanceof COSInteger && ((COSInteger)inObj).intValue() == intValue();
	}

}
