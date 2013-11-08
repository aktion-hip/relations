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
package org.elbe.relations.internal.forms;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.junit.Test;

/**
 * 
 * @author Luthiger
 */
public class AbstractEditFormTest {

	@Test
	public void testKeyStrokes() {
		final KeyStroke lKeyStroke = KeyStroke.getInstance(SWT.MOD1, 'I');
		assertEquals(SWT.MOD1 | 'I',
				lKeyStroke.getModifierKeys() | lKeyStroke.getNaturalKey());
		assertEquals("CTRL+I", lKeyStroke.format());

		final KeySequence lSequence = KeySequence.getInstance(lKeyStroke);

		assertEquals("CTRL+I", lSequence.format());
		assertEquals(SWT.MOD1 | 'I', getKeyCode(lSequence));
	}

	private int getKeyCode(final KeySequence inSequence) {
		int out = 0;
		for (final KeyStroke lStroke : inSequence.getKeyStrokes()) {
			out |= lStroke.getModifierKeys() | lStroke.getNaturalKey();
		}
		return out;
	}

}
