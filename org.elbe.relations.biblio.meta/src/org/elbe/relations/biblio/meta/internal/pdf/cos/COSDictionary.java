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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elbe.relations.biblio.meta.internal.pdf.DateConverter;

/**
 * This class represents a dictionary where name/value pairs reside.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSDictionary extends COSBase {

    /**
     * These are all of the items in the dictionary.
     */
    private Map<COSName, COSBase> items = new HashMap<COSName, COSBase>();

    /**
     * Used to store original sequence of keys, for testing.
     */
    private List<COSName> keys = new ArrayList<COSName>();

    /**
     * Default constructor.
     */
    public COSDictionary() {
    }

    /**
     * Copy Constructor. This will make a shallow copy of this dictionary.
     *
     * @param inDictionary COSDictionary the dictionary to copy.
     */
    public COSDictionary(COSDictionary inDictionary) {
        items = new HashMap<COSName, COSBase>(inDictionary.items);
        keys = new ArrayList<COSName>(inDictionary.keys);
	}

	/**
     * This will set an item in the dictionary.  If value is null then the result
     * will be the same as removeItem( key ).
     *
     * @param key The key to the dictionary object.
     * @param value The value to the dictionary object.
     */
	public void setItem(COSName inKey, COSBase inValue) {
        if (inValue == null) {
            removeItem(inKey);
        }
        else {
            if (!items.containsKey(inKey)) {
                // insert only if not already there
                keys.add(inKey);
            }
            items.put(inKey, inValue);
        }
	}

    /**
     * This will remove an item for the dictionary.  This
     * will do nothing of the object does not exist.
     *
     * @param inKey The key to the item to remove from the dictionary.
     */
    public void removeItem(COSName inKey) {
        keys.remove(inKey);
        items.remove(inKey);
    }

    /**
     * This will do a lookup into the dictionary.
     *
     * @param key The key to the object.
     * @return The item that matches the key.
     */
	public COSBase getItem(COSName inKey) {
        return (COSBase)items.get(inKey);
	}

    /**
     * This will add all of the dictionarys keys/values to this dictionary.
     * Only called when adding keys to a trailer that already exists.
     *
     * @param inDictionary The dic to get the keys from.
     */
	public void addAll(COSDictionary inDictionary) {
        Iterator<COSName> lDictionaryKeys = inDictionary.keyList().iterator();
        while (lDictionaryKeys.hasNext()) {
            COSName lKey = lDictionaryKeys.next();
            COSBase lValue = inDictionary.getItem(lKey);
            /*
             * If we're at a second trailer, we have a linearized
             * pdf file, meaning that the first Size entry represents
             * all of the objects so we don't need to grab the second.
             */
            if (!lKey.getName().equals("Size") || !keys.contains(COSName.getPDFName("Size"))) {
                setItem(lKey, lValue);
            }
        }
	}

    /**
     * This will get the keys for all objects in the dictionary in the sequence that
     * they were added.
     *
     * @return a list of the keys in the sequence of insertion
     */
	public List<COSName> keyList() {
		return keys;
	}

    /**
     * This will get an object from this dictionary.  If the object is a reference then it will
     * dereference it and get it from the document.  If the object is COSNull then
     * null will be returned.
     *
     * @param inKey The key to the object that we are getting.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject(COSName inKey) {
        COSBase outValue = (COSBase)items.get(inKey);
        if (outValue instanceof COSObject) {
            outValue = ((COSObject)outValue).getObject();
        }
        if (outValue instanceof COSNull) {
            outValue = null;
        }
        return outValue;
    }

	/**
	 * This will get an object from this dictionary.  If the object is a reference then it will
	 * dereference it and get it from the document.  If the object is COSNull then
	 * null will be returned.
	 *
	 * @param inKey The key to the object that we are getting.
	 * @return The object that matches the key.
	 */
	public COSBase getDictionaryObject(String inKey) {
		return getDictionaryObject(COSName.getPDFName(inKey));
	}

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an int.  -1 is returned if there is no value.
     *
     * @param inKey The key to the item in the dictionary.
     * @return The integer value.
     */
    public int getInt(String inKey) {
        return getInt(COSName.getPDFName(inKey));
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an int.  -1 is returned if there is no value.
     *
     * @param inKey The key to the item in the dictionary.
     * @return The integer value..
     */
    public int getInt(COSName inKey) {
        return getInt(inKey, -1);
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param inKey The key to the item in the dictionary.
     * @param inDefaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt(COSName inKey, int inDefaultValue) {
        return getInt(inKey.getName(), inDefaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param inKey The key to the item in the dictionary.
     * @param inDefaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt(String inKey, int inDefaultValue) {
        return getInt(new String []{inKey}, inDefaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be an integer.  If the dictionary value is null then the
     * default Value will be returned.
     *
     * @param inKeyList The key to the item in the dictionary.
     * @param inDefaultValue The value to return if the dictionary item is null.
     * @return The integer value.
     */
    public int getInt(String[] inKeyList, int inDefaultValue) {
        int outValue = inDefaultValue;
        COSBase lObject = getDictionaryObject( inKeyList );
        if (lObject != null && lObject instanceof COSNumber) {
            outValue = ((COSNumber)lObject).intValue();
        }
        return outValue;
    }

    /**
     * This is a special case of getDictionaryObject that takes multiple keys, it will handle
     * the situation where multiple keys could get the same value, ie if either CS or ColorSpace
     * is used to get the colorspace.
     * This will get an object from this dictionary.  If the object is a reference then it will
     * dereference it and get it from the document.  If the object is COSNull then
     * null will be returned.
     *
     * @param inKeyList The list of keys to find a value.
     *
     * @return The object that matches the key.
     */
    public COSBase getDictionaryObject(String[] inKeyList) {
        COSBase outValue = null;
        for (int i=0; i<inKeyList.length && outValue == null; i++) {
            outValue = getDictionaryObject(COSName.getPDFName(inKeyList[i]));
        }
        return outValue;
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param inKey The key to the item in the dictionary.
     * @return The name converted to a string.
     */
	public String getString(COSName inKey) {
        String outValue = null;
        COSBase lValue = getDictionaryObject(inKey);
        if (lValue != null && lValue instanceof COSString) {
            outValue = ((COSString)lValue).getString();
        }
        return outValue;
	}

    /**
     * This is a convenience method that will convert the value to a COSString
     * object.  If it is null then the object will be removed.
     *
     * @param inKey The key to the object,
     * @param inValue The string value for the name.
     */
    public void setString(COSName inKey, String inValue) {
        COSString name = null;
        if (inValue != null) {
            name = new COSString(inValue);
        }
        setItem(inKey, name);
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a name and convert it to a string.  Null is returned
     * if the entry does not exist in the dictionary.
     *
     * @param key The key to the item in the dictionary.
     * @return The name converted to a string.
     * @throws IOException If there is an error converting to a date.
     */
	public Calendar getDate(COSName inDate) throws IOException {
        COSString lDate = (COSString)getDictionaryObject(inDate);
        return DateConverter.toCalendar(lDate);
	}

    /**
     * Set the date object.
     *
     * @param inKey The key to the date.
     * @param inDate The date to set.
     */
    public void setDate(COSName inKey, Calendar inDate) {
        setString(inKey, DateConverter.toString(inDate));
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a cos boolean and convert it to a primitive boolean.
     *
     * @param inKey The key to the item in the dictionary.
     * @param inDefaultValue The value returned if the entry is null.
     * @return The value converted to a boolean.
     */
    public boolean getBoolean(String inKey, boolean inDefaultValue) {
        return getBoolean(COSName.getPDFName(inKey), inDefaultValue);
    }

    /**
     * This is a convenience method that will get the dictionary object that
     * is expected to be a COSBoolean and convert it to a primitive boolean.
     *
     * @param inKey The key to the item in the dictionary.
     * @param inDefaultValue The value returned if the entry is null.
     * @return The entry converted to a boolean.
     */
    public boolean getBoolean(COSName inKey, boolean inDefaultValue) {
        boolean outValue = inDefaultValue;
        COSBase lBoolean = getDictionaryObject(inKey);
        if (lBoolean != null && lBoolean instanceof COSBoolean) {
            outValue = ((COSBoolean)lBoolean).getValue();
        }
        return outValue;
    }

    /**
     * This will return the number of elements in this dictionary.
     *
     * @return The number of elements in the dictionary.
     */
    public int size() {
        return keys.size();
    }

    @Override
    public String toString() {
        String outValue = "COSDictionary{";
        for (int i = 0; i<size(); i++) {
            COSName lKey = (COSName)keyList().get(i);
            outValue = outValue + "(" + lKey + ":" + getDictionaryObject(lKey).toString() + ") ";
        }
        outValue = outValue + "}";
        return outValue;
    }

}
