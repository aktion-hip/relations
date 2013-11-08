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
package org.elbe.relations.indexer.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
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
import org.elbe.relations.search.RetrievedItemWithIcon;

/**
 * The Lucene implementation of the <code>IIndexer</code> interface.
 * 
 * @author Luthiger
 */
public class LuceneIndexer implements IIndexer {
	// enum for language analyzers (see
	// contrib/analyzers/lucene-analyzers-2.2.0.jar)
	private enum LanguageAnalyzer {
		EN("en", new StandardAnalyzer()), //$NON-NLS-1$
		DE("de", new GermanAnalyzer()), //$NON-NLS-1$
		BR("br", new BrazilianAnalyzer()), //$NON-NLS-1$
		CN("cn", new ChineseAnalyzer()), //$NON-NLS-1$
		CZ("cz", new CzechAnalyzer()), //$NON-NLS-1$
		EL("el", new GreekAnalyzer()), //$NON-NLS-1$
		FR("fr", new FrenchAnalyzer()), //$NON-NLS-1$
		NL("nl", new DutchAnalyzer()), //$NON-NLS-1$
		RU("ru", new RussianAnalyzer()), //$NON-NLS-1$
		TH("th", new ThaiAnalyzer()); //$NON-NLS-1$

		public final String isoLanguage;
		public final Analyzer analyzer;

		LanguageAnalyzer(final String inISOLanguage, final Analyzer inAnalyzer) {
			isoLanguage = inISOLanguage;
			analyzer = inAnalyzer;
		}
	}

	@Override
	public void processIndexer(final IndexerHelper inIndexer,
			final File inIndexDir, final String inLanguage,
			final boolean inCreate) throws IOException {
		IndexWriter lWriter = null;
		try {
			lWriter = new IndexWriter(inIndexDir, getAnalyzer(inLanguage),
					inCreate, IndexWriter.MaxFieldLength.UNLIMITED);
			for (final IndexerDocument lDoc : inIndexer.getDocuments()) {
				final Document lDocument = transformDoc(lDoc);
				lWriter.addDocument(lDocument);
			}
			lWriter.commit();
			synchronized (this) {
				lWriter.optimize();
			}
		} finally {
			if (lWriter != null) {
				lWriter.close();
			}
		}
	}

	@Override
	public void processIndexer(final IndexerHelper inIndexer,
			final File inIndexDir, final String inLanguage) throws IOException {
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

	private Fieldable createField(final IndexerField inField) {
		// translate store value
		final IndexerField.Store lFieldStore = inField.getStoreValue();
		Field.Store lStore = Field.Store.YES;
		if (lFieldStore == IndexerField.Store.NO) {
			lStore = Field.Store.NO;
		} else if (lFieldStore == IndexerField.Store.COMPRESS) {
			lStore = Field.Store.COMPRESS;
		}

		// translate index value
		final IndexerField.Index lFieldIndex = inField.getIndexValue();
		Field.Index lIndex = Field.Index.NO;
		if (lFieldIndex == IndexerField.Index.NOT_ANALYZED_NO_NORMS) {
			lIndex = Field.Index.NOT_ANALYZED_NO_NORMS;
		} else if (lFieldIndex == IndexerField.Index.ANALYZED) {
			lIndex = Field.Index.ANALYZED;
		} else if (lFieldIndex == IndexerField.Index.NOT_ANALYZED) {
			lIndex = Field.Index.NOT_ANALYZED;
		} else if (lFieldIndex == IndexerField.Index.ANALYZED_NO_NORMS) {
			lIndex = Field.Index.ANALYZED_NO_NORMS;
		}

		String lValue = inField.getValue();
		if (inField instanceof IndexerDateField) {
			final IndexerDateField lDateField = (IndexerDateField) inField;
			lValue = DateTools.timeToString(lDateField.getTime(),
					getResolution(lDateField.getResolution()));
		}
		return new Field(inField.getFieldName(), lValue, lStore, lIndex);
	}

	private Resolution getResolution(final TimeResolution inResolution) {
		DateTools.Resolution outResolution = DateTools.Resolution.YEAR;
		if (inResolution == TimeResolution.MONTH) {
			outResolution = Resolution.MONTH;
		} else if (inResolution == TimeResolution.DAY) {
			outResolution = Resolution.DAY;
		} else if (inResolution == TimeResolution.HOUR) {
			outResolution = Resolution.HOUR;
		} else if (inResolution == TimeResolution.MINUTE) {
			outResolution = Resolution.MINUTE;
		} else if (inResolution == TimeResolution.SECOND) {
			outResolution = Resolution.SECOND;
		} else if (inResolution == TimeResolution.MILLISECOND) {
			outResolution = Resolution.MILLISECOND;
		}

		return outResolution;
	}

	@Override
	public int numberOfIndexed(final File inIndexDir) throws IOException {
		int outNumber = 0;
		final IndexReader lReader = IndexReader.open(FSDirectory
				.getDirectory(inIndexDir));
		try {
			outNumber = lReader.numDocs();
		} finally {
			lReader.close();
		}
		return outNumber;
	}

	@Override
	public Collection<String> getAnalyzerLanguages() {
		final Collection<String> outLanguages = new Vector<String>();
		for (final LanguageAnalyzer lAnalyzer : LanguageAnalyzer.values()) {
			outLanguages.add(lAnalyzer.isoLanguage);
		}
		return outLanguages;
	}

	@Override
	public void deleteItemInIndex(final String inUniqueID,
			final String inFieldName, final File inIndexDir) throws IOException {
		IndexReader lReader = null;
		try {
			lReader = IndexReader.open(inIndexDir);
			lReader.deleteDocuments(new Term(inFieldName, inUniqueID));
		} finally {
			lReader.close();
		}
	}

	@Override
	public void initializeIndex(final File inIndexDir) throws IOException {
		IndexWriter lWriter = null;
		try {
			lWriter = new IndexWriter(inIndexDir,
					getAnalyzer(""), true, IndexWriter.MaxFieldLength.UNLIMITED); //$NON-NLS-1$
			lWriter.commit();
		} finally {
			if (lWriter != null)
				lWriter.close();
		}
	}

	@Override
	public List<RetrievedItem> search(final String inQueryTerm,
			final File inIndexDir, final String inLanguage, final int inMaxHits)
			throws IOException, RException {
		final Searcher lSearcher = new IndexSearcher(
				FSDirectory.getDirectory(inIndexDir));
		try {
			final TopDocs lDocs = lSearcher.search(
					parseQuery(inQueryTerm, inLanguage), inMaxHits);
			return createResults(lDocs, lSearcher);
		}
		catch (final ParseException exc) {
			throw new RException(exc.getMessage());
		} finally {
			lSearcher.close();
		}
	}

	private List<RetrievedItem> createResults(final TopDocs inDocs,
			final Searcher inSearcher) throws CorruptIndexException,
			IOException {
		final ScoreDoc[] lDocs = inDocs.scoreDocs;
		final List<RetrievedItem> out = new Vector<RetrievedItem>(lDocs.length);
		for (int i = 0; i < lDocs.length; i++) {
			final int lDocID = lDocs[i].doc;
			final Document lDocument = inSearcher.doc(lDocID);
			out.add(new RetrievedItemWithIcon(new UniqueID(lDocument
					.get(AbstractSearching.ITEM_ID)), lDocument
					.get(AbstractSearching.TITLE)));
		}
		return out;
	}

	private Query parseQuery(final String inQueryTerm, final String inLanguage)
			throws ParseException {
		final QueryParser outParser = new QueryParser(
				AbstractSearching.CONTENT_FULL, getAnalyzer(inLanguage));
		return outParser.parse(inQueryTerm);
	}

}
