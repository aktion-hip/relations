/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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
package org.elbe.relations.internal.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.xml.transform.TransformerException;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.bom.XMLSerializerSpecial;
import org.elbe.relations.internal.models.ItemWithIcon;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.utility.RelatedItemHelper;
import org.elbe.relations.internal.wizards.PrintOutWizardPage;
import org.elbe.relations.services.IPrintOut;
import org.hip.kernel.bom.AlternativeModel;
import org.hip.kernel.bom.impl.XMLSerializer;
import org.hip.kernel.exc.VException;

/** Class to manage the print out of selected sets of items.
 *
 * @author Luthiger */
@SuppressWarnings("restriction")
@Creatable
@Singleton
public class PrintOutManager {

    private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$
    private final static String DOC_TITLE = RelationsMessages.getString("PrintOutManager.doc.title"); //$NON-NLS-1$
    private final static MessageFormat DOC_SUBTITLE = new MessageFormat(
            RelationsMessages.getString("PrintOutManager.doc.subtitle")); //$NON-NLS-1$
    private final static String XML_ITEM = "<Item>%s</Item>"; //$NON-NLS-1$
    private final static String XML_REFERENCE = "<related_item type=\"%s\">%s</related_item>"; //$NON-NLS-1$
    private final static String XML_REFERENCES = "<related>%s</related>"; //$NON-NLS-1$

    private final IItemCollector[] CONTENT_SETS = new IItemCollector[] {
            new SimleCollector(), new SelectedAndRelated(),
            new WholeDBCollector() };
    private IItemCollector contentSet;

    private IPrintOut printer;
    private Collection<UniqueID> uniqueIDs;
    private int contentScope = PrintOutWizardPage.SELECTED_RELATED;
    private boolean printOutReferences = true;

    @Inject
    private LanguageService language;

    @Inject
    private BibliographyController biblioController;

    @Inject
    private Logger log;

    @Inject
    private IDataService data;

    /**
     * PrintOutManager constructor.
     */
    public PrintOutManager() {
        super();
        this.uniqueIDs = new ArrayList<UniqueID>();
    }

    /**
     * This method initializes a new print out process with the specified
     * settings.
     *
     * @param inFileName
     *            String the file name of the document to print out the content.
     * @param inConfig
     *            AvailablePrintOut the print out configuration
     * @return boolean <code>true</code> if the process initiation has been
     *         successful.
     */
    public boolean initNew(final String inFileName, final IPrintOut inPrinter) {
        this.printer = inPrinter;
        if (this.printer == null) {
            return false;
        }

        this.uniqueIDs = new ArrayList<>();
        try {
            this.printer.setDocTitle(DOC_TITLE);
            this.printer.setDocSubTitle(getSubTitle());
            this.printer.openNew(inFileName);
            return true;
        }
        catch (final Exception exc) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    RelationsMessages.getString("PrintOutManager.msg.title"), //$NON-NLS-1$
                    String.format(RelationsMessages.getString(
                            "PrintOutManager.error.msg"), inFileName)); //$NON-NLS-1$
            this.log.error(exc, exc.getMessage());
        }
        return false;
    }

    /**
     * This method initalizes the process to print out further items according
     * to the specified scope using the settings specified to initialize the
     * print out.
     *
     * @param inFileName
     *            String the file name of the document to print out the content.
     * @return boolean <code>true</code> if the process initiation has been
     *         successful.
     */
    public boolean initFurther(final String inFileName) {
        try {
            this.printer.openAppend(inFileName);
            return true;
        }
        catch (final IOException exc) {
            this.log.error(exc, exc.getMessage());
        }
        return false;
    }

    /**
     * @param inContentScope
     *            int constant for the content scope, e.g.
     *            <code>PrintOutWizardPage#SELECTED_RELATED</code>.
     * @see PrintOutWizardPage#SELECTED_RELATED etc.
     */
    public void setContentScope(final int inContentScope) {
        this.contentScope = inContentScope;
        this.contentSet = this.CONTENT_SETS[inContentScope];
    }

    /**
     * @return int the scope of the last print out.
     * @see PrintOutWizardPage#SELECTED_RELATED
     */
    public int getContentSope() {
        return this.contentScope;
    }

    /**
     * @param inPrintOutReferences
     *            boolean <code>true</code> if the item's references are
     *            displayed in the print out.
     */
    public void setPrintOutReferences(final boolean inPrintOutReferences) {
        this.printOutReferences = inPrintOutReferences;
    }

    /**
     * @return boolean the PrintOutReferences setting of the last print out.
     */
    public boolean getPrintOutReferences() {
        return this.printOutReferences;
    }

    private String getSubTitle() {
        DOC_SUBTITLE.setLocale(this.language.getAppLocale());
        return DOC_SUBTITLE.format(new Object[] { this.data.getDBName(),
                new Date(System.currentTimeMillis()) });
    }

    /**
     * Returns the set of <code>IItem</code>s to print out.
     *
     * @param inSelected
     *            IItem
     * @return Collection of <code>IItem</code>s
     * @throws VException
     * @throws SQLException
     */
    public Collection<IItem> getItemSet(final IItem inSelected)
            throws VException, SQLException {
        return this.contentSet.collectItems(inSelected);
    }

    /**
     * Checks whether the specified is printed yet.
     *
     * @param inItem
     *            IItem
     * @return boolean <code>true</code> if item isn't printed yet.
     * @throws VException
     */
    private boolean isNotPrinted(final IItem inItem) throws VException {
        final UniqueID lID = new UniqueID(inItem.getItemType(), inItem.getID());
        if (this.uniqueIDs.contains(lID)) {
            return false;
        }
        this.uniqueIDs.add(lID);
        return true;
    }

    /**
     * Prints the specified item to the print out document. The print is
     * checked, i.e. the item is printed only if it hasn't printed yet to the
     * document.
     *
     * @param inItem
     *            IItem
     * @return int number of printed items
     * @throws VException
     * @throws IOException
     */
    public int printItem(final IItem inItem) throws VException, IOException {
        int outPrinted = 0;
        if (isNotPrinted(inItem)) {
            final XMLSerializer lVisitor = new XMLSerializerSpecial(
                    this.biblioController, this.log);
            inItem.accept(lVisitor);
            String lXML = lVisitor.toString();
            try {
                if (this.printOutReferences) {
                    lXML += collectReferences(inItem);
                }
                this.printer.printItem(String.format(XML_ITEM, lXML));
            }
            catch (SQLException | TransformerException exc) {
                this.log.error(exc, exc.getMessage());
            }

            outPrinted++;
        }
        return outPrinted;
    }

    private String collectReferences(final IItem inItem)
            throws VException, SQLException {
        final StringBuilder outReferences = new StringBuilder();
        for (final ItemWithIcon lItem : RelatedItemHelper
                .getRelatedItems(inItem)) {
            outReferences.append(String.format(XML_REFERENCE,
                    lItem.getItem().getItemType(), lItem.getItem().getTitle()))
            .append(NL);
        }
        return String.format(XML_REFERENCES, new String(outReferences));
    }

    /**
     * Check whether there is a print out process started.
     *
     * @return boolean <code>true</code> if a print out has been started and
     *         some items have been printed out.
     */
    public boolean isPrinting() {
        return !this.uniqueIDs.isEmpty();
    }

    /**
     * Close the print out document.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        this.printer.close();
    }

    // --- inner classes ---

    private interface IItemCollector {
        Collection<IItem> collectItems(IItem inSelected)
                throws VException, SQLException;
    }

    private class SimleCollector implements IItemCollector {
        @Override
        public Collection<IItem> collectItems(final IItem inSelected)
                throws VException, SQLException {
            final Collection<IItem> outItems = new ArrayList<>();
            outItems.add(inSelected);
            return outItems;
        }
    }

    private class SelectedAndRelated implements IItemCollector {
        @Override
        public Collection<IItem> collectItems(final IItem inSelected)
                throws VException, SQLException {
            final Collection<IItem> outItems = new ArrayList<>();
            outItems.add(inSelected);
            for (final ItemWithIcon lItem : RelatedItemHelper
                    .getRelatedItems(inSelected)) {
                outItems.add(lItem.getItem());
            }
            return outItems;
        }
    }

    private class WholeDBCollector implements IItemCollector {
        @Override
        public Collection<IItem> collectItems(final IItem selected)
                throws VException, SQLException {
            return new ItemCollectionWrapper(PrintOutManager.this.data.getAll());
        }
    }

    // Two helper classes to get an iterator that returns IItem objects when
    // calling the next() method.
    // These are used for the <code>WholeDBCollector</code>
    @SuppressWarnings("serial")
    private class ItemCollectionWrapper extends ArrayList<IItem> {
        private final Collection<AlternativeModel> wrapped;

        public ItemCollectionWrapper(final Collection<AlternativeModel> collection) {
            this.wrapped = collection;
        }

        @Override
        public Iterator<IItem> iterator() {
            return new ItemIteratorWrapper(this.wrapped.iterator());
        }

        @Override
        public synchronized int size() {
            return this.wrapped.size();
        }
    }

    private class ItemIteratorWrapper implements Iterator<IItem> {
        private final Iterator<AlternativeModel> wrapped;

        public ItemIteratorWrapper(final Iterator<AlternativeModel> iterator) {
            this.wrapped = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.wrapped.hasNext();
        }

        @Override
        public IItem next() {
            final AlternativeModel item = this.wrapped.next();
            try {
                if (item instanceof final IItem leight) {
                    return PrintOutManager.this.data.retrieveItem(new UniqueID(leight.getItemType(), leight.getID()));
                }
            }
            catch (final BOMException | VException exc) {
                PrintOutManager.this.log.error(exc, exc.getMessage());
            }
            return null;
        }

        @Override
        public void remove() {
            this.wrapped.remove();
        }
    }

}
