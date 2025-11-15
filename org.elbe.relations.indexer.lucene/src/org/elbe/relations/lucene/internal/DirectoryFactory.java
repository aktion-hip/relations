/**
 *
 */
package org.elbe.relations.lucene.internal;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.store.Directory;

/**
 * Interface for index Directory factories.
 *
 * @author lbenno
 */
public interface DirectoryFactory {

	/** Creates the directory in the specified file system location.
     *
     * @param inIndexDir {@link Path}
     * @return {@link Directory}
     * @throws IOException */
    Directory getDirectory(Path inIndexDir) throws IOException;

    // File getIndexContainer(String inIndexName);
}
