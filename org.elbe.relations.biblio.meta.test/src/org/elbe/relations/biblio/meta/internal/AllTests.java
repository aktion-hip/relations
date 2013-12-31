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

package org.elbe.relations.biblio.meta.internal;

import org.elbe.relations.biblio.meta.internal.extract.ExcelExtractorTest;
import org.elbe.relations.biblio.meta.internal.extract.GifExtractorTest;
import org.elbe.relations.biblio.meta.internal.extract.JpgExtractorTest;
import org.elbe.relations.biblio.meta.internal.extract.OOExtractorTest;
import org.elbe.relations.biblio.meta.internal.extract.OfficeXMLExtractorTest;
import org.elbe.relations.biblio.meta.internal.extract.PdfExtractorTest;
import org.elbe.relations.biblio.meta.internal.extract.PowerPointExtractorTest;
import org.elbe.relations.biblio.meta.internal.extract.WordExtractorTest;
import org.elbe.relations.biblio.meta.internal.unapi.UnAPIProviderTest;
import org.elbe.relations.biblio.meta.internal.unapi.XMLFormatsParserTest;
import org.elbe.relations.biblio.meta.internal.utility.ExtractorUtilTest;
import org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObjectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 
 * @author lbenno
 */
@RunWith(Suite.class)
@SuiteClasses({ ExcelExtractorTest.class, GifExtractorTest.class,
        JpgExtractorTest.class, OfficeXMLExtractorTest.class,
        OOExtractorTest.class, PdfExtractorTest.class,
        PowerPointExtractorTest.class, WordExtractorTest.class,
        UnAPIProviderTest.class, XMLFormatsParserTest.class,
        ExtractorUtilTest.class, ListenerParameterObjectTest.class })
public class AllTests {

}
