/**
 *
 */
package org.elbe.relations.lucene.internal;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;

/**
 * Interface for index Directory factories.
 *
 * @author lbenno
 */
public interface DirectoryFactory {

	/**
	 * Creates the directory in the specified file system location.
	 *
	 * @param inIndexDir
	 *            {@link File}
	 * @return {@link Directory}
	 * @throws IOException
	 */
	Directory getDirectory(File inIndexDir) throws IOException;

	// File getIndexContainer(String inIndexName);
}
