/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.internal.bom;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.internal.controller.BibliographyController;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.impl.XMLSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author lbenno
 */
@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class XMLSerializerSpecialTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String TEXT_VAL = "<ul indent=\"0\"><li>111</li>" + NL + "<li>222</li>" + NL
			+ "<li>333\n<ul indent=\"1\"><li>aaa\n<ul indent=\"2\"><li>bbb</li>" + NL + "</ul></li><li>ccc</li>" + NL
			+ "<li>444</li>" + NL + "</ul></li><li>555</li>" + NL + "</ul>";
	private static final String EXPECTED = NL + "<Text><ul indent=\"0\"><li>111</li>" + NL + "<li>222</li>" + NL
			+ "<li>333" + NL + "<ul indent=\"1\"><li>aaa" + NL + "<ul indent=\"2\"><li>bbb</li>" + NL
			+ "</ul></li><li>ccc</li>" + NL + "<li>444</li>" + NL + "</ul></li><li>555</li>" + NL + "</ul></Text>";

	@Mock
	private BibliographyController biblioController;

	@Mock
	private Logger log;

	@Mock
	private Property property;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		when(property.getName()).thenReturn("Text");
		when(property.getValue()).thenReturn(TEXT_VAL);
	}

	/**
	 * Test method for
	 * {@link org.hip.kernel.bom.AbstractDomainObjectVisitor#visitProperty(org.hip.kernel.bom.Property)}
	 * .
	 */
	@Test
	public void testVisitProperty() {
		final XMLSerializer serializer = new XMLSerializerSpecial(biblioController, log);
		serializer.visitProperty(property);
		assertEquals(EXPECTED, serializer.toString());
	}

}
