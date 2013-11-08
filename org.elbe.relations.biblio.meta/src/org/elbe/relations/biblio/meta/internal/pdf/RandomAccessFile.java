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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * An interface to allow temp PDF data to be stored in a scratch
 * file on the disk to reduce memory consumption.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class RandomAccessFile implements RandomAccess {

	private java.io.RandomAccessFile ras;

	public RandomAccessFile(File file, String mode) throws FileNotFoundException {
		ras = new java.io.RandomAccessFile(file, mode);
	}

	@Override
	public long length() throws IOException {
        return ras.length();
	}

	@Override
	public void seek(long inPosition) throws IOException {
		ras.seek(inPosition);
	}

	@Override
	public void write(int inByte) throws IOException {
		ras.write(inByte);
	}

	@Override
	public int read() throws IOException {
		return ras.read();
	}

	@Override
	public void close() throws IOException {
		ras.close();
	}

}
