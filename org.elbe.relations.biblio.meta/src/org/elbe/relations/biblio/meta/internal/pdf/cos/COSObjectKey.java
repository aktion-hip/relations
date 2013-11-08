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

/**
 * Object representing the physical reference to an indirect pdf object.
 *
 * @author Michael Traut
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSObjectKey {

	private long number;
	private long generation;


    /**
     * PDFObjectKey constructor comment.
     *
     * @param num The object number.
     * @param gen The object generation number.
     */
	public COSObjectKey(long inNumber, long inGeneration) {
        setNumber(inNumber);
        setGeneration(inGeneration);
	}

    /**
     * PDFObjectKey constructor comment.
     *
     * @param object The object that this key will represent.
     */
	public COSObjectKey(COSObject inObject) {
		this(inObject.getObjectNumber().longValue(), inObject.getGenerationNumber().longValue());
	}

	private void setNumber(long inNumber) {
		number = inNumber;
	}

	private void setGeneration(long inGeneration) {
		generation = inGeneration;
	}

	/**
	 * @return the number
	 */
	public long getNumber() {
		return number;
	}

	/**
	 * @return the generation
	 */
	public long getGeneration() {
		return generation;
	}

	@Override
	public String toString() {
		return "" + getNumber() + " " + getGeneration() + " R";
	}

	@Override
	public int hashCode() {
		return (int)(number + generation);
	}

	@Override
	public boolean equals(Object inObj) {
        return (inObj instanceof COSObjectKey) &&
        ((COSObjectKey)inObj).getNumber() == getNumber() &&
        ((COSObjectKey)inObj).getGeneration() == getGeneration();
	}

}
