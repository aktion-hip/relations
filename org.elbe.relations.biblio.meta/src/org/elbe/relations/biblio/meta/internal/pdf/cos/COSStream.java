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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.elbe.relations.biblio.meta.internal.pdf.RandomAccess;
import org.elbe.relations.biblio.meta.internal.pdf.RandomAccessFileInputStream;
import org.elbe.relations.biblio.meta.internal.pdf.RandomAccessFileOutputStream;
import org.elbe.relations.biblio.meta.internal.pdf.filter.Filter;
import org.elbe.relations.biblio.meta.internal.pdf.filter.FilterManager;

/**
 * This class represents a stream object in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class COSStream extends COSDictionary {
	private static final int BUFFER_SIZE=16384;

	private RandomAccess file;
	private RandomAccessFileOutputStream filteredStream;

	/**
     * The stream with no filters, this contains the useful data.
     */
    private RandomAccessFileOutputStream unFilteredStream;

	public COSStream(COSDictionary inDic, RandomAccess inFile) {
		super(inDic);
		file = inFile;
	}

    /**
     * This will create a new stream for which filtered byte should be
     * written to.  You probably don't want this but want to use the
     * createUnfilteredStream, which is used to write raw bytes to.
     *
     * @param expectedLength An entry where a length is expected.
     * @return A stream that can be written to.
     * @throws IOException If there is an error creating the stream.
     */
	public OutputStream createFilteredStream(COSBase expectedLength) throws IOException {
        filteredStream = new RandomAccessFileOutputStream( file );
        filteredStream.setExpectedLength( expectedLength );
        unFilteredStream = null;
        return new BufferedOutputStream(filteredStream, BUFFER_SIZE);
	}

    /**
     * This will get the logical content stream with none of the filters.
     *
     * @return the bytes of the logical (decoded) stream
     * @throws IOException when encoding/decoding causes an exception
     */
	public InputStream getUnfilteredStream() throws IOException {
        InputStream outValue = null;
        if (unFilteredStream == null) {
            doDecode();
        }

        //if unFilteredStream is still null then this stream has not been
        //created yet, so we should return null.
        if (unFilteredStream != null) {
            long lPosition = unFilteredStream.getPosition();
            long lLength = unFilteredStream.getLength();
            RandomAccessFileInputStream lInput = new RandomAccessFileInputStream(file, lPosition, lLength);
            outValue = new BufferedInputStream(lInput, BUFFER_SIZE);
        }
        else {
        	//if there is no stream data then simply return an empty stream.
        	outValue = new ByteArrayInputStream(new byte[0]);
        }
        return outValue;
	}

    /**
     * This will decode the physical byte stream applying all of the filters to the stream.
     *
     * @throws IOException If there is an error applying a filter to the stream.
     */
	private void doDecode() throws IOException {
		// FIXME: We shouldn't keep the same reference?
        unFilteredStream = filteredStream;

        COSBase lFilters = getFilters();
        if (lFilters == null) {
        	//then do nothing
        }
        else if (lFilters instanceof COSName) {
            doDecode((COSName)lFilters, 0);
        }
        else if (lFilters instanceof COSArray) {
            COSArray lFilterArray = (COSArray)lFilters;
            for (int i=0; i<lFilterArray.size(); i++ ) {
                COSName lFilterName = (COSName)lFilterArray.get(i);
                doDecode(lFilterName, i );
            }
        }
        else {
            throw new IOException( "Error: Unknown filter type:" + lFilters); //$NON-NLS-1$
        }
	}

    /**
     * This will decode applying a single filter on the stream.
     *
     * @param filterName The name of the filter.
     * @param filterIndex The index of the current filter.
     * @throws IOException If there is an error parsing the stream.
     */
    private void doDecode(COSName filterName, int filterIndex) throws IOException {
        FilterManager lManager = getFilterManager();
        Filter lFilter = lManager.getFilter(filterName);
        InputStream lInput;

        boolean lDone = false;
        IOException lException = null;
        long lPosition = unFilteredStream.getPosition();
        long lLength = unFilteredStream.getLength();
        // in case we need it later
        long lWrittenLength = unFilteredStream.getLengthWritten();

        if (lLength == 0) {
            //if the length is zero then don't bother trying to decode
            //some filters don't work when attempting to decode
            //with a zero length stream.  See zlib_error_01.pdf
            unFilteredStream = new RandomAccessFileOutputStream(file);
            lDone = true;
        }
        else {
            //ok this is a simple hack, sometimes we read a couple extra
            //bytes that shouldn't be there, so we encounter an error we will just
            //try again with one less byte.
            for (int tryCount=0; !lDone && tryCount<5; tryCount++) {
                try {
                    lInput = new BufferedInputStream(new RandomAccessFileInputStream(file, lPosition, lLength), BUFFER_SIZE);
                    unFilteredStream = new RandomAccessFileOutputStream(file);
                    lFilter.decode(lInput, unFilteredStream, this, filterIndex);
                    lDone = true;
                }
                catch (IOException exc) {
                    lLength--;
                    lException = exc;
                }
            }
            if (!lDone) {
                //if no good stream was found then lets try again but with the
                //length of data that was actually read and not length
                //defined in the dictionary
                lLength = lWrittenLength;
                for (int tryCount=0; !lDone && tryCount<5; tryCount++) {
                    try {
                        lInput = new BufferedInputStream(new RandomAccessFileInputStream( file, lPosition, lLength ), BUFFER_SIZE );
                        unFilteredStream = new RandomAccessFileOutputStream( file );
                        lFilter.decode( lInput, unFilteredStream, this, filterIndex );
                        lDone = true;
                    }
                    catch (IOException exc) {
                        lLength--;
                        lException = exc;
                    }
                }
            }
        }
        if (!lDone) {
            throw lException;
        }
	}

	/**
     * This will return the filters to apply to the byte stream.
     * The method will return
     * - null if no filters are to be applied
     * - a COSName if one filter is to be applied
     * - a COSArray containing COSNames if multiple filters are to be applied
     *
     * @return the COSBase object representing the filters
     */
    public COSBase getFilters() {
        return getDictionaryObject(COSName.FILTER);
    }

}
