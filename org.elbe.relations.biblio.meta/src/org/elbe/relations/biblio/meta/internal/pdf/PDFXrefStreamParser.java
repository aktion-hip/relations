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
package org.elbe.relations.biblio.meta.internal.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSArray;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSBase;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDictionary;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSDocument;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSInteger;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSObjectKey;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSStream;

/**
 * This will parse a PDF 1.5 (or better) Xref stream and
 * extract the xref information from the stream.
 *
 * @author <a href="mailto:justinl@basistech.com">Justin LeFebvre</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class PDFXrefStreamParser extends BaseParser {

	private COSDictionary stream;

	/**
	 *
	 * @param inStream COSStream
	 * @param inDocument COSDocument
	 * @throws IOException
	 */
	public PDFXrefStreamParser(COSStream inStream, COSDocument inDocument) throws IOException {
        super(inStream.getUnfilteredStream());
        setDocument(inDocument);
        stream = inStream;
	}

    /**
     * Parses through the unfiltered stream and populates the xrefTable HashMap.
     *
     * @throws IOException If there is an error while parsing the stream.
     */
	public void parse() throws IOException {
		try {
            COSArray lXrefFormat = (COSArray)stream.getDictionaryObject("W"); //$NON-NLS-1$
            COSArray lIndexArray = (COSArray)stream.getDictionaryObject("Index"); //$NON-NLS-1$
            /*
             * If Index doesn't exist, we will use the default values.
             */
            if (lIndexArray == null) {
                lIndexArray = new COSArray();
                lIndexArray.add(new COSInteger(0));
                lIndexArray.add(stream.getDictionaryObject("Size")); //$NON-NLS-1$
            }
            ArrayList<Integer> lObjNumbers = new ArrayList<Integer>();

            /*
             * Populates objNums with all object numbers available
             */
            Iterator<COSBase> lIndexIterator = lIndexArray.iterator();
            while (lIndexIterator.hasNext()) {
                int lObjID = ((COSInteger)lIndexIterator.next()).intValue();
                int lSize = ((COSInteger)lIndexIterator.next()).intValue();
                for (int i = 0; i < lSize; i++) {
                    lObjNumbers.add(new Integer(lObjID + i));
                }
            }
            Iterator<Integer> lObjIterator = lObjNumbers.iterator();
            /*
             * Calculating the size of the line in bytes
             */
            int w0 = lXrefFormat.getInt(0);
            int w1 = lXrefFormat.getInt(1);
            int w2 = lXrefFormat.getInt(2);
            int lLineSize = w0 + w1 + w2;

            while (pdfSource.available() > 0 && lObjIterator.hasNext()) {
                byte[] lCurrentLine = new byte[lLineSize];
                pdfSource.read(lCurrentLine);

                int lType = 0;
                /*
                 * Grabs the number of bytes specified for the first column in
                 * the W array and stores it.
                 */
                for (int i = 0; i < w0; i++) {
                    lType += (lCurrentLine[i] & 0x00ff) << ((w0 - i - 1)* 8);
                }
                //Need to remember the current objID
                Integer lObjID = (Integer)lObjIterator.next();
                /*
                 * 3 different types of entries.
                 */
                switch (lType) {
                case 0:
                    /*
                     * Skipping free objects
                     */
                    break;
                case 1:
                    int lOffset = 0;
                    for (int i = 0; i < w1; i++) {
                        lOffset += (lCurrentLine[i + w0] & 0x00ff) << ((w1 - i - 1) * 8);
                    }
                    int lGeneratedNumber = 0;
                    for (int i = 0; i < w2; i++) {
                        lGeneratedNumber += (lCurrentLine[i + w0 + w1] & 0x00ff) << ((w2 - i - 1) * 8);
                    }
                    COSObjectKey lObjKey = new COSObjectKey(lObjID.intValue(), lGeneratedNumber);
                    document.setXRef(lObjKey, lOffset);
                    break;
                case 2:
                    /*
                     * These objects are handled by the dereferenceObjects() method
                     * since they're only pointing to object numbers
                     */
                    break;
                default:
                    break;
                }
            }
		}
        finally {
            pdfSource.close();
        }
	}

}
