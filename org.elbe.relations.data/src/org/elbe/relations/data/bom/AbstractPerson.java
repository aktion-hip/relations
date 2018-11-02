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

import org.elbe.relations.data.utility.IItemVisitor;
import org.hip.kernel.exc.VException;

/**
 * Base class for all person items.
 *
 * @author Luthiger Created on 01.09.2006
 */
@SuppressWarnings("serial")
public abstract class AbstractPerson extends AbstractItem implements IItem {

    /**
     * AbstractPerson constructor
     */
    public AbstractPerson() {
        super();
    }

    @Override
    public void visit(final IItemVisitor visitor) throws VException {
        visitor.setTitle(getTitle());
        visitor.setTitleEditable(false);
        visitor.setSubTitle(getSubTitle());
        visitor.setText(getChecked(PersonHome.KEY_TEXT));
        visitor.setTextEditable(true);
    }

    private String getSubTitle() throws VException {
        final String outSubtitle = getChecked(PersonHome.KEY_FROM) + " - " + getChecked(PersonHome.KEY_TO); //$NON-NLS-1$
        return outSubtitle.trim();
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getID()
     */
    @Override
    public long getID() throws VException {
        return ((Long) get(PersonHome.KEY_ID)).longValue();
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getItemType()
     */
    @Override
    public int getItemType() {
        return IItem.PERSON;
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getLightWeight()
     */
    @Override
    public ILightWeightItem getLightWeight() throws BOMException {
        try {
            return new LightWeightPerson(((Long) get(PersonHome.KEY_ID)).longValue(),
                    get(PersonHome.KEY_NAME).toString(), getChecked(PersonHome.KEY_FIRSTNAME),
                    getChecked(PersonHome.KEY_TEXT), getChecked(PersonHome.KEY_FROM), getChecked(PersonHome.KEY_TO),
                    (Timestamp) get(PersonHome.KEY_CREATED), (Timestamp) get(PersonHome.KEY_MODIFIED));
        } catch (final Exception exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    /**
     * @see org.elbe.relations.data.bom.IItem#getTitle()
     */
    @Override
    public String getTitle() throws VException {
        final String name = get(PersonHome.KEY_NAME).toString();
        final String fistname = getChecked(PersonHome.KEY_FIRSTNAME);
        if ((fistname != null) && (fistname.length() > 0)) {
            return String.format("%s, %s", name, fistname); //$NON-NLS-1$
        }
        return name;
    }

    @Override
    protected Timestamp[] getCreatedModified() throws VException {
        return new Timestamp[] { (Timestamp) get(PersonHome.KEY_CREATED), (Timestamp) get(PersonHome.KEY_MODIFIED) };
    }

    /**
     * @see IItem#saveTitleText(String, String)
     */
    @Override
    public void saveTitleText(final String inTitle, final String inText) throws BOMException {
        try {
            setModel(inTitle, inText);
            setToEventStore(EventStoreHome.StoreType.UPDATE);
            update(true);
        } catch (VException | SQLException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    protected void setModel(final String title, final String text) throws VException {
        set(PersonHome.KEY_TEXT, text);
        set(PersonHome.KEY_MODIFIED, new Timestamp(System.currentTimeMillis()));
    }

    /** Save changes after an edit of all fields.
     *
     * @param name
     * @param firstName
     * @param text
     * @param from
     * @param to
     * @throws BOMException */
    public void save(final String name, final String firstName, final String text, final String from, final String to)
            throws BOMException {
        try {
            setModel(name, firstName, text, from, to);
            setToEventStore(EventStoreHome.StoreType.UPDATE);
            update(true);
            refreshItemInIndex();
        } catch (final VException | IOException exc) {
            throw new BOMException(exc.getMessage());
        } catch (final SQLException exc) {
            if (TRUNCATION_STATE.equals(exc.getSQLState())) {
                throw new BOMTruncationException(TRUNCATION_MSG);
            }
            throw new BOMException(exc.getMessage());
        }
    }

    protected void setModel(final String name, final String firstName, final String text, final String from,
            final String to) throws VException {
        set(PersonHome.KEY_NAME, name);
        set(PersonHome.KEY_FIRSTNAME, firstName);
        set(PersonHome.KEY_TEXT, text);
        set(PersonHome.KEY_FROM, from);
        set(PersonHome.KEY_TO, to);
        set(PersonHome.KEY_MODIFIED, new Timestamp(System.currentTimeMillis()));
    }

}
