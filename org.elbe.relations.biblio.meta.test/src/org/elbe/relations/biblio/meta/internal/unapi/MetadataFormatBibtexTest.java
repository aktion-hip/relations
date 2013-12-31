package org.elbe.relations.biblio.meta.internal.unapi;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
public class MetadataFormatBibtexTest {
	private static final String FILE_NAME = "resources/unapi_bibtex.txt";
	private static final String BIBTEX1 = "@Inbook{Mattsson2001,author=\"Mattsson, K. and Lehtinen, KJ and Tana, J. and Hardig, J. and Kukkonen, J. and Nakari, T. and Engstrom, C.\",chapter=\"Effects of pulp mill effluents and restricted diet on growth and physiology of rainbow trout (Oncorhynchus mykiss).\",title=\"Ecotoxicol Environ Saf\",publisher=\"Science Press\",year=\"2001\",volume=\"49\",issue=\"2\",pages=\"144--54\",issn=\"0147-6513\"}";
	private static final String BIBTEX2 = "@article{lin1973,author={Shen Lin and Brian W. Kernighan},title={An Effective Heuristic Algorithm for the Travelling-Salesman Problem},   journal = {Operations Research},   volume  = {21},year=1973,pages={498--516}}";
	private static final String BIBTEX3 = "@INCOLLECTION{Luthiger:07c,AUTHOR = {Luthiger, Benno and Carola Jungwirth},TITLE = {The Chase for OSS Quality: The Meaning of Member Roles, Motivations, and Business Models},PUBLISHER = {IGI Publishing},YEAR = 2007,EDITOR = {Sowe, Sulayman K. and Ioannis G. Stamelos and Ioannis Samoladas},BOOKTITLE = {Emerging Free and Open Source Software Practices},ADDRESS = {New York}}";
	private static final String BIBTEX4 = "@BOOK{Lutterbeck:06,AUTHOR = {Lutterbeck, Bernd; Bärwolff and Matthias; Gehring and Robert A. (Hrsg.)},TITLE = {Open Source Jahrbuch 2006},PUBLISHER = {Technische Universität},YEAR = 2006,ADDRESS = {Berlin}}";
	private File bibTex;

	private IEclipseContext context;

	@Mock
	private Logger log;

	@Mock
	private IDataService data;

	@Before
	public void setUp() throws Exception {
		bibTex = new File(FILE_NAME);

		context = EclipseContextFactory.create("test context");
		context.set(Logger.class, log);
		context.set(IDataService.class, data);
	}

	@Test
	public void testInbook() throws Exception {
		writeFile(BIBTEX1);

		final IUnAPIHandler lParser = new MetadataFormatBibtex();
		final NewTextAction lAction = lParser.createAction(bibTex.toURI()
		        .toURL(), context);
		final String lExpected = "title=Effects of pulp mill effluents and restricted diet on growth and physiology of rainbow trout (Oncorhynchus mykiss).&author=Mattsson, K. and Lehtinen, KJ and Tana, J. and Hardig, J. and Kukkonen, J. and Nakari, T. and Engstrom, C.&coAuthor=-&subTitle=&year=2001&publication=Ecotoxicol Environ Saf&pages=144--54&volume=49&number=0&publisher=Science Press&place=&type=2&text=";
		assertEquals("inbook", lExpected, lAction.toString());
	}

	@Test
	public void testArticle() throws Exception {
		writeFile(BIBTEX2);

		final IUnAPIHandler lParser = new MetadataFormatBibtex();
		final NewTextAction lAction = lParser.createAction(bibTex.toURI()
		        .toURL(), context);

		final String lExpected = "title=An Effective Heuristic Algorithm for the Travelling-Salesman Problem&author=Shen Lin &coAuthor=Brian W. Kernighan&subTitle=&year=1973&publication=Operations Research&pages=498--516&volume=21&number=0&publisher=&place=&type=1&text=";
		assertEquals("article", lExpected, lAction.toString());
	}

	public void testIncollection() throws Exception {
		writeFile(BIBTEX3);

		final IUnAPIHandler lParser = new MetadataFormatBibtex();
		final NewTextAction lAction = lParser.createAction(bibTex.toURI()
		        .toURL(), context);

		final String lExpected = "title=The Chase for OSS Quality: The Meaning of Member Roles, Motivations, and Business Models&author=Luthiger, Benno and Carola Jungwirth&coAuthor=Sowe, Sulayman K. and Ioannis G. Stamelos and Ioannis Samoladas&subTitle=&year=2007&publication=Emerging Free and Open Source Software Practices&pages=&volume=0&number=0&publisher=IGI Publishing&place=New York&type=2&text=Emerging Free and Open Source Software Practices, ";
		assertEquals("incollection", lExpected, lAction.toString());
	}

	public void testBook() throws Exception {
		writeFile(BIBTEX4);

		final IUnAPIHandler lParser = new MetadataFormatBibtex();
		final NewTextAction lAction = lParser.createAction(bibTex.toURI()
		        .toURL(), context);

		final String lExpected = "title=Open Source Jahrbuch 2006&author=Lutterbeck, Bernd; Bärwolff &coAuthor=Matthias; Gehring and Robert A. (Hrsg.)&subTitle=&year=2006&publication=&pages=&volume=0&number=0&publisher=Technische Universität&place=Berlin&type=0&text=";
		assertEquals("book", lExpected, lAction.toString());
	}

	/**
	 * @param inText
	 * @throws IOException
	 */
	private void writeFile(final String inText) throws IOException {
		final FileWriter lWriter = new FileWriter(bibTex);
		lWriter.write(inText);
		lWriter.close();
	}

}
