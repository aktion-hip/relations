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

package org.elbe.relations.biblio.meta.internal.unapi;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.db.IDataService;
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
public class MetadataFormatDCTest {
	private static final String FILE_NAME = "resources/unapi_dc.xml";
	private File dcXml;

	private IEclipseContext context;

	@Mock
	private Logger log;

	@Mock
	private IDataService data;

	@Before
	public void setUp() throws Exception {
		dcXml = new File(FILE_NAME);

		context = EclipseContextFactory.create("test context");
		context.set(Logger.class, log);
		context.set(IDataService.class, data);
	}

	@Test
	public void testCreate() throws Exception {
		final AbstractMetadataFormat lParser = new MetadataFormatDC();
		final NewTextAction lAction = lParser.createAction(dcXml.toURI()
		        .toURL(), context);

		final String lExpected = "title=Patterngrams; how to copy designs at home.&author=Olson, Nancy.&coAuthor=&subTitle=&year=[1973]&publication=&pages=&volume=0&number=0&publisher=New York, Fairchild Publications&place=&type=0&text=Dressmaking Tailoring";
		assertEquals("oai-dc", lExpected, lAction.toString());
	}

}
