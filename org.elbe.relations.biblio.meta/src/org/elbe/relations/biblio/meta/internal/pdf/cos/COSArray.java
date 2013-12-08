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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An array of PDFBase objects as part of the PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSArray extends COSBase {
	private List<COSBase> objects = new ArrayList<COSBase>();

    /**
     * This will add an object to the array.
     *
     * @param inObject The object to add to the array.
     */
    public void add(COSBase inObject) {
        objects.add(inObject);
    }

    /**
     * This will get the size of this array.
     *
     * @return The number of elements in the array.
     */
	public int size() {
		return objects.size();
	}

    /**
     * This will get an object from the array.  This will NOT derefernce
     * the COS object.
     *
     * @param inIndex The index into the array to get the object.
     *
     * @return The object at the requested index.
     */
    public COSBase get(int inIndex) {
        return (COSBase)objects.get(inIndex);
    }

    /**
     * This will remove an element from the array.
     *
     * @param inIndex The index of the object to remove.
     *
     * @return The object that was removed.
     */
    public COSBase remove(int inIndex) {
        return (COSBase)objects.remove(inIndex);
    }

    /**
     * This will remove an element from the array.
     *
     * @param inObject The object to remove.
     *
     * @return The object that was removed.
     */
    public boolean remove(COSBase inObject) {
        return objects.remove(inObject);
    }

    /**
     * Get access to the list.
     *
     * @return an iterator over the array elements
     */
	public Iterator<COSBase> iterator() {
		return objects.iterator();
	}

    /**
     * Get the value of the array as an integer.
     *
     * @param index The index into the list.
     * @return The value at that index or -1 if it is null.
     */
	public int getInt(int inIndex) {
		return getInt(inIndex, -1);
	}

    /**
     * Get the value of the array as an integer, return the default if it does
     * not exist.
     *
     * @param inIndex The value of the array.
     * @param inDefaultValue The value to return if the value is null.
     * @return The value at the index or the defaultValue.
     */
    public int getInt(int inIndex, int inDefaultValue) {
        int outValue = inDefaultValue;
        if (inIndex < size()) {
            Object lObject = objects.get( inIndex );
            if (lObject instanceof COSNumber) {
                outValue = ((COSNumber)lObject).intValue();
            }
        }
        return outValue;
    }


    /**
     * This will get an object from the array.  This will dereference the object.
     * If the object is COSNull then null will be returned.
     *
     * @param inIndex The index into the array to get the object.
     *
     * @return The object at the requested index.
     */
    public COSBase getObject(int inIndex) {
        Object outObject = objects.get(inIndex);
        if (outObject instanceof COSObject) {
            outObject = ((COSObject)outObject).getObject();
        }
        else if (outObject instanceof COSNull) {
            outObject = null;
        }
        return (COSBase)outObject;
    }

    @Override
    public String toString() {
        return "COSArray{" + objects + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
