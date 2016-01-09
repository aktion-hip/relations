/*
This package is part of Relations project.
Copyright (C) 2007, Benno Luthiger
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

import static org.junit.Assert.assertEquals;

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
	public final static String ROOT = "data";
	public final static String PARENT = "parent";
	public final static String CHILD = "child";
	public final static String FILE1 = "child1.txt";
	public final static String FILE2 = "child2.txt";
	public final static String FILE3 = "child3.txt";
	public final static String FILE4 = "child4.txt";
	public final static String ZIP_FILE = "backup_test.zip";

	public final static String[] EXPECTED_NAMES = new String[] { "data\\child1.txt", "data\\parent\\child2.txt",
			"data\\parent\\child\\child3.txt", "data\\parent\\child4.txt" };

	public final static String[] EXPECTED_CONTENT = new String[] {
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
		// create subdirectory of subdirectory and child3 as file contained in
		// this directory
		final File lSubSub = new File(lSub, CHILD);
		lSubSub.mkdir();
		createFile(new File(lSubSub, FILE3), EXPECTED_CONTENT[2]);
		return outRoot;
	}

	private static void createFile(final File inFile, final String inContent) throws IOException {
		inFile.createNewFile();
		fillFile(inFile, inContent);
	}

	private static void fillFile(final File inFile, final String inContent) throws IOException {
		final FileWriter lWriter = new FileWriter(inFile);
		final BufferedWriter lBuffer = new BufferedWriter(lWriter);
		try {
			lBuffer.write(inContent);
		} finally {
			lBuffer.close();
			lWriter.close();
		}
	}

	/**
	 * Deletes the specified directory structure.
	 *
	 * @param inRootName
	 *            String Path of root directory to delete with whole content.
	 */
	public static void deleteTestFiles(final String inRootName) {
		final File lRoot = new File(inRootName);
		if (lRoot.exists()) {
			traverse(lRoot);
			ensureDelete(lRoot);
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
		final Collection<String> outChildNames = new ArrayList<String>(inChilds.length);
		for (int i = 0; i < inChilds.length; i++) {
			outChildNames.add(inChilds[i].getName());
		}
		return outChildNames;
	}

	/**
	 * Asserts the specified file containing the specified text.
	 *
	 * @param inMessage
	 *            String Message
	 * @param inFile
	 *            File to test
	 * @param inText
	 *            String Test to compare with file content.
	 * @throws IOException
	 */
	public static void assertFileContent(final String inMessage, final File inFile, final String inText)
			throws IOException {
		final FileReader lReader = new FileReader(inFile);
		final BufferedReader lBuffer = new BufferedReader(lReader);
		String lRead = "";
		try {
			lRead = lBuffer.readLine();
		} finally {
			lBuffer.close();
			lReader.close();
		}
		assertEquals(inMessage, inText, lRead);
	}

	/**
	 * Returns the file with the specified name or <code>null</code>.
	 *
	 * @param inChilds
	 *            File[] array to look up the file with the specified name.
	 * @param inName
	 *            File name
	 * @return File or <code>null</code>
	 */
	public static File getChildFile(final File[] inChilds, final String inName) {
		for (int i = 0; i < inChilds.length; i++) {
			if (inName.equals(inChilds[i].getName())) {
				return inChilds[i];
			}
		}
		return null;
	}

}