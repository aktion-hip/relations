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
 * This class represents an abstract number in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public abstract class COSNumber extends COSBase {
    /**
     * ZERO.
    */
    public static final COSInteger ZERO = new COSInteger(0);

    /**
     * ONE.
    */
    public static final COSInteger ONE = new COSInteger(1);

    /**
     * Efficient lookup table for the ten decimal digits.
     */
    private static final COSInteger[] DIGITS = new COSInteger[] {
        ZERO,
        ONE,
        new COSInteger(2),
        new COSInteger(3),
        new COSInteger(4),
        new COSInteger(5),
        new COSInteger(6),
        new COSInteger(7),
        new COSInteger(8),
        new COSInteger(9),
    };

    /**
     * This factory method will get the appropriate number object.
     *
     * @param number The string representation of the number.
     * @return A number object, either float or int.
     * @throws IOException If the string is not a number.
     */
	public static COSBase get(String number) throws IOException {
        if (number.length() == 1) {
            char digit = number.charAt(0);
            if ('0' <= digit && digit <= '9') {
                return DIGITS[digit - '0'];
            }
            else {
                throw new IOException("Not a number: " + number);
            }
        }
        else if (number.indexOf('.') == -1) {
            return new COSInteger(number);
        }
        else {
            return new COSFloat(number);
        }
	}

    /**
     * This will get the integer value of this number.
     *
     * @return The integer value of this number.
     */
    public abstract int intValue();

}
