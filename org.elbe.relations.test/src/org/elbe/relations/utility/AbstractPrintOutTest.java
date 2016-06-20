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
package org.elbe.relations.utility;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author lbenno
 *
 */
public class AbstractPrintOutTest {
	private static final String NL = "\n";
	private static final String XML1 = "<para><b>Dynamic Services (OSGI DS)</b> in Relations-RCP:" + NL
			+ "Wenn eine neue Komponente erzeugt worden ist, muss Inhalt von C:/Data/eclipse/workbenches/Relations/.metadata.pluginsorg.eclipse.pde.core/relations.product gelöscht werden (<b>ausser</b> <i>config.ini</i>), damit die neue Komponente vom System erkannt wird.</para>"
			+ NL
			+ "<para>Wird DS verwendet, so ist es vorteilhaft, wenn die Service-Konsumenten laufen, bevor die Service-Provider gestartet werden:"
			+ NL + "Beispiele:</para>";
	private static final String XML2 = "<para>Wird DS verwendet, so ist es vorteilhaft, wenn die Service-Konsumenten laufen, bevor die Service-Provider gestartet werden:"
			+ NL + "Beispiele:</para>";
	private static final String XML3 = "<para>$> tar czvf Relations-x.y.z_gtk.x86.tgz Relations/*</para>";
	private static final String XML4 = "<ul indent=\"0\"><li>111</li>" + NL + "<li>222</li>" + NL + "<li>333" + NL
			+ "<ul indent=\"1\"><li>aaa" + NL + "<ul indent=\"2\"><li>bbb</li>" + NL + "</ul></li><li>ccc</li>" + NL
			+ "<li>444</li>" + NL + "</ul></li><li>555</li>" + NL + "</ul>";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.elbe.relations.utility.AbstractPrintOut#prepareItemXML(java.lang.String)}
	 * .
	 */
	@Test
	public void testPrepareItemXML() {
		final String expected1 = "<para><b>Dynamic Services (OSGI DS)</b> in Relations-RCP:<br/>Wenn eine neue Komponente erzeugt worden ist, muss Inhalt von C:/Data/eclipse/workbenches/Relations/.metadata.pluginsorg.eclipse.pde.core/relations.product gelöscht werden (<b>ausser</b> <i>config.ini</i>), damit die neue Komponente vom System erkannt wird.</para><para>Wird DS verwendet, so ist es vorteilhaft, wenn die Service-Konsumenten laufen, bevor die Service-Provider gestartet werden:<br/>Beispiele:</para>";
		final String expected2 = "<para>Wird DS verwendet, so ist es vorteilhaft, wenn die Service-Konsumenten laufen, bevor die Service-Provider gestartet werden:<br/>Beispiele:</para>";
		final String expected3 = "<para>$> tar czvf Relations-x.y.z_gtk.x86.tgz Relations/*</para>";
		final String expected4 = "";

		final TestPrintOut printOut = new TestPrintOut();
		assertEquals("", printOut.prepareItemXML(""));
		assertEquals(expected1, printOut.prepareItemXML(XML1));
		assertEquals("aaa " + expected1 + " bbb", printOut.prepareItemXML("aaa " + XML1 + " bbb"));
		assertEquals(expected2, printOut.prepareItemXML(XML2));
		assertEquals("aaa " + expected2 + " bbb", printOut.prepareItemXML("aaa " + XML2 + " bbb"));
		assertEquals("<para>123</para>", printOut.prepareItemXML("<para>123</para>"));
		assertEquals("<para></para>", printOut.prepareItemXML("<para></para>"));
		assertEquals(expected3, printOut.prepareItemXML(XML3));
		assertEquals(expected4, printOut.prepareItemXML(XML4));
	}

	//
	private class TestPrintOut extends AbstractPrintOut {

		@Override
		protected String prepareItemXML(String itemXML) {
			return super.prepareItemXML(itemXML);
		}

		@Override
		public boolean isAvailable() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected String getXSLNameBody() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected String getXSLNameContent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void manageAfterOpenNew(File inPrintOut) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		protected void manageAfterReopen(File inPrintOut) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		protected void manageBeforeClose(File inPrintOut) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		protected void insertSection(String inSection) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		protected void insertDocBody(String inXML) throws IOException {
			// TODO Auto-generated method stub

		}

	}

}
