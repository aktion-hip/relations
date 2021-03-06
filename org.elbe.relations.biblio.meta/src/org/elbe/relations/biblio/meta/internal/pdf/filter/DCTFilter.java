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
import java.io.InputStream;
import java.io.OutputStream;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDictionary;

/**
 * This is the used for the DCTDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public class DCTFilter implements Filter
{

    /**
     * {@inheritDoc}
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex )
        throws IOException
    {
        System.err.println( "Warning: DCTFilter.decode is not implemented yet, skipping this stream." ); //$NON-NLS-1$
    }

     /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex )
        throws IOException
    {
        System.err.println( "Warning: DCTFilter.encode is not implemented yet, skipping this stream." ); //$NON-NLS-1$
    }
}
