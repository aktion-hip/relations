/**
 *
 */
package org.elbe.relations.lucene.internal;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/** Helper class providing factory methods for lucene index fields.
 *
 * @author lbenno */
public class FieldFactoryHelper {

    private FieldFactoryHelper() {
        // prevent instantiation
    }

    public enum IndexField {
        // @formatter:off
        TERM_TITLE("", Field.Store.YES, new TextFieldFactory()),
        TERM_1("", Field.Store.YES, new TextFieldFactory()),
        TERM_2("", Field.Store.YES, new StringFieldFactory()),
        TERM_3("", Field.Store.YES, new StoredFieldFactory());
        // @formatter:on

        public final String fieldName;
        private final Field.Store storeValue;
        private final IFieldFactory factory;

        IndexField(final String fieldName, final Field.Store store, final IFieldFactory factory) {
            this.fieldName = fieldName;
            this.storeValue = store;
            this.factory = factory;
        }

        public Field createField(final String value) {
            return this.factory.createField(this.fieldName, value, this.storeValue);
        }
    }

    // ---

    private interface IFieldFactory {
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
