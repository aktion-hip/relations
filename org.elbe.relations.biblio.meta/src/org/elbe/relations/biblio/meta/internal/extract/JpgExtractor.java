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
 * Adapter to extract metadata from a jpg.
 * 
 * @author Luthiger Created on 14.01.2010
 */
public class JpgExtractor extends AbstractExtractor implements
		IExtractorAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.ds.IExtractorAdapter#acceptsFile(java.io.File)
	 */
	@Override
	public boolean acceptsFile(final File inFile) {
		boolean outJpg = false;
		final FileDataSource lSource = new FileDataSource(inFile);
		try {
			final JpgMarker lMarker = readMarker(lSource);
			if (lMarker.type == 0xd8 && lMarker.length == 0) {
				outJpg = true;
			}
		}
		catch (final IOException exc) {
			// intentionally left empty
		} finally {
			close(lSource);
		}
		return outJpg;
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
			JpgMarker lMarker = null;

			while (true) {
				lMarker = readMarker(lSource);
				final long lPosition = lSource.getPosition();

				if (lMarker.delim == 0xFF) {
					if (lMarker.type == 0xfe) {
						final String lComment = ExtractorUtil
								.getFixedStringValue(lSource,
										(int) lMarker.length - 1);
						outExtracted.setComment(lComment);
						break; // we're only interested in the comment
					}
				}
				if (lMarker.type == 0xda) {
					lSource.setPosition(inFile.length() - 2);
				} else {
					lSource.setPosition(lMarker.length + lPosition);
				}
				if (lMarker.type == 0xd9) {
					// EOF marker, we are done!
					break;
				}
			}
		} finally {
			close(lSource);
		}

		return outExtracted;
	}

	@Override
	protected String getInputType() {
		return "image/jpeg"; //$NON-NLS-1$
	}

	private JpgMarker readMarker(final FileDataSource inFile)
			throws IOException {
		final JpgMarker outMarker = new JpgMarker();
		outMarker.delim = ExtractorUtil.getNumericalValue(inFile, 1, true);
		outMarker.type = ExtractorUtil.getNumericalValue(inFile, 1, true);

		if (outMarker.delim == 0xFF) {
			if ((outMarker.type == 0xd8) || (outMarker.type == 0xd9)) {
				outMarker.length = 0;
			} else {
				// give the length as a length from the current files marker -
				// JPEG block lengths INCLUDE the two bytes of the length itself
				outMarker.length = ExtractorUtil.getNumericalValue(inFile, 2,
						true) - 2;
			}
		}

		return outMarker;
	}

	// --- private classes ---

	private static class JpgMarker {
		public long type;
		public long delim;
		public long length;

		@Override
		public String toString() {
			return "JPEGMarker[" + "type=" + Long.toHexString(type) + ", length=" + length + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

}
