/*
This package is part of Relations application.
Copyright (C) 2010, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.elbe.relations.biblio.meta.internal.extract;

import java.io.File;
import java.io.IOException;

import org.elbe.relations.biblio.meta.internal.utility.ExtractorUtil;
import org.elbe.relations.biblio.meta.internal.utility.FileDataSource;
import org.elbe.relations.parsing.ExtractedData;
import org.elbe.relations.services.IExtractorAdapter;

/**
 * Adapter to extract metadata from a GIF.
 * 
 * @author Luthiger Created on 20.01.2010
 */
public class GifExtractor extends AbstractExtractor implements
		IExtractorAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.biblio.meta.internal.extract.AbstractExtractor#
	 * getInputType()
	 */
	@Override
	protected String getInputType() {
		return "image/gif"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.ds.IExtractorAdapter#acceptsFile(java.io.File)
	 */
	@Override
	public boolean acceptsFile(final File inFile) {
		boolean outGif = false;
		final FileDataSource lSource = new FileDataSource(inFile);
		try {
			final String lHead = ExtractorUtil.getFixedStringValue(lSource, 6)
					.toLowerCase();
			outGif = lHead.equals("gif87a") || lHead.equals("gif89a"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (final IOException exc) {
			// intentionally left empty
		} finally {
			close(lSource);
		}
		return outGif;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.ds.IExtractorAdapter#process(java.io.File)
	 */
	@Override
	public ExtractedData process(final File inFile) throws IOException {
		final ExtractedData outExtracted = extractGenericData(inFile);

		final FileDataSource lSource = new FileDataSource(inFile);
		try {
			while (true) {
				final long lBlock = ExtractorUtil.getNumericalValue(
						lSource.getData(1), false);
				if (lBlock == 0x21) {
					final long lSub = ExtractorUtil.getNumericalValue(
							lSource.getData(1), false);
					if (lSub == 0xfe) {
						// comment
						final ParameterObject lComment = new ParameterObject();
						readSubBlocks(lSource, false, lComment);
						if (lComment.isFilled()) {
							outExtracted.setComment(lComment.value);
						}
						break; // we're only interested in the comment
					}
				} else if (lBlock == 0x3b) {
					// terminator
					break;
				}
			}
		} finally {
			close(lSource);
		}
		return outExtracted;
	}

	private int readSubBlocks(final FileDataSource inSource,
			final boolean inSkip, final ParameterObject inParameter)
			throws IOException {
		boolean lHasMore = true;
		int out = 0;
		while (lHasMore) {
			lHasMore = readSubBlock(inSource, inSkip, inParameter);
			if (lHasMore) {
				out++;
			}
		}
		return out;
	}

	private boolean readSubBlock(final FileDataSource inSource,
			final boolean inSkip, final ParameterObject inParameter)
			throws IOException {
		final byte[] lBytes = inSource.getData(1);
		final long lSize = ExtractorUtil.getNumericalValue(lBytes, false);
		if (lSize == 0x00) {
			return false;
		}
		if (inSkip) {
			inSource.setPosition(inSource.getPosition() + lSize);
		} else {
			inParameter.value = ExtractorUtil.getFixedStringValue(inSource,
					(int) lSize - 1);
		}
		return true;
	}

	// --- private classes ---

	private class ParameterObject {
		String value = ""; //$NON-NLS-1$

		boolean isFilled() {
			return value.length() > 0;
		}
	}

}
