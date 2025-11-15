/**
 *
 */
package org.elbe.relations.biblio.meta.internal.extract;

/** Utility class for testing. */
public final class TestUtil {
    public static final String NBSP = "\u202F";
    public static final String REGEX = "(Last Modified:\\s)(.+)(</i>)";
    public static final String REPLACEMENT = "$1XXX$3";

    private TestUtil() {
        // prevent instantiation
    }
}
