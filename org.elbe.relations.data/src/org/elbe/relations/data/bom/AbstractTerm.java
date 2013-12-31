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
 * Base class for all term items.
 * 
 * @author Luthiger Created on 29.08.2006
 */
@SuppressWarnings("serial")
public abstract class AbstractTerm extends AbstractItem {

	public AbstractTerm() {
		super();
	}

	/**
	 * @see IItem#getID()
	 */
	@Override
	public long getID() throws VException {
		return ((Long) get(TermHome.KEY_ID)).longValue();
	}

	/**
	 * @see IItem#getItemType()()
	 */
	@Override
	public int getItemType() {
		return IItem.TERM;
	}

	/**
	 * @see IItem#getTitle()
	 */
	@Override
	public String getTitle() throws VException {
		return get(TermHome.KEY_TITLE).toString();
	}

	@Override
	protected Timestamp[] getCreatedModified() throws VException {
		return new Timestamp[] { (Timestamp) get(TermHome.KEY_CREATED),
		        (Timestamp) get(TermHome.KEY_MODIFIED) };
	}

	@Override
	public void visit(final IItemVisitor inVisitor) throws VException {
		inVisitor.setTitle(getTitle());
		inVisitor.setTitleEditable(true);
		inVisitor.setText(getChecked(TermHome.KEY_TEXT));
		inVisitor.setTextEditable(true);
	}

	/**
	 * Returns the lightweight version of this model.
	 * 
	 * @return ILightWeightItem
	 * @throws BOMException
	 */
	@Override
	public ILightWeightItem getLightWeight() throws BOMException {
		try {
			return new LightWeightTerm(
			        ((Long) get(TermHome.KEY_ID)).longValue(), get(
			                TermHome.KEY_TITLE).toString(),
			        getChecked(TermHome.KEY_TEXT),
			        (Timestamp) get(TermHome.KEY_CREATED),
			        (Timestamp) get(TermHome.KEY_MODIFIED));
		}
		catch (final Exception exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * @see IItem#saveTitleText(String, String)
	 */
	@Override
	public void saveTitleText(final String inTitle, final String inText)
	        throws BOMException {
		save(inTitle, inText);
	}

	/**
	 * Saves the changes to the database.
	 * 
	 * @param inTitle
	 *            String
	 * @param inText
	 *            String
	 * @throws BOMException
	 */
	public void save(final String inTitle, final String inText)
	        throws BOMException {
		try {
			setModel(inTitle, inText);
			update(true);
			refreshItemInIndex();
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			if (TRUNCATION_STATE.equals(exc.getSQLState())) {
				throw new BOMTruncationException(TRUNCATION_MSG);
			}
			throw new BOMException(exc.getMessage());
		}
		catch (final IOException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Updates the model with the specified values.
	 * 
	 * @param inTitle
	 *            String
	 * @param inText
	 *            String
	 * @throws VException
	 */
	protected void setModel(final String inTitle, final String inText)
	        throws VException {
		set(TermHome.KEY_TITLE, inTitle);
		set(TermHome.KEY_TEXT, inText);
		set(TermHome.KEY_MODIFIED, new Timestamp(System.currentTimeMillis()));
	}

}