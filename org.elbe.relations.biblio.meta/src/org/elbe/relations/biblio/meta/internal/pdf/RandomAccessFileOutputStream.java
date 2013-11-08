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
import java.io.OutputStream;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSBase;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSNumber;
import org.elbe.relations.biblio.meta.internal.pdf.cos.COSObject;

/**
 * This will write to a RandomAccessFile in the filesystem and keep track
 * of the position it is writing to and the length of the stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class RandomAccessFileOutputStream extends OutputStream {

	private RandomAccess file;
	private long position;
    private long lengthWritten = 0;
    private COSBase expectedLength = null;

	public RandomAccessFileOutputStream(RandomAccess inRandomAccess) throws IOException {
        file = inRandomAccess;
        //first get the position that we will be writing to
        position = inRandomAccess.length();
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
        file.seek(position + lengthWritten);
        lengthWritten++;
        file.write(b);
	}


    /**
     * This will set the expected length of this stream.
     *
     * @param value The expected value.
     */
	public void setExpectedLength(COSBase inValue) {
		expectedLength = inValue;
	}

	/**
     * This will get the length that the PDF document specified this stream
     * should be.  This may not match the number of bytes read.
     *
     * @return The expected length.
     */
    public COSBase getExpectedLength() {
        return expectedLength;
    }

    /**
     * This will get the position in the RAF that the stream was written
     * to.
     *
     * @return The position in the raf where the file can be obtained.
     */
    public long getPosition() {
		return position;
	}

    /**
     * The number of bytes written to the stream.
     *
     * @return The number of bytes read to the stream.
     */
	public long getLength() {
        long length = -1;
        if (expectedLength instanceof COSNumber) {
            length = ((COSNumber)expectedLength).intValue();
        }
        else if (expectedLength instanceof COSObject && ((COSObject)expectedLength).getObject() instanceof COSNumber) {
            length = ((COSNumber)((COSObject)expectedLength).getObject()).intValue();
        }
        if (length == -1) {
            length = lengthWritten;
        }
        return length;
	}

    /**
     * Get the amount of data that was actually written to the stream, in theory this
     * should be the same as the length specified but in some cases it doesn't match.
     *
     * @return The number of bytes actually written to this stream.
     */
	public long getLengthWritten() {
		return lengthWritten;
	}

}
