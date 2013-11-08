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
 * This class represents a PDF object.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSObject extends COSBase {

	private COSBase baseObject;
	private COSInteger objectNumber;
	private COSInteger generationNumber;

    /**
     * Constructor.
     *
     * @param object The object that this encapsulates.
     * @throws IOException If there is an error with the object passed in.
     */
	public COSObject(COSBase inObject) {
		setObject(inObject);
	}

    /**
     * This will set the object that this object encapsulates.
     *
     * @param object The new object to encapsulate.
     * @throws IOException If there is an error setting the updated object.
     */
	public void setObject(COSBase inObject) {
		baseObject = inObject;
	}

	/**
	 * This will get the object that this object encapsulates.
	 *
	 * @return the baseObject
	 */
	public COSBase getObject() {
		return baseObject;
	}

	/**
	 * Setter for property objectNumber.
	 *
	 * @param inObjectNumber COSInteger
	 */
	public void setObjectNumber(COSInteger inObjectNumber) {
		objectNumber = inObjectNumber;
	}

	/**
	 * Getter for property objectNumber.
	 *
	 * @return COSInteger
	 */
	public COSInteger getObjectNumber() {
		return objectNumber;
	}

	/**
	 * Setter for property generationNumber.
	 *
	 * @param inGenerationNumber COSInteger
	 */
	public void setGenerationNumber(COSInteger inGenerationNumber) {
		generationNumber = inGenerationNumber;
	}

	/**
	 * Getter for property generationNumber.
	 *
	 * @return COSInteger
	 */
	public COSInteger getGenerationNumber() {
		return generationNumber;
	}

	@Override
	public String toString() {
        return "COSObject{" + (objectNumber == null ? "unknown" : "" + objectNumber.intValue() ) + ", " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	        (generationNumber == null ? "unknown" : "" + generationNumber.intValue() ) + //$NON-NLS-1$ //$NON-NLS-2$
	        "}"; //$NON-NLS-1$
	}

}
