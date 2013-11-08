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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * A simple subclass that adds a few convience methods.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class PushBackInputStream extends PushbackInputStream {
	private int offset = 0;

	public PushBackInputStream(InputStream inStream, int inSize) throws IOException {
		super(inStream, inSize);
		if (inStream == null) {
			throw new IOException("Error: input was null"); //$NON-NLS-1$
		}
	}

	public boolean isEOF() throws IOException {
		int lPeek = peek();
		return lPeek == -1;
	}

    /**
     * This will peek at the next byte.
     *
     * @return The next byte on the stream, leaving it as available to read.
     * @throws IOException If there is an error reading the next byte.
     */
	public int peek() throws IOException {
		int outResult = read();
		if (outResult != -1) {
			unread(outResult);
		}
		return outResult;
	}

	public int read() throws IOException {
		int outResult = super.read();
		if (outResult != -1) {
			offset++;
		}
		return outResult;
	}

	public void unread(int b) throws IOException {
		offset--;
		super.unread(b);
	}

	/**
	 * Returns the current byte offset in the file.
	 *
	 * @return int the byte offset.
	 */
	public int getOffset() {
		return offset;
	}

    /**
     * Reads a given number of bytes from the underlying stream.
     *
     * @param inLength the number of bytes to be read
     * @return a byte array containing the bytes just read
     * @throws IOException if an I/O error occurs while reading data
     */
	public byte[] readFully(int inLength) throws IOException {
        byte[] outData = new byte[inLength];
        int lPos = 0;
        while (lPos < inLength) {
            int lAmountRead = read(outData, lPos, inLength - lPos);
            if (lAmountRead < 0) {
                throw new EOFException("Premature end of file"); //$NON-NLS-1$
            }
            lPos += lAmountRead;
        }
        return outData;
	}

}
