/**
 *
 */
package org.elbe.relations.lucene.internal;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * @author lbenno
 *
 */
public class FieldFactoryHelper {

	private FieldFactoryHelper() {
		// prevent instantiation
	}

	public enum IndexField {
		TERM_TITLE("", Field.Store.YES, new TextFieldFactory(), 1), TERM_1("", Field.Store.YES, new TextFieldFactory(),
				1), TERM_2("", Field.Store.YES, new StringFieldFactory(), 1), TERM_3("", Field.Store.YES,
						new StoredFieldFactory(), 1);

		public final String fieldName;
		private final Field.Store storeValue;
		private final IFieldFactory factory;
		private final float boostFactor;

		IndexField(final String inFieldName, final Field.Store inStore, final IFieldFactory inFactory,
				final float inBoost) {
			fieldName = inFieldName;
			storeValue = inStore;
			factory = inFactory;
			boostFactor = inBoost;
		}

		public Field createField(final String inValue) {
			final Field out = factory.createField(fieldName, inValue, storeValue);
			if (out.fieldType().indexed()) {
				out.setBoost(boostFactor);
			}
			return out;
		}
	}

	// ---

	private static interface IFieldFactory {
		Field createField(String inName, String inValue, Field.Store inStored);
	}

	private static class TextFieldFactory implements IFieldFactory {

		@Override
		public Field createField(final String inName, final String inValue, final Store inStored) {
			return new TextField(inName, inValue, inStored);
		}

	}

	private static class StringFieldFactory implements IFieldFactory {

		@Override
		public Field createField(final String inName, final String inValue, final Store inStored) {
			return new StringField(inName, inValue, inStored);
		}

	}

	private static class StoredFieldFactory implements IFieldFactory {

		@Override
		public Field createField(final String inName, final String inValue, final Store inStored) {
			return new StoredField(inName, inValue);
		}

	}

}
