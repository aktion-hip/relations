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
 * This is the interface that will be used to apply filters to a byte stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public interface Filter {
    /**
     * This will decode some compressed data.
     *
     * @param inCompressedData The compressed byte stream.
     * @param inResult The place to write the uncompressed byte stream.
     * @param inOptions The options to use to encode the data.
     * @param inFilterIndex The index to the filter being decoded.
     *
     * @throws IOException If there is an error decompressing the stream.
     */
    public void decode(InputStream inCompressedData, OutputStream inResult, COSDictionary inOptions, int inFilterIndex) throws IOException;

    /**
     * This will encode some data.
     *
     * @param inRawData The raw data to encode.
     * @param inResult The place to write to encoded results to.
     * @param inOptions The options to use to encode the data.
     * @param inFilterIndex The index to the filter being encoded.
     *
     * @throws IOException If there is an error compressing the stream.
     */
    public void encode(InputStream inRawData, OutputStream inResult, COSDictionary inOptions, int inFilterIndex) throws IOException;

}
