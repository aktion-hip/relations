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
package org.elbe.relations.indexer.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.elbe.relations.data.search.AbstractSearching;
import org.elbe.relations.data.search.IIndexer;
import org.elbe.relations.data.search.IndexerDateField;
import org.elbe.relations.data.search.IndexerDateField.TimeResolution;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.search.RetrievedItem;
import org.elbe.relations.data.utility.RException;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.lucene.internal.DirectoryFactory;
import org.elbe.relations.search.RetrievedItemWithIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Lucene implementation of the <code>IIndexer</code> interface.
 *
 * @author Luthiger
 */
public class LuceneIndexer implements IIndexer {
	private static final Logger LOG = LoggerFactory.getLogger(LuceneIndexer.class);
	public static final Version LUCENE_VERSION = Version.LUCENE_4_10_1;

	private final DirectoryFactory directoryFactory = new FileSystemDirectoryFactory();

	// enum for language analyzers (see lucene-analyzers-common-4.10.1.jar)
	private enum LanguageAnalyzer {
		AR("ar", new ArabicAnalyzer()),
		BG("bg", new BulgarianAnalyzer()),
		BR("br", new BrazilianAnalyzer()),
		CA("ca", new CatalanAnalyzer()),
		CN("cn", new StandardAnalyzer()),
		CZ("cz", new CzechAnalyzer()),
		DA("da", new DanishAnalyzer()),
		DE("de", new GermanAnalyzer()),
		EL("el", new GreekAnalyzer()),
		EN("en", new StandardAnalyzer()),
		ES("es", new SpanishAnalyzer()),
		EU("eu", new BasqueAnalyzer()),
		FA("fa", new PersianAnalyzer()),
		FI("fi", new FinnishAnalyzer()),
		FR("fr", new FrenchAnalyzer()),
		GL("gl", new GalicianAnalyzer()),
		HI("hi", new HindiAnalyzer()),
		HU("hu", new HungarianAnalyzer()),
		HY("hy", new ArmenianAnalyzer()),
		ID("id", new IndonesianAnalyzer()),
		IT("it", new ItalianAnalyzer()),
		LV("lv", new LatvianAnalyzer()),
		NL("nl", new DutchAnalyzer()),
		NO("no", new NorwegianAnalyzer()),
		PT("pt", new PortugueseAnalyzer()),
		RO("ro", new RomanianAnalyzer()),
		RU("ru", new RussianAnalyzer()),
		SV("sv", new SwedishAnalyzer()),
		TH("th", new ThaiAnalyzer()),
		TR("tr", new TurkishAnalyzer());

		public final String isoLanguage;
		public final Analyzer analyzer;

		LanguageAnalyzer(final String inISOLanguage, final Analyzer inAnalyzer) {
			isoLanguage = inISOLanguage;
			analyzer = inAnalyzer;
			analyzer.setVersion(LUCENE_VERSION);
		}
	}

	@Override
	public void processIndexer(final IndexerHelper inIndexer, final File inIndexDir, final String inLanguage,
			final boolean inCreate) throws IOException {
		try (IndexWriter lWriter = new IndexWriter(directoryFactory.getDirectory(inIndexDir),
				createConfiguration(inLanguage, inCreate))) {
			for (final IndexerDocument lDoc : inIndexer.getDocuments()) {
				final Document lDocument = transformDoc(lDoc);
				lWriter.addDocument(lDocument);
			}
			lWriter.commit();
		} catch (final IOException exc) {
			LOG.error("Error with Lucene index encountered!", exc);
		}
	}

	private IndexWriterConfig createConfiguration(String inLanguage, final boolean inCreateNew) {
		final IndexWriterConfig out = new IndexWriterConfig(LUCENE_VERSION, getAnalyzer(inLanguage));
		out.setOpenMode(inCreateNew ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);
		return out;
	}

	@Override
	public void processIndexer(final IndexerHelper inIndexer, final File inIndexDir, final String inLanguage)
			throws IOException {
		processIndexer(inIndexer, inIndexDir, inLanguage, false);
	}

	private Analyzer getAnalyzer(final String inLanguage) {
		for (final LanguageAnalyzer lAnalyzer : LanguageAnalyzer.values()) {
			if (inLanguage.equals(lAnalyzer.isoLanguage)) {
				return lAnalyzer.analyzer;
			}
		}
		return new StandardAnalyzer();
	}

	private Document transformDoc(final IndexerDocument inDoc) {
		final Document outDocument = new Document();
		for (final IndexerField lField : inDoc.getFields()) {
			outDocument.add(createField(lField));
		}
		return outDocument;
	}

	private Field createField(IndexerField inField) {
		final Field.Store lStore = inField.getStoreValue() == IndexerField.Store.YES ? Field.Store.YES : Field.Store.NO;
		final IFieldFactory factory = inField.getFieldType() == IndexerField.Type.ID ? new StringFieldFactory()
				: new TextFieldFactory();

		String value = inField.getValue();
		if (inField instanceof IndexerDateField) {
			value = DateTools.timeToString(0, getResolution(((IndexerDateField) inField).getResolution()));
		}
		final Field out = factory.createField(inField.getFieldName(), value, lStore);
		if (out.fieldType().indexed()) {
			out.setBoost(inField.getBoost());
		}
		return out;
	}

	private DateTools.Resolution getResolution(final TimeResolution inResolution) {
		DateTools.Resolution outResolution = DateTools.Resolution.YEAR;
		if (inResolution == TimeResolution.MONTH) {
			outResolution = DateTools.Resolution.MONTH;
		} else if (inResolution == TimeResolution.DAY) {
			outResolution = DateTools.Resolution.DAY;
		} else if (inResolution == TimeResolution.HOUR) {
			outResolution = DateTools.Resolution.HOUR;
		} else if (inResolution == TimeResolution.MINUTE) {
			outResolution = DateTools.Resolution.MINUTE;
		} else if (inResolution == TimeResolution.SECOND) {
			outResolution = DateTools.Resolution.SECOND;
		} else if (inResolution == TimeResolution.MILLISECOND) {
			outResolution = DateTools.Resolution.MILLISECOND;
		}
		return outResolution;
	}

	@Override
	public int numberOfIndexed(final File inIndexDir) throws IOException {
		int outNumber = 0;
		try (IndexReader lReader = DirectoryReader.open(directoryFactory.getDirectory(inIndexDir))) {
			outNumber = lReader.numDocs();
		}
		return outNumber;
	}

	@Override
	public Collection<String> getAnalyzerLanguages() {
		final Collection<String> outLanguages = new ArrayList<String>();
		for (final LanguageAnalyzer lAnalyzer : LanguageAnalyzer.values()) {
			outLanguages.add(lAnalyzer.isoLanguage);
		}
		return outLanguages;
	}

	@Override
	public void deleteItemInIndex(final String inUniqueID, final String inFieldName, final File inIndexDir,
			String inLanguage) throws IOException {
		try (IndexWriter lWriter = new IndexWriter(directoryFactory.getDirectory(inIndexDir),
				createConfiguration(inLanguage, false))) {
			lWriter.deleteDocuments(new Term(inFieldName, inUniqueID));
			lWriter.commit();
		} catch (final IOException exc) {
			LOG.error("Error with Lucene index encountered!", exc);
		}
	}

	@Override
	public void initializeIndex(final File inIndexDir, String inLanguage) throws IOException {
		final Directory directory = directoryFactory.getDirectory(inIndexDir);
		final IndexWriter lNew = new IndexWriter(directory, createConfiguration(inLanguage, true));
		lNew.commit();
		lNew.close();
	}

	@Override
	public List<RetrievedItem> search(final String inQueryTerm, final File inIndexDir, final String inLanguage,
			final int inMaxHits) throws IOException, RException {
		try (IndexReader lReader = DirectoryReader.open(directoryFactory.getDirectory(inIndexDir))) {
			final IndexSearcher lSearcher = new IndexSearcher(lReader);
			final TopDocs lDocs = lSearcher.search(parseQuery(inQueryTerm, inLanguage), inMaxHits);
			return createResults(lDocs, lSearcher);

		} catch (final ParseException exc) {
			throw new RException(exc.getMessage());
		}
	}

	private List<RetrievedItem> createResults(final TopDocs inDocs, final IndexSearcher inSearcher)
			throws CorruptIndexException, IOException {
		final ScoreDoc[] lDocs = inDocs.scoreDocs;
		final List<RetrievedItem> out = new ArrayList<RetrievedItem>(lDocs.length);
		for (int i = 0; i < lDocs.length; i++) {
			final int lDocID = lDocs[i].doc;
			final Document lDocument = inSearcher.doc(lDocID);
			out.add(new RetrievedItemWithIcon(new UniqueID(lDocument.get(AbstractSearching.UNIQUE_ID)),
					lDocument.get(AbstractSearching.TITLE)));
		}
		return out;
	}

	private Query parseQuery(final String inQueryTerm, final String inLanguage) throws ParseException {
		final QueryParser outParser = new QueryParser(AbstractSearching.CONTENT_FULL, getAnalyzer(inLanguage));
		return outParser.parse(inQueryTerm);
	}

	// --- inner classes ---

	private static class FileSystemDirectoryFactory implements DirectoryFactory {

		@Override
		public Directory getDirectory(File inIndexDir) throws IOException {
			return FSDirectory.open(inIndexDir);
		}
	}

	// ---

	private static interface IFieldFactory {
		Field createField(String inName, String inValue, Field.Store inStored);
	}

	private static class TextFieldFactory implements IFieldFactory {

		@Override
		public Field createField(String inName, String inValue, Store inStored) {
			return new TextField(inName, inValue, inStored);
		}

	}

	private static class StringFieldFactory implements IFieldFactory {

		@Override
		public Field createField(final String inName, final String inValue, final Store inStored) {
			return new StringField(inName, inValue, inStored);
		}

	}

}
