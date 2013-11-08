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
import java.io.InputStream;

/**
 * This class allows a section of a RandomAccessFile to be accessed as an
 * input stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class RandomAccessFileInputStream extends InputStream {

	private RandomAccess file;
	private long currentPosition;
	private long endPosition;

	/**
	 *
	 * @param inFile
	 * @param inPosition
	 * @param inLength
	 */
	public RandomAccessFileInputStream(RandomAccess inFile, long inPosition, long inLength) {
        file = inFile;
        currentPosition = inPosition;
        endPosition = currentPosition + inLength;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
        synchronized(file) {
            int retval = -1;
            if (currentPosition < endPosition) {
                file.seek( currentPosition );
                currentPosition++;
                retval = file.read();
            }
            return retval;
        }
	}

}
