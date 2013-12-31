/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/

package org.elbe.relations.biblio.meta.internal.html;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.parsing.XPathHelper;
import org.elbe.relations.utility.NewTextAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit Plug-in test
 * 
 * @author lbenno
 */
@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class RDFaExtractorTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String FILE1 = "resources/rdfa_example1.html";
	private static final String FILE2 = "resources/rdfa_example2.html";
	private static final String FILE3 = "resources/rdfa_example3.html";

	private File file1;
	private File file2;
	private File file3;

	private IEclipseContext context;

	@Mock
	private Logger log;

	@Mock
	private IDataService data;

	@Before
	public void setUp() throws Exception {
		file1 = new File(FILE1);
		file2 = new File(FILE2);
		file3 = new File(FILE3);

		context = EclipseContextFactory.create("test context");
		context.set(Logger.class, log);
		context.set(IDataService.class, data);
	}

	@Test
	public final void testProcess() throws Exception {
		XPathHelper lHelper = XPathHelper.newInstance(file1.toURI().toURL());
		NewTextAction lAction = RDFaExtractor.process(lHelper,
		        file1.getAbsolutePath(), context);
		String lExpected = "title=Weaving the Web&author=Tim Berners-Lee&coAuthor=&subTitle=&year=&publication=&pages=&volume=0&number=0&publisher=&place=&type=0&text=";
		assertEquals("extracted 1", lExpected, lAction.toString());

		lHelper = XPathHelper.newInstance(file2.toURI().toURL());
		lAction = RDFaExtractor.process(lHelper, file2.getAbsolutePath(),
		        context);
		lExpected = "title=Dissent, Public Space and the Politics of Citizenship: Riots and the Outside Agitator&author=Bruce D'Arcus&coAuthor=&subTitle=&year=2004&publication=Space & Polity&pages=355 - 370&volume=8&number=3&publisher=&place=&type=1&text=Citizenship, Dissent, Geopolitics, Law, Public Space, Race riots, State"
		        + NL + "[<i>DOI: 10.1080/1356257042000309652</i>]";
		assertEquals("extracted 2", lExpected, lAction.toString());

		lHelper = XPathHelper.newInstance(file3.toURI().toURL());
		lAction = RDFaExtractor.process(lHelper, file3.getAbsolutePath(),
		        context);
		lExpected = "title=Mycobacterium tuberculosis evades macrophage defenses by inhibiting plasma membrane repair., Nature Immunology&author=http://crossref.org/contributor/a5f6ee&coAuthor=&subTitle=&year=2009-06-01&publication=http://nature.com/issn/1234-5678&pages=899 - 906&volume=10&number=0&publisher=&place=&type=1&text=[<i>DOI: 10.1038/ni.1758</i>]";
		assertEquals("extracted 3", lExpected, lAction.toString());
	}

}
