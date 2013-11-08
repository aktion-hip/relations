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
package org.elbe.relations.biblio.meta.internal.pdf.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSName;

/**
 * This will contain manage all the different types of filters that are available.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class FilterManager {
	private Map<COSName, Filter> filters = new HashMap<COSName, Filter>();

    /**
     * Constructor.
     */
    public FilterManager() {
        Filter lFlateFilter = new FlateFilter();
        Filter lDctFilter = new DCTFilter();
        Filter lCcittFaxFilter = new CCITTFaxDecodeFilter();
        Filter lLzwFilter = new LZWFilter();
        Filter lAsciiHexFilter = new ASCIIHexFilter();
        Filter lAscii85Filter = new ASCII85Filter();
        Filter lRunLengthFilter = new RunLengthDecodeFilter();

        addFilter(COSName.FLATE_DECODE, lFlateFilter);
        addFilter(COSName.FLATE_DECODE_ABBREVIATION, lFlateFilter);
        addFilter(COSName.DCT_DECODE, lDctFilter);
        addFilter(COSName.DCT_DECODE_ABBREVIATION, lDctFilter);
        addFilter(COSName.CCITTFAX_DECODE, lCcittFaxFilter);
        addFilter(COSName.CCITTFAX_DECODE_ABBREVIATION, lCcittFaxFilter);
        addFilter(COSName.LZW_DECODE, lLzwFilter);
        addFilter(COSName.LZW_DECODE_ABBREVIATION, lLzwFilter);
        addFilter(COSName.ASCII_HEX_DECODE, lAsciiHexFilter);
        addFilter(COSName.ASCII_HEX_DECODE_ABBREVIATION, lAsciiHexFilter);
        addFilter(COSName.ASCII85_DECODE, lAscii85Filter );
        addFilter(COSName.ASCII85_DECODE_ABBREVIATION, lAscii85Filter);
        addFilter(COSName.RUN_LENGTH_DECODE, lRunLengthFilter);
        addFilter(COSName.RUN_LENGTH_DECODE_ABBREVIATION, lRunLengthFilter);
    }

    /**
     * This will get all of the filters that are available in the system.
     *
     * @return All available filters in the system.
     */
    public Collection<Filter> getFilters() {
        return filters.values();
    }

    /**
     * This will add an available filter.
     *
     * @param inFilterName The name of the filter.
     * @param inFilter The filter to use.
     */
    public void addFilter(COSName inFilterName, Filter inFilter) {
        filters.put(inFilterName, inFilter);
    }

    /**
     * This will get a filter by name.
     *
     * @param inFilterName The name of the filter to retrieve.
     * @return The filter that matches the name.
     * @throws IOException If the filter could not be found.
     */
	public Filter getFilter(COSName inFilterName) throws IOException {
        Filter outFilter = (Filter)filters.get(inFilterName);
        if (outFilter == null) {
            throw new IOException( "Unknown stream filter:" + inFilterName);
        }
        return outFilter;
	}

}
