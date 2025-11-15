/**
This package is part of Relations project.
Copyright (C) 2007-2018, Benno Luthiger
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.
You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.elbe.relations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Helper class for Zip file tests
 *
 * @author Luthiger
 */
public class ZipHouseKeeper {
    public static final String ROOT = "rt_data";
    public static final String PARENT = "parent";
    public static final String CHILD = "child";
    public static final String FILE1 = "child1.txt";
    public static final String FILE2 = "child2.txt";
    public static final String FILE3 = "child3.txt";
    public static final String FILE4 = "child4.txt";
    public static final String ZIP_FILE = "backup_test.zip";

    public static final String[] EXPECTED_NAMES = new String[] { ROOT + "\\child1.txt", ROOT + "\\parent\\child2.txt",
            ROOT + "\\parent\\child\\child3.txt", ROOT + "\\parent\\child4.txt" };
    public static final String[] EXPECTED_CONTENT = new String[] {
            FILE1 + " is contained in the test's root directory.",
            FILE2 + " is contained in the test's first sub-directory.",
            FILE3 + " is contained in the test's last sub-directory.",
            FILE4 + " is contained in the test's first sub-directory too." };

    /**
     * Creates directory structure for testing purpose
     *
     * @return File the root directory
     * @throws IOException
     */
    public static File createFiles() throws IOException {
        // create root and child1 as file contained in root
        final File outRoot = new File(ROOT);
        outRoot.mkdir();
        createFile(new File(outRoot, FILE1), EXPECTED_CONTENT[0]);
        // create subdirectory and child2 as file contained in this directory
        final File lSub = new File(outRoot, PARENT);
        lSub.mkdir();
        createFile(new File(lSub, FILE2), EXPECTED_CONTENT[1]);
        createFile(new File(lSub, FILE4), EXPECTED_CONTENT[3]);
        // create subdirectory of subdirectory and child3 as file contained in this directory
        final File lSubSub = new File(lSub, CHILD);
        lSubSub.mkdir();
        createFile(new File(lSubSub, FILE3), EXPECTED_CONTENT[2]);
        return outRoot;
    }

    private static void createFile(final File inFile, final String inContent) throws IOException {
        inFile.createNewFile();
        fillFile(inFile, inContent);
    }

    private static void fillFile(final File file, final String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            final BufferedWriter buffer = new BufferedWriter(writer);
            buffer.write(content);
            buffer.close();
        }
    }

    /** Deletes the specified directory structure.
     *
     * @param rootName String Path of root directory to delete with whole content. */
    public static void deleteTestFiles(final String rootName) {
        final File root = new File(rootName);
        if (root.exists()) {
            traverse(root);
            ensureDelete(root);
        }
    }

    private static void traverse(final File inDirectory) {
        final File[] lChildren = inDirectory.listFiles();
        for (int i = 0; i < lChildren.length; i++) {
            if (lChildren[i].isDirectory()) {
                traverse(lChildren[i]);
                ensureDelete(lChildren[i]);
            } else {
                ensureDelete(lChildren[i]);
            }
        }
    }

    /**
     * Make sure that the specified file is deleted, at least on exit.
     *
     * @param inFile
     *            File to delete.
     */
    public static void ensureDelete(final File inFile) {
        if (!inFile.delete()) {
            inFile.deleteOnExit();
        }
    }

    /**
     * Converts the specified array of files to a collection containing the file
     * names.
     *
     * @param inChilds
     *            File[]
     * @return Collection<String> of file names
     */
    public static Collection<String> getChildNames(final File[] inChilds) {
        final Collection<String> outChildNames = new ArrayList<>(inChilds.length);
        for (int i = 0; i < inChilds.length; i++) {
            outChildNames.add(inChilds[i].getName());
        }
        return outChildNames;
    }

    /** Asserts the specified file containing the specified text.
     *
     * @param message String Message
     * @param file File to test
     * @param text String Test to compare with file content.
     * @throws IOException */
    public static void assertFileContent(final String message, final File file, final String text) throws IOException {
        // final FileReader lReader = new FileReader(file);
        String read = "";
        try (FileReader reader = new FileReader(file)) {
            final BufferedReader buffer = new BufferedReader(reader);
            read = buffer.readLine();
        }
        assertEquals(text, read, message);
    }

    /** Returns the file with the specified name or <code>null</code>.
     *
     * @param children File[] array to look up the file with the specified name.
     * @param name File name
     * @return File or <code>null</code> */
    public static File getChildFile(final File[] children, final String name) {
        for (int i = 0; i < children.length; i++) {
            if (name.equals(children[i].getName())) {
                return children[i];
            }
        }
        return null;
    }

}