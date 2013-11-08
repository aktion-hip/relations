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
 * This class represents a floating point number in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSFloat extends COSNumber {

	private float value;

	public COSFloat(float inValue) {
		value = inValue;
	}

	public COSFloat(String inValue) throws IOException {
        try {
            value = Float.parseFloat( inValue );
        }
        catch( NumberFormatException exc) {
            throw new IOException( "Error expected floating point number actual='" +inValue + "'" );
        }
	}

	public float floatValue() {
		return value;
	}

	public double doubleValue() {
		return value;
	}

	public long longValue() {
		return (long)value;
	}

	public int intValue() {
		return (int)value;
	}

	@Override
	public String toString() {
		return "COSFloat{" + value + "}";
	}

	@Override
	public int hashCode() {
		return Float.floatToIntBits(value);
	}

	@Override
	public boolean equals(Object inOjb) {
        return inOjb instanceof COSFloat && Float.floatToIntBits(((COSFloat)inOjb).value) == Float.floatToIntBits(value);
	}

}
