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
package org.elbe.relations.data.bom;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.elbe.relations.data.utility.IBibliography;
import org.elbe.relations.data.utility.IItemVisitor;
import org.hip.kernel.exc.VException;
import org.xml.sax.SAXException;

/**
 * Base class for all text items.
 *
 * @author Luthiger Created on 01.09.2006
 */
@SuppressWarnings("serial")
public abstract class AbstractText extends AbstractItem implements IItem {
    // Constants for text type items.
    public static final int TYPE_BOOK = 0;
    public static final int TYPE_ARTICLE = 1;
    public static final int TYPE_CONTRIBUTION = 2;
    public static final int TYPE_WEBPAGE = 3;

    private final static String TEXT_ADD_TMPL = System.getProperty("line.separator") + "[%s...]"; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * AbstractText constructor.
     */
    public AbstractText() {
        super();
    }

    @Override
    public void visit(final IItemVisitor inVisitor) throws VException {
        inVisitor.setTitle(getTitle());
        inVisitor.setTitleEditable(true);
        String lText = getText();
        inVisitor.setRealText(lText);
        if (!lText.isEmpty()) {
            try {
                final String lBare = getStyleParser().getUntaggedText(lText);
                lText = String.format(TEXT_ADD_TMPL, lBare.substring(0, Math.min(lText.length(), 10)));
            } catch (final Exception exc) {
                throw new VException(exc.getMessage());
            }
        }
        inVisitor.setText(getBiblioHandler().render(this) + lText);
        inVisitor.setTextEditable(false);
    }

    /**
     * Returns a no operating <code>IStyleParser</code>. Subclasses should
     * override.
     *
     * @return {@link IStyleParser}
     */
    protected IStyleParser getStyleParser() {
        return new IStyleParser() {
            @Override
            public String getUntaggedText(final String inTagged) throws IOException, SAXException {
                return inTagged;
            }
        };
    }

    /**
     * Returns a no operating <code>IBiblioHandler</code>. Subclasses should
     * override.
     *
     * @return {@link IBibliography}
     */
    protected IBibliography getBiblioHandler() {
        return new IBibliography() {
            @Override
            public String render(final AbstractText inText) throws VException {
                return ""; //$NON-NLS-1$
            }
        };
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getID()
     */
    @Override
    public long getID() throws VException {
        return ((Long) get(TextHome.KEY_ID)).longValue();
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getItemType()
     */
    @Override
    public int getItemType() {
        return IItem.TEXT;
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getLightWeight()
     */
    @Override
    public ILightWeightItem getLightWeight() throws BOMException {
        try {
            return new LightWeightText(((Long) get(TextHome.KEY_ID)).longValue(), get(TextHome.KEY_TITLE).toString(),
                    getChecked(TextHome.KEY_TEXT), get(TextHome.KEY_AUTHOR).toString(),
                    getChecked(TextHome.KEY_COAUTHORS), getChecked(TextHome.KEY_SUBTITLE),
                    getChecked(TextHome.KEY_YEAR), getChecked(TextHome.KEY_PUBLICATION), getChecked(TextHome.KEY_PAGES),
                    Integer.parseInt(getChecked(TextHome.KEY_VOLUME)),
                    Integer.parseInt(getChecked(TextHome.KEY_NUMBER)), getChecked(TextHome.KEY_PUBLISHER),
                    getChecked(TextHome.KEY_PLACE), Integer.parseInt(getChecked(TextHome.KEY_TYPE)),
                    (Timestamp) get(TextHome.KEY_CREATED), (Timestamp) get(TextHome.KEY_MODIFIED));
        } catch (final Exception exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getTitle()
     */
    @Override
    public String getTitle() throws VException {
        return get(TextHome.KEY_TITLE).toString();
    }

    @Override
    protected Timestamp[] getCreatedModified() throws VException {
        return new Timestamp[] { (Timestamp) get(TextHome.KEY_CREATED), (Timestamp) get(TextHome.KEY_MODIFIED) };
    }

    /**
     * @see IItem#saveTitleText(String, String)
     */
    @Override
    public void saveTitleText(final String inTitle, final String inText) throws BOMException {
        try {
            setModel(inTitle, inText);
            update(true);
            setToEventStore(EventStoreHome.StoreType.UPDATE);
        } catch (VException | SQLException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    protected void setModel(final String inTitle, final String inText) throws VException {
        set(TextHome.KEY_TITLE, inTitle);
        set(TextHome.KEY_TEXT, inText);
        set(TextHome.KEY_MODIFIED, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Save changes of an edit dialog of all fields.
     *
     * @param inTitle
     *            String
     * @param inText
     *            String
     * @param inType
     *            Integer
     * @param inAuthor
     *            String
     * @param inCoAuthor
     *            String
     * @param inSubTitle
     *            String
     * @param inPublisher
     *            String
     * @param inYear
     *            String
     * @param inJournal
     *            String
     * @param inPages
     *            String
     * @param inArticleVolume
     *            Integer
     * @param inArticleNumber
     *            Integer
     * @param inLocation
     *            String
     * @throws BOMException
     */
    public void save(final String inTitle, final String inText, final Integer inType, final String inAuthor,
            final String inCoAuthor, final String inSubTitle, final String inPublisher, final String inYear,
            final String inJournal, final String inPages, final Integer inArticleVolume, final Integer inArticleNumber,
            final String inLocation) throws BOMException {
        try {
            setModel(inTitle, inText, inType, inAuthor, inCoAuthor, inSubTitle, inPublisher, inYear, inJournal, inPages,
                    inArticleVolume, inArticleNumber, inLocation);
            update(true);
            setToEventStore(EventStoreHome.StoreType.UPDATE);
            refreshItemInIndex();
        } catch (final VException exc) {
            throw new BOMException(exc.getMessage());
        } catch (final SQLException exc) {
            if (TRUNCATION_STATE.equals(exc.getSQLState())) {
                throw new BOMTruncationException(TRUNCATION_MSG);
            }
            throw new BOMException(exc.getMessage());
        } catch (final IOException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    protected void setModel(final String inTitle, final String inText, final Integer inType, final String inAuthor,
            final String inCoAuthor, final String inSubTitle, final String inPublisher, final String inYear,
            final String inJournal, final String inPages, final Integer inArticleVolume, final Integer inArticleNumber,
            final String inLocation) throws VException {
        set(TextHome.KEY_TITLE, inTitle);
        set(TextHome.KEY_TEXT, inText);
        set(TextHome.KEY_TYPE, inType);
        set(TextHome.KEY_AUTHOR, inAuthor);
        set(TextHome.KEY_COAUTHORS, inCoAuthor);
        set(TextHome.KEY_SUBTITLE, inSubTitle);
        set(TextHome.KEY_PUBLISHER, inPublisher);
        set(TextHome.KEY_YEAR, inYear);
        set(TextHome.KEY_PUBLICATION, inJournal);
        set(TextHome.KEY_PAGES, inPages);
        set(TextHome.KEY_VOLUME, inArticleVolume);
        set(TextHome.KEY_NUMBER, inArticleNumber);
        set(TextHome.KEY_PLACE, inLocation);
        set(TextHome.KEY_MODIFIED, new Timestamp(System.currentTimeMillis()));
    }

    public String getText() throws VException {
        return getChecked(TextHome.KEY_TEXT);
    }

    public String getAuthor() throws VException {
        return get(TextHome.KEY_AUTHOR).toString();
    }

    public String getCoAuthor() throws VException {
        return getChecked(TextHome.KEY_COAUTHORS);
    }

    public String getSubtitle() throws VException {
        return getChecked(TextHome.KEY_SUBTITLE);
    }

    public String getPublisher() throws VException {
        return getChecked(TextHome.KEY_PUBLISHER);
    }

    public String getYear() throws VException {
        return getChecked(TextHome.KEY_YEAR);
    }

    public String getPublication() throws VException {
        return getChecked(TextHome.KEY_PUBLICATION);
    }

    public String getPages() throws VException {
        return getChecked(TextHome.KEY_PAGES);
    }

    public String getVolume() throws VException {
        final String lVolume = getChecked(TextHome.KEY_VOLUME);
        if (lVolume.length() == 0) {
            return ""; //$NON-NLS-1$
        }
        return Integer.parseInt(lVolume) == 0 ? "" : lVolume; //$NON-NLS-1$
    }

    public String getNumber() throws VException {
        final String lNumber = getChecked(TextHome.KEY_NUMBER);
        if (lNumber.length() == 0) {
            return ""; //$NON-NLS-1$
        }
        return Integer.parseInt(lNumber) == 0 ? "" : lNumber; //$NON-NLS-1$
    }

    public String getPlace() throws VException {
        return getChecked(TextHome.KEY_PLACE);
    }

    public int getType() throws VException {
        return Integer.parseInt(get(TextHome.KEY_TYPE).toString());
    }
}
