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
public class MetadataFormatModsTest {
	private static final String FILE_NAME = "resources/unapi_mods.xml";
	// private static final String FILE_NAME = "resources/unapi_mods2.xml";
	// private static final String FILE_NAME = "resources/unapi_mods3.xml";
	private File modsXml;

	private IEclipseContext context;

	@Mock
	private Logger log;

	@Mock
	private IDataService data;

	@Before
	public void setUp() throws Exception {
		modsXml = new File(FILE_NAME);

		context = EclipseContextFactory.create("test context");
		context.set(Logger.class, log);
		context.set(IDataService.class, data);
	}

	@Test
	public void testCreate() throws Exception {
		final AbstractMetadataFormat lParser = new MetadataFormatMods();
		final NewTextAction lAction = lParser.createAction(modsXml.toURI()
		        .toURL(), context);

		final String lExpected = "title=Effects of pulp mill effluents and restricted diet on growth and physiology of rainbow trout (Oncorhynchus mykiss).&author=Mattsson, K, Lehtinen, KJ, Tana, J, Hardig, J, Kukkonen, J, Nakari, T, Engstrom, C&coAuthor=&subTitle=&year=2001&publication=Ecotoxicol Environ Saf&pages=144-54&volume=49&number=2&publisher=&place=&type=0&text=Academic libraries need to change their recruiting and hiring procedures to stay competitive in today's changing marketplace.";
		assertEquals("parsed mods metadata", lExpected, lAction.toString());
	}

}
