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
package org.elbe.relations.data;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.elbe.relations.data.db.AbstractDBObjectCreator;

/**
 * @author Luthiger
 */
public class TestEmbeddedCreator extends AbstractDBObjectCreator {
    private static String XSL = "db_derby.xsl"; //$NON-NLS-1$

    @Override
    protected URL getXSL() {
        return getClass().getResource(XSL);
    }

    @Override
    protected URL getModelXML(final String xmlName) {
        final File parent = new File(AbstractDBObjectCreator.class.getResource("/").getPath()).getParentFile();
        final File xml = new File(parent, "resources/" + xmlName);
        try {
            return xml.toURI().toURL();
        } catch (final MalformedURLException exc) {
            // intentionally left empty
        }
        return null;
    }

}
